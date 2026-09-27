import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

class RuntimeMemoryTest {

    static final K3sContainer k3s = new K3sContainer(DockerImageName.parse("rancher/k3s:v1.36.4-k3s1"));
    static String logs = "";

    @BeforeAll
    static void deploy() throws Exception {
        k3s.start();
        k3s.copyFileToContainer(MountableFile.forClasspathResource("CacheWarmup.class"), "/lab/app/CacheWarmup.class");
        k3s.copyFileToContainer(MountableFile.forClasspathResource("k8s/cache.yaml"), "/lab/k8s/cache.yaml");
        kubectl("apply", "-f", "/lab/k8s/cache.yaml");
        for (int i = 0; i < 60 && !logs.contains("cache warm") && restarts() == 0; i++) {   // the image pull takes a while
            Thread.sleep(3000);
            logs = k3s.execInContainer("kubectl", "logs", "deploy/cache").getStdout();
        }
        Thread.sleep(5000);
        if (restarts() > 0) {
            logs = k3s.execInContainer("kubectl", "logs", "deploy/cache", "--previous").getStdout();
        }
    }

    private static int restarts() throws Exception {
        String count = k3s.execInContainer("kubectl", "get", "pods", "-l", "app=cache", "-o",
                "jsonpath={.items[0].status.containerStatuses[0].restartCount}").getStdout().trim();
        return count.isEmpty() ? 0 : Integer.parseInt(count);
    }

    @AfterAll
    static void shutdown() {
        k3s.stop();
    }

    @Test
    void limitIsUnchanged() throws Exception {
        assertEquals("512Mi", kubectl("get", "deploy", "cache", "-o",
                "jsonpath={.spec.template.spec.containers[0].resources.limits.memory}"));
    }

    @Test
    void cacheWarmsUpWithoutRestarts() throws Exception {
        assertTrue(logs.contains("cache warm") && restarts() == 0, "the app did not start cleanly. Its output:\n" + logs);
    }

    @Test
    void heapIsSizedRelativeToTheLimit() throws Exception {
        Matcher heap = Pattern.compile("max heap MiB: (\\d+)").matcher(logs);
        assertTrue(heap.find(), "no output from the app:\n" + logs);
        int mib = Integer.parseInt(heap.group(1));
        assertTrue(mib >= 0.6 * 512 && mib <= 0.8 * 512, "max heap is " + mib + " MiB, expected 60 to 80% of 512 MiB");
        String options = kubectl("get", "deploy", "cache", "-o", "jsonpath={.spec.template.spec.containers[0].env}");
        assertTrue(options.contains("MaxRAMPercentage") && !options.contains("-Xmx"),
                "size the heap as a percentage of the container limit, not with -Xmx: " + options);
    }

    @Test
    void usesG1() {
        assertTrue(logs.contains("G1 Young Generation"), "the JVM does not use G1:\n" + logs);
    }

    @Test
    void exitsOnOutOfMemory() {
        assertTrue(logs.contains("exit on OOM: true"), "ExitOnOutOfMemoryError is off:\n" + logs);
    }

    private static String kubectl(String... args) throws Exception {
        String[] command = Stream.concat(Stream.of("kubectl"), Arrays.stream(args)).toArray(String[]::new);
        ExecResult result = k3s.execInContainer(command);
        assertEquals(0, result.getExitCode(), String.join(" ", command) + " failed: " + result.getStderr());
        return result.getStdout().trim();
    }
}
