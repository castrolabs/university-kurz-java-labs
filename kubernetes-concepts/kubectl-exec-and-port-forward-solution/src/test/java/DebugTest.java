import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.k3s.K3sContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

class DebugTest {

    static final K3sContainer k3s = new K3sContainer(DockerImageName.parse("rancher/k3s:v1.36.4-k3s1"));
    static ExecResult script;
    static List<String> output;
    static String expectedWorkers;
    static String expectedServer;

    @BeforeAll
    static void run() throws Exception {
        k3s.start();
        k3s.copyFileToContainer(MountableFile.forClasspathResource("k8s/web.yaml"), "/lab/k8s/web.yaml");
        k3s.copyFileToContainer(MountableFile.forClasspathResource("inspect.sh"), "/lab/inspect.sh");
        kubectl("apply", "-f", "/lab/k8s/web.yaml");
        kubectl("rollout", "status", "deploy/web", "--timeout=120s");
        kubectl("wait", "--for=condition=Ready", "pod/client", "--timeout=60s");

        String pod = kubectl("get", "pod", "-l", "app=web", "-o", "jsonpath={.items[0].metadata.name}");
        String podIp = kubectl("get", "pod", pod, "-o", "jsonpath={.status.podIP}");
        expectedServer = kubectl("exec", "client", "--", "sh", "-c",
                "wget -S -qO /dev/null http://" + podIp + ":8080 2>&1 | grep -i -m1 'server:'").strip();
        expectedWorkers = k3s.execInContainer("sh", "-c", "kubectl debug pod/" + pod
                + " -q --image=busybox:1.37 --target=nginx -c expected -- grep -m1 worker_processes"
                + " /proc/1/root/etc/nginx/nginx.conf >/dev/null; sleep 8; kubectl logs " + pod + " -c expected")
                .getStdout().strip();

        script = k3s.execInContainer("sh", "/lab/inspect.sh");
        output = script.getStdout().strip().lines().map(String::strip).toList();
    }

    @AfterAll
    static void shutdown() {
        k3s.stop();
    }

    @Test
    void scriptSucceeds() {
        assertEquals(0, script.getExitCode(), "inspect.sh failed:\n" + script.getStdout() + script.getStderr());
    }

    @Test
    void printsTheConfigOfTheRunningContainer() {
        assertTrue(expectedWorkers.startsWith("worker_processes"), "test setup could not read the config: " + expectedWorkers);
        assertTrue(!output.isEmpty() && output.get(0).equals(expectedWorkers),
                "line 1 should be \"" + expectedWorkers + "\", output was:\n" + script.getStdout());
    }

    @Test
    void printsTheServerHeaderThroughThePortForward() throws Exception {
        assertTrue(output.size() > 1 && output.get(1).equalsIgnoreCase(expectedServer),
                "line 2 should be \"" + expectedServer + "\", output was:\n" + script.getStdout());
        assertEquals("", k3s.execInContainer("sh", "-c", "pgrep -f 'kubectl port-forward' || true").getStdout().strip(),
                "a kubectl port-forward process is still running after the script");
    }

    private static String kubectl(String... args) throws Exception {
        String[] command = Stream.concat(Stream.of("kubectl"), Arrays.stream(args)).toArray(String[]::new);
        ExecResult result = k3s.execInContainer(command);
        assertEquals(0, result.getExitCode(), String.join(" ", command) + " failed: " + result.getStderr());
        return result.getStdout().trim();
    }
}
