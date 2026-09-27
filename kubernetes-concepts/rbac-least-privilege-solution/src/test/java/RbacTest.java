import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.k3s.K3sContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

class RbacTest {

    static final K3sContainer k3s = new K3sContainer(DockerImageName.parse("rancher/k3s:v1.36.4-k3s1"));
    static final String DEPLOYER = "system:serviceaccount:team-a:deployer";
    static final String VIEWER = "system:serviceaccount:team-a:viewer";

    @BeforeAll
    static void deploy() throws Exception {
        k3s.start();
        k3s.copyFileToContainer(MountableFile.forClasspathResource("k8s/rbac.yaml"), "/lab/k8s/rbac.yaml");
        kubectl("create", "namespace", "team-a");
        kubectl("create", "namespace", "team-b");
        kubectl("apply", "-f", "/lab/k8s/rbac.yaml");
    }

    @AfterAll
    static void shutdown() {
        k3s.stop();
    }

    @Test
    void deployerCanRollOutDeploymentsInTeamA() throws Exception {
        assertAllowed(DEPLOYER, "team-a", "patch", "deployments.apps");
        assertAllowed(DEPLOYER, "team-a", "update", "deployments.apps");
        assertAllowed(DEPLOYER, "team-a", "watch", "deployments.apps");
    }

    @Test
    void deployerCanDiagnosePods() throws Exception {
        assertAllowed(DEPLOYER, "team-a", "list", "pods");
        assertAllowed(DEPLOYER, "team-a", "get", "pods/log");
    }

    @Test
    void deployerCannotDoAnythingElse() throws Exception {
        assertDenied(DEPLOYER, "team-a", "delete", "deployments.apps");
        assertDenied(DEPLOYER, "team-a", "get", "secrets");
        assertDenied(DEPLOYER, "team-a", "create", "pods/exec");
        assertDenied(DEPLOYER, "team-a", "create", "rolebindings.rbac.authorization.k8s.io");
        assertDenied(DEPLOYER, "team-b", "patch", "deployments.apps");
        assertDenied(DEPLOYER, null, "list", "nodes");
    }

    @Test
    void viewerCanReadTeamA() throws Exception {
        assertAllowed(VIEWER, "team-a", "list", "deployments.apps");
        assertAllowed(VIEWER, "team-a", "get", "pods/log");
        assertAllowed(VIEWER, "team-a", "list", "configmaps");
    }

    @Test
    void viewerCannotChangeAnythingNorReadSecrets() throws Exception {
        assertDenied(VIEWER, "team-a", "patch", "deployments.apps");
        assertDenied(VIEWER, "team-a", "get", "secrets");
        assertDenied(VIEWER, "team-a", "create", "pods/exec");
        assertDenied(VIEWER, "team-b", "list", "pods");
    }

    private static void assertAllowed(String user, String namespace, String verb, String resource) throws Exception {
        assertEquals("yes", canI(user, namespace, verb, resource), user + " should be allowed to " + verb + " " + resource
                + (namespace == null ? "" : " in " + namespace));
    }

    private static void assertDenied(String user, String namespace, String verb, String resource) throws Exception {
        assertEquals("no", canI(user, namespace, verb, resource), user + " must not be allowed to " + verb + " "
                + resource + (namespace == null ? " (cluster-wide)" : " in " + namespace));
    }

    private static String canI(String user, String namespace, String verb, String resource) throws Exception {
        String[] command = namespace == null
                ? new String[] {"kubectl", "auth", "can-i", verb, resource, "--as=" + user}
                : new String[] {"kubectl", "auth", "can-i", verb, resource, "-n", namespace, "--as=" + user};
        // can-i exits with 1 when the answer is "no", so the exit code is not checked here.
        return k3s.execInContainer(command).getStdout().trim();
    }

    private static String kubectl(String... args) throws Exception {
        String[] command = Stream.concat(Stream.of("kubectl"), Arrays.stream(args)).toArray(String[]::new);
        ExecResult result = k3s.execInContainer(command);
        assertEquals(0, result.getExitCode(), String.join(" ", command) + " failed: " + result.getStderr());
        return result.getStdout().trim();
    }
}
