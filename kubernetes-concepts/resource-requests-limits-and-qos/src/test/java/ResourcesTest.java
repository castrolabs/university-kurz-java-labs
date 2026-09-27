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

class ResourcesTest {

    static final K3sContainer k3s = new K3sContainer(DockerImageName.parse("rancher/k3s:v1.36.4-k3s1"));

    @BeforeAll
    static void deploy() throws Exception {
        k3s.start();
        k3s.copyFileToContainer(MountableFile.forClasspathResource("k8s/pods.yaml"), "/lab/k8s/pods.yaml");
        k3s.copyFileToContainer(MountableFile.forClasspathResource("k8s/naive-worker.yaml"), "/lab/k8s/naive-worker.yaml");
        kubectl("create", "namespace", "example");
        kubectl("apply", "-f", "/lab/k8s/naive-worker.yaml");
        kubectl("apply", "-f", "/lab/k8s/pods.yaml");
        // Give the Pods time to start, crash or get stuck.
        k3s.execInContainer("kubectl", "-n", "example", "wait", "--for=condition=Ready", "pod/db", "pod/batch",
                "--timeout=60s");
        Thread.sleep(30_000);
    }

    @AfterAll
    static void shutdown() {
        k3s.stop();
    }

    @Test
    void naiveWorkerIsOomKilled() throws Exception {
        String last = field("naive-worker", "{.status.containerStatuses[0].lastState.terminated.reason}"
                + "/{.status.containerStatuses[0].lastState.terminated.exitCode}");
        assertEquals("OOMKilled/137", last, "exit code 137 = 128 + SIGKILL, sent by the kernel's OOM killer");
    }

    @Test
    void workerLoadsItsCacheAndStaysUp() throws Exception {
        String state = field("worker", "restarts={.status.containerStatuses[0].restartCount} "
                + "last={.status.containerStatuses[0].lastState.terminated.reason}");
        assertEquals("restarts=0 last=", state, "pod/worker is still being killed");
        assertTrue(kubectl("-n", "example", "logs", "worker").contains("loaded"), "the cache never finished loading");
    }

    @Test
    void workerKeepsAMemoryLimit() throws Exception {
        assertTrue(!field("worker", "{.spec.containers[0].resources.limits.memory}").isEmpty(),
                "keep a memory limit: without one a leak can take the whole node down");
    }

    @Test
    void dbIsGuaranteed() throws Exception {
        assertEquals("Guaranteed", field("db", "{.status.qosClass}"));
    }

    @Test
    void batchIsScheduledAndRunning() throws Exception {
        assertEquals("Running", field("batch", "{.status.phase}"), "pod/batch: "
                + kubectl("-n", "example", "get", "events", "--field-selector", "involvedObject.name=batch",
                        "-o", "jsonpath={.items[*].message}"));
    }

    private static String field(String pod, String jsonpath) throws Exception {
        return kubectl("-n", "example", "get", "pod", pod, "-o", "jsonpath=" + jsonpath);
    }

    private static String kubectl(String... args) throws Exception {
        String[] command = Stream.concat(Stream.of("kubectl"), Arrays.stream(args)).toArray(String[]::new);
        ExecResult result = k3s.execInContainer(command);
        assertEquals(0, result.getExitCode(), String.join(" ", command) + " failed: " + result.getStderr());
        return result.getStdout().trim();
    }
}
