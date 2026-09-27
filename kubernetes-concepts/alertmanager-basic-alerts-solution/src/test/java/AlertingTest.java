import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.k3s.K3sContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

class AlertingTest {

    static final String OPERATOR =
            "https://github.com/prometheus-operator/prometheus-operator/releases/download/v0.94.1/bundle.yaml";

    static final K3sContainer k3s = new K3sContainer(DockerImageName.parse("rancher/k3s:v1.36.4-k3s1"));
    static String alertmanagerAlerts = "";

    @BeforeAll
    static void deploy() throws Exception {
        k3s.start();
        for (String f : new String[] {"k8s/monitoring.yaml", "k8s/app.yaml", "k8s/rules.yaml"}) {
            k3s.copyFileToContainer(MountableFile.forClasspathResource(f), "/lab/" + f);
        }
        kubectl("apply", "--server-side", "-f", OPERATOR);
        kubectl("rollout", "status", "deploy/prometheus-operator", "--timeout=180s");
        kubectl("wait", "--for=condition=Established", "crd/prometheuses.monitoring.coreos.com",
                "crd/prometheusrules.monitoring.coreos.com", "crd/alertmanagers.monitoring.coreos.com", "--timeout=60s");
        kubectl("apply", "-f", "/lab/k8s/app.yaml");
        // Rules created before Prometheus starts are in its first configuration. Later changes arrive
        // through a ConfigMap volume, which takes one to three minutes.
        kubectl("apply", "-f", "/lab/k8s/rules.yaml");
        kubectl("apply", "-f", "/lab/k8s/monitoring.yaml");
        for (String pod : new String[] {"prometheus-lab-0", "alertmanager-lab-0"}) {
            for (int i = 0; i < 60 && k3s.execInContainer("kubectl", "-n", "monitoring", "get", "pod", pod)
                    .getExitCode() != 0; i++) {
                Thread.sleep(2000);
            }
            kubectl("-n", "monitoring", "wait", "--for=condition=Ready", "pod/" + pod, "--timeout=180s");
        }
        for (int i = 0; i < 50 && !alertmanagerAlerts.contains("ExampleAppErrors"); i++) {   // up to 2.5 minutes
            Thread.sleep(3000);
            alertmanagerAlerts = get("alertmanager-lab-0", "alertmanager", "http://localhost:9093/api/v2/alerts");
        }
    }

    @AfterAll
    static void shutdown() {
        k3s.stop();
    }

    @Test
    void ruleIsLoaded() throws Exception {
        String rules = prometheus("/api/v1/rules");
        String groups = rules.replaceAll(".*?\"name\":\"([^\"]*)\",\"file\"", "$1 ").replaceAll("\\{.*", "").strip();
        assertTrue(rules.contains("ExampleAppErrors"), "Prometheus did not load the rule. Rule groups it has: "
                + (groups.isEmpty() ? "(none)" : groups));
    }

    @Test
    void alertReachesAlertmanager() throws Exception {
        assertTrue(alertmanagerAlerts.contains("ExampleAppErrors"),
                "no ExampleAppErrors alert in Alertmanager.\nPrometheus alerts: " + prometheus("/api/v1/alerts")
                        + "\nThe series of http_requests_total in shop: "
                        + prometheus("/api/v1/series?match[]=" + URLEncoder.encode("http_requests_total{namespace=\"shop\"}",
                                StandardCharsets.UTF_8)));
        assertTrue(alertmanagerAlerts.contains("\"severity\":\"warning\""), alertmanagerAlerts);
    }

    private static String prometheus(String path) throws Exception {
        return get("prometheus-lab-0", "prometheus", "http://localhost:9090" + path);
    }

    private static String get(String pod, String container, String url) throws Exception {
        return kubectl("-n", "monitoring", "exec", pod, "-c", container, "--", "wget", "-qO-", url);
    }

    private static String kubectl(String... args) throws Exception {
        String[] command = Stream.concat(Stream.of("kubectl"), Arrays.stream(args)).toArray(String[]::new);
        ExecResult result = k3s.execInContainer(command);
        assertEquals(0, result.getExitCode(), String.join(" ", command) + " failed: " + result.getStderr());
        return result.getStdout().trim();
    }
}
