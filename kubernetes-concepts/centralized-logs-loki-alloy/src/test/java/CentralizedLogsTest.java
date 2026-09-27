import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

class CentralizedLogsTest {

    static final K3sContainer k3s = new K3sContainer(DockerImageName.parse("rancher/k3s:v1.36.4-k3s1"));
    static String shopLogs = "";

    @BeforeAll
    static void deploy() throws Exception {
        k3s.start();
        for (String f : new String[] {"k8s/logging.yaml", "k8s/alloy-config.yaml"}) {
            k3s.copyFileToContainer(MountableFile.forClasspathResource(f), "/lab/" + f);
        }
        kubectl("apply", "-f", "/lab/k8s/logging.yaml");   // creates the namespace
        kubectl("apply", "-f", "/lab/k8s/alloy-config.yaml");
        kubectl("-n", "logging", "rollout", "status", "deploy/loki", "--timeout=180s");
        kubectl("-n", "logging", "rollout", "status", "deploy/alloy", "--timeout=180s");
        kubectl("-n", "logging", "wait", "--for=condition=Ready", "pod/client", "--timeout=60s");
        for (int i = 0; i < 30 && !shopLogs.contains("placed"); i++) {
            Thread.sleep(3000);
            shopLogs = loki("{namespace=\"shop\"}");
        }
    }

    @AfterAll
    static void shutdown() {
        k3s.stop();
    }

    @Test
    void shopLogsAreSearchableByNamespace() throws Exception {
        assertTrue(shopLogs.contains("order") && shopLogs.contains("placed"),
                "no logs for {namespace=\"shop\"} in Loki.\nLabels Loki knows: " + lokiApi("/loki/api/v1/labels")
                        + "\nAlloy's last errors:\n" + k3s.execInContainer("sh", "-c",
                                "kubectl -n logging logs deploy/alloy | grep -i -E 'error|warn' | tail -3").getStdout());
    }

    @Test
    void passwordsNeverReachLoki() throws Exception {
        assertTrue(shopLogs.contains("login user=alice"), "no login lines in Loki yet");
        assertFalse(loki("{namespace=~\".+\"} |= \"hunter2\"").contains("hunter2"), "a password is stored in Loki");
        assertTrue(shopLogs.contains("password=****"), "passwords should be replaced with ****");
    }

    private static String loki(String logql) throws Exception {
        return lokiApi("/loki/api/v1/query_range?limit=50&query=" + URLEncoder.encode(logql, StandardCharsets.UTF_8));
    }

    private static String lokiApi(String path) throws Exception {
        return kubectl("-n", "logging", "exec", "client", "--", "wget", "-qO-", "http://loki:3100" + path);
    }

    private static String kubectl(String... args) throws Exception {
        String[] command = Stream.concat(Stream.of("kubectl"), Arrays.stream(args)).toArray(String[]::new);
        ExecResult result = k3s.execInContainer(command);
        assertEquals(0, result.getExitCode(), String.join(" ", command) + " failed: " + result.getStderr());
        return result.getStdout().trim();
    }
}
