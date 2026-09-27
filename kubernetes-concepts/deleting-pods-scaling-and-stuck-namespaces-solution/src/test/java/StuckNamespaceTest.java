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

class StuckNamespaceTest {

    static final K3sContainer k3s = new K3sContainer(DockerImageName.parse("rancher/k3s:v1.36.4-k3s1"));
    static ExecResult ops;

    @BeforeAll
    static void run() throws Exception {
        k3s.start();
        k3s.copyFileToContainer(MountableFile.forClasspathResource("k8s/cluster.yaml"), "/lab/k8s/cluster.yaml");
        k3s.copyFileToContainer(MountableFile.forClasspathResource("ops.sh"), "/lab/ops.sh");
        kubectl("apply", "-f", "/lab/k8s/cluster.yaml");
        kubectl("-n", "shop", "rollout", "status", "deploy/web", "--timeout=90s");
        kubectl("delete", "namespace", "old-team", "--wait=false");
        kubectl("wait", "namespace/old-team", "--for=condition=NamespaceFinalizersRemaining", "--timeout=60s");
        ops = k3s.execInContainer("sh", "/lab/ops.sh");
    }

    @AfterAll
    static void shutdown() {
        k3s.stop();
    }

    @Test
    void scriptSucceeds() {
        assertEquals(0, ops.getExitCode(), "ops.sh failed:\n" + ops.getStdout() + ops.getStderr());
    }

    @Test
    void webIsStoppedButKept() throws Exception {
        assertEquals("0", kubectl("-n", "shop", "get", "deploy", "web", "-o", "jsonpath={.spec.replicas}"),
                "deploy/web still wants Pods, so deleted Pods come back");
        ExecResult gone = k3s.execInContainer("kubectl", "-n", "shop", "wait", "--for=delete", "pod", "-l", "app=web",
                "--timeout=30s");
        assertEquals(0, gone.getExitCode(), "web Pods are still running: " + gone.getStderr());
    }

    @Test
    void oldTeamIsGoneForGood() throws Exception {
        ExecResult gone = k3s.execInContainer("kubectl", "wait", "--for=delete", "namespace/old-team", "--timeout=60s");
        assertEquals(0, gone.getExitCode(), "old-team is still there: "
                + k3s.execInContainer("kubectl", "get", "namespace", "old-team", "-o",
                        "jsonpath={.status.conditions[?(@.status==\"True\")].message}").getStdout());
        // Recreate it, as a new team would. Nothing from the old one may come back.
        kubectl("create", "namespace", "old-team");
        String left = kubectl("-n", "old-team", "get", "configmaps", "-o", "name");
        assertTrue(!left.contains("leftover"), "the old ConfigMap came back in the new namespace: " + left);
    }

    private static String kubectl(String... args) throws Exception {
        String[] command = Stream.concat(Stream.of("kubectl"), Arrays.stream(args)).toArray(String[]::new);
        ExecResult result = k3s.execInContainer(command);
        assertEquals(0, result.getExitCode(), String.join(" ", command) + " failed: " + result.getStderr());
        return result.getStdout().trim();
    }
}
