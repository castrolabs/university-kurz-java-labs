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
class MemoryBudgetTest {

    static final K3sContainer k3s = new K3sContainer(DockerImageName.parse("rancher/k3s:v1.36.4-k3s1"))
            .withCopyFileToContainer(MountableFile.forClasspathResource("config.yaml"), "/etc/rancher/k3s/config.yaml");
    static int answer = -1;
    static ExecResult budget;

    @BeforeAll
    static void run() throws Exception {
        k3s.start();
        k3s.copyFileToContainer(MountableFile.forClasspathResource("k8s/worker.yaml"), "/lab/k8s/worker.yaml");
        k3s.copyFileToContainer(MountableFile.forClasspathResource("budget.sh"), "/lab/budget.sh");
        kubectl("apply", "-f", "/lab/k8s/worker.yaml");
        kubectl("rollout", "status", "deploy/db", "--timeout=120s");
        for (String d : new String[] {"coredns", "metrics-server", "local-path-provisioner"}) {
            kubectl("-n", "kube-system", "rollout", "status", "deploy/" + d, "--timeout=120s");
        }
        budget = k3s.execInContainer("sh", "/lab/budget.sh");
        try {
            answer = Integer.parseInt(budget.getStdout().trim());
        } catch (NumberFormatException e) {
            answer = -1;
        }
    }

    @AfterAll
    static void shutdown() {
        k3s.stop();
    }

    @Test
    @Order(1)
    void memoryIsReservedForTheSystem() throws Exception {
        long capacity = ki(kubectl("get", "nodes", "-o", "jsonpath={.items[0].status.capacity.memory}"));
        long allocatable = ki(kubectl("get", "nodes", "-o", "jsonpath={.items[0].status.allocatable.memory}"));
        // 512Mi system + 256Mi kube + a 100Mi hard eviction threshold for memory
        assertEquals((512 + 256 + 100) * 1024L, capacity - allocatable,
                "capacity - allocatable is " + (capacity - allocatable) / 1024 + "Mi");
    }

    @Test
    @Order(2)
    void budgetMatchesTheScheduler() throws Exception {
        assertTrue(answer >= 0, "budget.sh printed \"" + budget.getStdout().trim() + "\" " + budget.getStderr());
        kubectl("scale", "deploy/worker", "--replicas=" + answer);
        String pending = "";
        for (int i = 0; i < 20; i++) {
            Thread.sleep(2000);
            pending = kubectl("get", "pods", "-l", "app=worker", "--field-selector=status.phase=Pending", "-o", "name");
            if (pending.isEmpty() && scheduled() == answer) {
                break;
            }
        }
        assertEquals(answer, scheduled(), "not all " + answer + " workers were scheduled: the budget is too high");

        kubectl("scale", "deploy/worker", "--replicas=" + (answer + 1));
        Thread.sleep(5000);
        assertEquals(answer, scheduled(), "one more worker still fit: the budget is too low");
    }

    private static long scheduled() throws Exception {
        return kubectl("get", "pods", "-l", "app=worker", "-o", "jsonpath={range .items[?(@.spec.nodeName)]}x{\"\\n\"}{end}")
                .lines().filter(l -> !l.isBlank()).count();
    }

    private static long ki(String quantity) {
        return Long.parseLong(quantity.replace("Ki", ""));
    }

    private static String kubectl(String... args) throws Exception {
        String[] command = Stream.concat(Stream.of("kubectl"), Arrays.stream(args)).toArray(String[]::new);
        ExecResult result = k3s.execInContainer(command);
        assertEquals(0, result.getExitCode(), String.join(" ", command) + " failed: " + result.getStderr());
        return result.getStdout().trim();
    }
}
