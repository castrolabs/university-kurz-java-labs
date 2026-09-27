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

class ApplyOrderTest {

    static final K3sContainer k3s = new K3sContainer(DockerImageName.parse("rancher/k3s:v1.36.4-k3s1"));
    static ExecResult first;
    static String availableRightAfter;

    @BeforeAll
    static void deploy() throws Exception {
        k3s.start();
        k3s.copyFileToContainer(MountableFile.forClasspathResource("deploy"), "/lab/deploy");
        k3s.copyFileToContainer(MountableFile.forClasspathResource("deploy.sh"), "/lab/deploy.sh");
        first = k3s.execInContainer("sh", "/lab/deploy.sh");
        availableRightAfter = k3s.execInContainer("kubectl", "-n", "shop", "get", "deploy", "web", "-o",
                "jsonpath={.status.availableReplicas}").getStdout().trim();
    }

    @AfterAll
    static void shutdown() {
        k3s.stop();
    }

    @Test
    void firstRunSucceedsWithoutErrors() {
        assertEquals(0, first.getExitCode(), "deploy.sh failed on an empty cluster:\n" + first.getStderr());
        assertFalse(first.getStderr().toLowerCase().contains("error"),
                "deploy.sh printed errors (retrying until it works is not the fix):\n" + first.getStderr());
    }

    @Test
    void everythingIsCreated() throws Exception {
        assertEquals("hello", kubectl("-n", "shop", "get", "configmap", "web-config", "-o", "jsonpath={.data.GREETING}"));
        assertEquals("3", kubectl("-n", "shop", "get", "widget", "default-widget", "-o", "jsonpath={.spec.size}"));
    }

    @Test
    void returnsOnlyWhenWebIsAvailable() {
        assertEquals("1", availableRightAfter, "deploy.sh returned before deploy/web was available");
    }

    @Test
    void secondRunSucceedsToo() throws Exception {
        ExecResult again = k3s.execInContainer("sh", "/lab/deploy.sh");
        assertEquals(0, again.getExitCode(), "deploy.sh failed on its second run:\n" + again.getStderr());
    }

    private static String kubectl(String... args) throws Exception {
        String[] command = Stream.concat(Stream.of("kubectl"), Arrays.stream(args)).toArray(String[]::new);
        ExecResult result = k3s.execInContainer(command);
        assertEquals(0, result.getExitCode(), String.join(" ", command) + " failed: " + result.getStderr());
        return result.getStdout().trim();
    }
}
