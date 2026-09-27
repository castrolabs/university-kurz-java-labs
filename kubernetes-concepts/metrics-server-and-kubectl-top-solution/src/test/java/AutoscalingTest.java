import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.k3s.K3sContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AutoscalingTest {

    // k3s ships metrics-server, which feeds both "kubectl top" and the HorizontalPodAutoscaler.
    static final K3sContainer k3s = new K3sContainer(DockerImageName.parse("rancher/k3s:v1.36.4-k3s1"));

    @BeforeAll
    static void deploy() throws Exception {
        k3s.start();
        k3s.copyFileToContainer(MountableFile.forClasspathResource("k8s/worker.yaml"), "/lab/k8s/worker.yaml");
        kubectl("apply", "-f", "/lab/k8s/worker.yaml");
        kubectl("rollout", "status", "deploy/worker", "--timeout=90s");
    }

    @AfterAll
    static void shutdown() {
        k3s.stop();
    }

    @Test
    @Order(1)
    void autoscalerReadsCpuUsage() throws Exception {
        String current = "";
        for (int i = 0; i < 40 && current.isEmpty(); i++) {   // metrics-server needs a minute or two after startup
            Thread.sleep(5000);
            current = kubectl("get", "hpa", "worker", "-o",
                    "jsonpath={.status.currentMetrics[0].resource.current.averageUtilization}");
        }
        assertTrue(!current.isEmpty(), "the HPA never got a CPU utilization. Its conditions:\n"
                + kubectl("get", "hpa", "worker", "-o",
                        "jsonpath={range .status.conditions[*]}{.type}={.status} {.reason}: {.message}{\"\\n\"}{end}"));
        assertTrue(kubectl("top", "pods", "-l", "app=worker").contains("worker"), "kubectl top shows no worker");
    }

    @Test
    @Order(2)
    void workerIsLimitedTo200Millicores() throws Exception {
        assertEquals("200m", kubectl("get", "deploy", "worker", "-o",
                "jsonpath={.spec.template.spec.containers[0].resources.limits.cpu}"));
    }

    @Test
    @Order(3)
    void autoscalerScalesUpAndApplyKeepsItsDecision() throws Exception {
        String replicas = "";
        for (int i = 0; i < 24 && !"3".equals(replicas); i++) {
            Thread.sleep(5000);
            replicas = kubectl("get", "deploy", "worker", "-o", "jsonpath={.spec.replicas}");
        }
        assertEquals("3", replicas, "the HPA did not scale the busy worker to its maximum");

        kubectl("apply", "-f", "/lab/k8s/worker.yaml");   // the next release
        assertEquals("3", kubectl("get", "deploy", "worker", "-o", "jsonpath={.spec.replicas}"),
                "kubectl apply reset the replicas the HPA had chosen");
    }

    private static String kubectl(String... args) throws Exception {
        String[] command = Stream.concat(Stream.of("kubectl"), Arrays.stream(args)).toArray(String[]::new);
        ExecResult result = k3s.execInContainer(command);
        assertEquals(0, result.getExitCode(), String.join(" ", command) + " failed: " + result.getStderr());
        return result.getStdout().trim();
    }
}
