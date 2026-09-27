import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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

class ProbesTest {

    static final K3sContainer k3s = new K3sContainer(DockerImageName.parse("rancher/k3s:v1.36.4-k3s1"));
    static boolean slowReady;
    static String slowStatus;
    static String readyzAfterBrokenRollout;
    static int rolloutStatusExitCode;

    @BeforeAll
    static void deploy() throws Exception {
        k3s.start();
        k3s.copyFileToContainer(MountableFile.forClasspathResource("k8s/slow.yaml"), "/lab/k8s/slow.yaml");
        k3s.copyFileToContainer(MountableFile.forClasspathResource("k8s/web.yaml"), "/lab/k8s/web.yaml");
        kubectl("create", "namespace", "example");
        kubectl("-n", "example", "run", "client", "--image=busybox:1.37", "--", "sleep", "3600");
        kubectl("apply", "-f", "/lab/k8s/slow.yaml");
        kubectl("apply", "-f", "/lab/k8s/web.yaml");

        // Scenario 1: the slow starter.
        // No readiness probe on this Pod, so "Ready" means nothing: wait until the app really answers.
        long start = System.currentTimeMillis();
        while (!slowReady && System.currentTimeMillis() - start < 120_000) {
            slowReady = k3s.execInContainer("kubectl", "-n", "example", "exec", "slow", "--",
                    "wget", "-qO-", "-T", "2", "http://127.0.0.1:8080/healthz").getStdout().trim().equals("ok");
            Thread.sleep(3000);
        }
        if (slowReady) {
            Thread.sleep(35_000); // long enough for a too-eager liveness probe to strike
        }
        slowReady = slowReady && kubectl("-n", "example", "get", "pod", "slow", "-o",
                "jsonpath={.status.containerStatuses[0].restartCount}").equals("0");
        slowStatus = kubectl("-n", "example", "get", "pod", "slow", "-o",
                "jsonpath=restarts={.status.containerStatuses[0].restartCount} {.status.containerStatuses[0].state}");

        // Scenario 2: roll out a broken version of web.
        kubectl("-n", "example", "rollout", "status", "deploy/web", "--timeout=120s");
        kubectl("-n", "example", "wait", "--for=condition=Ready", "pod/client", "--timeout=60s");
        kubectl("-n", "example", "set", "env", "deploy/web", "VERSION=2");
        Thread.sleep(45_000);
        readyzAfterBrokenRollout = k3s.execInContainer("kubectl", "-n", "example", "exec", "client", "--",
                "wget", "-qO-", "-T", "3", "http://web/readyz").getStdout().trim();
        rolloutStatusExitCode = k3s.execInContainer("kubectl", "-n", "example", "rollout", "status", "deploy/web",
                "--timeout=5s").getExitCode();
    }

    @AfterAll
    static void shutdown() {
        k3s.stop();
    }

    @Test
    void slowAppStartsWithoutBeingKilled() {
        assertTrue(slowReady, "pod/slow did not start cleanly: " + slowStatus);
    }

    @Test
    void livenessProbeIsKeptTight() throws Exception {
        String liveness = kubectl("-n", "example", "get", "pod", "slow", "-o",
                "jsonpath={.spec.containers[0].livenessProbe.periodSeconds}x{.spec.containers[0].livenessProbe.failureThreshold}");
        assertFalse(liveness.equals("x"), "keep the liveness probe: it is what restarts a hung app");
        String[] parts = liveness.split("x");
        assertTrue(Integer.parseInt(parts[0]) * Integer.parseInt(parts[1]) <= 30,
                "the liveness probe should still react within about 30 s, got " + liveness);
    }

    @Test
    void startupProbeGivesAtLeastSixtySeconds() throws Exception {
        String startup = kubectl("-n", "example", "get", "pod", "slow", "-o",
                "jsonpath={.spec.containers[0].startupProbe.periodSeconds}x{.spec.containers[0].startupProbe.failureThreshold}");
        assertNotEquals("x", startup, "there is no startupProbe");
        String[] parts = startup.split("x");
        assertTrue(Integer.parseInt(parts[0]) * Integer.parseInt(parts[1]) >= 60,
                "startup budget (periodSeconds x failureThreshold) is below 60 s: " + startup);
    }

    @Test
    void brokenVersionDoesNotReplaceHealthyPods() {
        assertEquals("ok", readyzAfterBrokenRollout,
                "after rolling out VERSION=2, the Service no longer has a Pod that serves /readyz");
    }

    @Test
    void stuckRolloutIsVisible() {
        assertNotEquals(0, rolloutStatusExitCode,
                "kubectl rollout status reports success although VERSION=2 is broken");
    }

    private static String kubectl(String... args) throws Exception {
        String[] command = Stream.concat(Stream.of("kubectl"), Arrays.stream(args)).toArray(String[]::new);
        ExecResult result = k3s.execInContainer(command);
        assertEquals(0, result.getExitCode(), String.join(" ", command) + " failed: " + result.getStderr());
        return result.getStdout().trim();
    }
}
