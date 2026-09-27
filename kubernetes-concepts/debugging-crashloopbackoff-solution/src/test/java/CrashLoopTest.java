import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Arrays;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.k3s.K3sContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

class CrashLoopTest {

    static final K3sContainer k3s = new K3sContainer(DockerImageName.parse("rancher/k3s:v1.36.4-k3s1"));

    @BeforeAll
    static void deploy() throws Exception {
        k3s.start();
        k3s.copyFileToContainer(MountableFile.forClasspathResource("k8s/apps.yaml"), "/lab/k8s/apps.yaml");
        kubectl("apply", "-f", "/lab/k8s/apps.yaml");
        for (String app : new String[] {"api", "worker", "cache"}) {
            k3s.execInContainer("kubectl", "rollout", "status", "deploy/" + app, "--timeout=60s");
        }
        Thread.sleep(20000);   // long enough for a crashing container to crash at least once
    }

    @AfterAll
    static void shutdown() {
        k3s.stop();
    }

    @Test
    void apiRuns() throws Exception {
        assertStable("api");
        assertEquals("connected to postgres://db.shared:5432/shop", kubectl("logs", "deploy/api"));
    }

    @Test
    void workerRuns() throws Exception {
        assertStable("worker");
        assertEquals("working", kubectl("logs", "deploy/worker", "--tail=1"));
    }

    @Test
    void cacheRunsWithinALimit() throws Exception {
        assertStable("cache");
        assertFalse(kubectl("get", "deploy", "cache", "-o",
                "jsonpath={.spec.template.spec.containers[0].resources.limits.memory}").isEmpty(),
                "cache must keep a memory limit");
    }

    private static void assertStable(String app) throws Exception {
        String pod = "{.items[0].status.containerStatuses[0]";
        String restarts = kubectl("get", "pods", "-l", "app=" + app, "-o", "jsonpath=" + pod + ".restartCount}");
        String ready = kubectl("get", "pods", "-l", "app=" + app, "-o", "jsonpath=" + pod + ".ready}");
        if (!"0".equals(restarts) || !"true".equals(ready)) {
            String state = kubectl("get", "pods", "-l", "app=" + app, "-o", "jsonpath=state: " + pod
                    + ".state}{\"\\n\"}lastState: " + pod + ".lastState}");
            // A container that already exited still has its own logs; one waiting to restart does not.
            String logs = k3s.execInContainer("kubectl", "logs", "deploy/" + app, "--tail=3").getStdout().trim();
            if (logs.isEmpty() || logs.startsWith("unable to retrieve")) {
                logs = k3s.execInContainer("kubectl", "logs", "deploy/" + app, "--previous", "--tail=3").getStdout().trim();
            }
            if (logs.startsWith("unable to retrieve")) {
                logs = "";
            }
            assertEquals("0 restarts, ready", restarts + " restarts, " + ("true".equals(ready) ? "ready" : "not ready"),
                    app + " is not running.\n" + state + "\nlogs: " + (logs.isEmpty() ? "(none)" : logs) + "\n");
        }
    }

    private static String kubectl(String... args) throws Exception {
        String[] command = Stream.concat(Stream.of("kubectl"), Arrays.stream(args)).toArray(String[]::new);
        ExecResult result = k3s.execInContainer(command);
        assertEquals(0, result.getExitCode(), String.join(" ", command) + " failed: " + result.getStderr());
        return result.getStdout().trim();
    }
}
