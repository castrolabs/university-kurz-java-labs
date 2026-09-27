import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.k3s.K3sContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

class ServiceMonitorTest {

    static final String OPERATOR =
            "https://github.com/prometheus-operator/prometheus-operator/releases/download/v0.94.1/bundle.yaml";

    static final K3sContainer k3s = new K3sContainer(DockerImageName.parse("rancher/k3s:v1.36.4-k3s1"));
    static String up = "";

    @BeforeAll
    static void deploy() throws Exception {
        k3s.start();
        for (String f : new String[] {"k8s/monitoring.yaml", "k8s/app.yaml", "k8s/servicemonitor.yaml"}) {
            k3s.copyFileToContainer(MountableFile.forClasspathResource(f), "/lab/" + f);
        }
        // The operator's CRDs are too big for client-side apply's annotation, hence --server-side.
        kubectl("apply", "--server-side", "-f", OPERATOR);
        kubectl("rollout", "status", "deploy/prometheus-operator", "--timeout=180s");
        kubectl("wait", "--for=condition=Established", "crd/prometheuses.monitoring.coreos.com",
                "crd/servicemonitors.monitoring.coreos.com", "--timeout=60s");
        kubectl("apply", "-f", "/lab/k8s/app.yaml");
        kubectl("apply", "-f", "/lab/k8s/servicemonitor.yaml");
        // The ServiceMonitor exists before Prometheus starts, so it is in the first configuration. Later
        // changes reach Prometheus through a Secret volume, which takes one to three minutes.
        kubectl("apply", "-f", "/lab/k8s/monitoring.yaml");
        for (int i = 0; i < 60 && k3s.execInContainer("kubectl", "-n", "monitoring", "get", "pod", "prometheus-lab-0")
                .getExitCode() != 0; i++) {
            Thread.sleep(2000);
        }
        kubectl("-n", "monitoring", "wait", "--for=condition=Ready", "pod/prometheus-lab-0", "--timeout=180s");
        kubectl("-n", "shop", "rollout", "status", "deploy/example-app", "--timeout=120s");

        for (int i = 0; i < 60 && !up.contains("\"1\""); i++) {   // a few scrapes
            Thread.sleep(3000);
            up = query("up{namespace=\"shop\"}");
        }
    }

    @AfterAll
    static void shutdown() {
        k3s.stop();
    }

    @Test
    void bothReplicasAreScraped() throws Exception {
        if (up.split("\"1\"\\]").length - 1 != 2) {
            String targets = prometheus("/api/v1/targets");
            Matcher dropped = Pattern.compile("\"droppedTargetCounts\":(\\{[^}]*\\}|null)").matcher(targets);
            assertEquals(2, up.split("\"1\"\\]").length - 1, "expected 2 healthy targets in shop."
                    + "\nscrape pools (one per ServiceMonitor endpoint Prometheus loaded): "
                    + prometheus("/api/v1/scrape_pools").replaceAll(".*\"scrapePools\":", "").replace("}}", "")
                    + "\nhealthy targets: " + (targets.split("\"health\":\"up\"").length - 1)
                    + "\ndiscovered but dropped by the ServiceMonitor's rules: " + (dropped.find() ? dropped.group(1) : "?"));
        }
    }

    @Test
    void appMetricsAreStored() throws Exception {
        assertTrue(query("version{namespace=\"shop\"}").contains("example-app"),
                "the app's own metrics are not in Prometheus");
    }

    private static String query(String promql) throws Exception {
        return prometheus("/api/v1/query?query=" + URLEncoder.encode(promql, StandardCharsets.UTF_8));
    }

    private static String prometheus(String path) throws Exception {
        return kubectl("-n", "monitoring", "exec", "prometheus-lab-0", "-c", "prometheus", "--",
                "wget", "-qO-", "http://localhost:9090" + path);
    }

    private static String kubectl(String... args) throws Exception {
        String[] command = Stream.concat(Stream.of("kubectl"), Arrays.stream(args)).toArray(String[]::new);
        ExecResult result = k3s.execInContainer(command);
        assertEquals(0, result.getExitCode(), String.join(" ", command) + " failed: " + result.getStderr());
        return result.getStdout().trim();
    }
}
