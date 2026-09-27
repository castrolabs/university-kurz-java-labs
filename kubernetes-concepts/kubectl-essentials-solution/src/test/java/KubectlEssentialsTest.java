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

class KubectlEssentialsTest {

    static final K3sContainer k3s = new K3sContainer(DockerImageName.parse("rancher/k3s:v1.36.4-k3s1"));
    static ExecResult ops;
    static String stableUid;

    @BeforeAll
    static void run() throws Exception {
        k3s.start();
        k3s.copyFileToContainer(MountableFile.forClasspathResource("k8s/shop.yaml"), "/lab/k8s/shop.yaml");
        k3s.copyFileToContainer(MountableFile.forClasspathResource("k8s/api.yaml"), "/lab/k8s/api.yaml");
        k3s.copyFileToContainer(MountableFile.forClasspathResource("ops.sh"), "/lab/ops.sh");
        kubectl("apply", "-f", "/lab/k8s/shop.yaml");
        kubectl("-n", "shop", "wait", "--for=condition=Ready", "pod", "--all", "--timeout=90s");
        stableUid = kubectl("-n", "shop", "get", "pod", "web-stable", "-o", "jsonpath={.metadata.uid}");
        ops = k3s.execInContainer("sh", "/lab/ops.sh");
    }

    @AfterAll
    static void shutdown() {
        k3s.stop();
    }

    @Test
    void scriptSucceeds() {
        assertEquals(0, ops.getExitCode(), "ops.sh failed:\n" + ops.getStderr());
    }

    @Test
    void apiIsDeployedIntoShop() throws Exception {
        assertEquals("", kubectl("-n", "default", "get", "deploy", "-o", "name"), "something was deployed into default");
        assertEquals("deployment.apps/api", kubectl("-n", "shop", "get", "deploy", "api", "-o", "name"));
    }

    @Test
    void onlyTheCanaryPodsAreGone() throws Exception {
        assertEquals("", kubectl("-n", "shop", "get", "pods", "-l", "track=canary", "-o", "name"), "canary Pods remain");
        ExecResult stable = k3s.execInContainer("kubectl", "-n", "shop", "get", "pod", "web-stable", "-o",
                "jsonpath={.metadata.uid}");
        assertTrue(stable.getExitCode() == 0 && stable.getStdout().trim().equals(stableUid),
                "web-stable was deleted too (it is a bare Pod: nothing recreates it)");
    }

    @Test
    void reportListsEveryDeploymentAndItsImage() {
        List<String> lines = ops.getStdout().strip().lines().map(String::strip)
                .filter(l -> !l.isEmpty() && !l.contains("created") && !l.contains("configured")
                        && !l.contains("unchanged") && !l.contains("deleted"))
                .toList();
        assertEquals(List.of("api busybox:1.37", "cache busybox:1.36"), lines, "report output:\n" + ops.getStdout());
    }

    private static String kubectl(String... args) throws Exception {
        String[] command = Stream.concat(Stream.of("kubectl"), Arrays.stream(args)).toArray(String[]::new);
        ExecResult result = k3s.execInContainer(command);
        assertEquals(0, result.getExitCode(), String.join(" ", command) + " failed: " + result.getStderr());
        return result.getStdout().trim();
    }
}
