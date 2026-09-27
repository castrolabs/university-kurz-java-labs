import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.k3s.K3sContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

class DeploymentStrategiesTest {

    static final K3sContainer k3s = new K3sContainer(DockerImageName.parse("rancher/k3s:v1.36.4-k3s1"));
    static String availableDuringBrokenRollout;
    static String serviceDuringBrokenRollout;
    static int maxRunningSingletons;

    @BeforeAll
    static void deploy() throws Exception {
        k3s.start();
        k3s.copyFileToContainer(MountableFile.forClasspathResource("k8s/web.yaml"), "/lab/k8s/web.yaml");
        k3s.copyFileToContainer(MountableFile.forClasspathResource("k8s/singleton.yaml"), "/lab/k8s/singleton.yaml");
        kubectl("create", "namespace", "example");
        kubectl("-n", "example", "run", "client", "--image=busybox:1.37", "--", "sleep", "3600");
        kubectl("apply", "-f", "/lab/k8s/web.yaml");
        kubectl("apply", "-f", "/lab/k8s/singleton.yaml");
        kubectl("-n", "example", "rollout", "status", "deploy/web", "--timeout=120s");
        kubectl("-n", "example", "rollout", "status", "deploy/singleton", "--timeout=120s");
        kubectl("-n", "example", "wait", "--for=condition=Ready", "pod/client", "--timeout=60s");

        // Scenario 1: roll out an image tag that does not exist.
        kubectl("-n", "example", "set", "image", "deploy/web", "app=busybox:0.0.0-does-not-exist");
        Thread.sleep(30_000);
        availableDuringBrokenRollout = kubectl("-n", "example", "get", "deploy", "web", "-o",
                "jsonpath={.status.availableReplicas}");
        serviceDuringBrokenRollout = k3s.execInContainer("kubectl", "-n", "example", "exec", "client", "--",
                "wget", "-qO-", "-T", "3", "http://web/").getStdout().trim();

        // Scenario 2: restart the singleton and count how many of its Pods run at the same time.
        kubectl("-n", "example", "rollout", "restart", "deploy/singleton");
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < 30_000) {
            String phases = kubectl("-n", "example", "get", "pods", "-l", "app=singleton", "-o",
                    "jsonpath={range .items[*]}{.status.phase}{\"\\n\"}{end}");
            int running = (int) phases.lines().filter("Running"::equals).count();
            maxRunningSingletons = Math.max(maxRunningSingletons, running);
            Thread.sleep(500);
        }
    }

    @AfterAll
    static void shutdown() {
        k3s.stop();
    }

    @Test
    void brokenImageDoesNotReduceCapacity() {
        assertEquals("2", availableDuringBrokenRollout,
                "a rollout of a broken image took available replicas below 2");
    }

    @Test
    void serviceKeepsAnsweringDuringBrokenRollout() {
        assertEquals("ok", serviceDuringBrokenRollout);
    }

    @Test
    void singletonNeverRunsTwice() {
        assertTrue(maxRunningSingletons <= 1,
                maxRunningSingletons + " singleton Pods were running at the same time during the rollout");
    }

    @Test
    void singletonCameBack() throws Exception {
        kubectl("-n", "example", "rollout", "status", "deploy/singleton", "--timeout=60s");
        assertEquals("1", kubectl("-n", "example", "get", "deploy", "singleton", "-o",
                "jsonpath={.status.availableReplicas}"));
    }

    private static String kubectl(String... args) throws Exception {
        String[] command = Stream.concat(Stream.of("kubectl"), Arrays.stream(args)).toArray(String[]::new);
        ExecResult result = k3s.execInContainer(command);
        assertEquals(0, result.getExitCode(), String.join(" ", command) + " failed: " + result.getStderr());
        return result.getStdout().trim();
    }
}
