import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Arrays;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.k3s.K3sContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

class CiDeployTest {

    static final K3sContainer k3s = new K3sContainer(DockerImageName.parse("rancher/k3s:v1.36.4-k3s1"));
    static final String CI = "--as=system:serviceaccount:shop:ci-deployer";
    static ExecResult goodRelease;
    static ExecResult badRelease;

    @BeforeAll
    static void pipeline() throws Exception {
        k3s.start();
        k3s.copyFileToContainer(MountableFile.forClasspathResource("k8s/app.yaml"), "/lab/k8s/app.yaml");
        k3s.copyFileToContainer(MountableFile.forClasspathResource("k8s/ci-rbac.yaml"), "/lab/k8s/ci-rbac.yaml");
        k3s.copyFileToContainer(MountableFile.forClasspathResource("deploy.sh"), "/lab/deploy.sh");
        kubectl("apply", "-f", "/lab/k8s/ci-rbac.yaml");

        // A kubeconfig with only the ServiceAccount's token, as a CI system would store it.
        String token = kubectl("-n", "shop", "create", "token", "ci-deployer", "--duration=1h");
        String cfg = "--kubeconfig=/lab/ci.kubeconfig";
        kubectl("config", cfg, "set-cluster", "lab", "--server=https://127.0.0.1:6443",
                "--certificate-authority=/var/lib/rancher/k3s/server/tls/server-ca.crt", "--embed-certs");
        kubectl("config", cfg, "set-credentials", "ci", "--token=" + token);
        kubectl("config", cfg, "set-context", "ci", "--cluster=lab", "--user=ci");
        kubectl("config", cfg, "use-context", "ci");

        goodRelease = deploy("busybox:1.37");
        badRelease = deploy("busybox:0.0-does-not-exist");
    }

    private static ExecResult deploy(String image) throws Exception {
        return k3s.execInContainer("env", "KUBECONFIG=/lab/ci.kubeconfig", "IMAGE=" + image, "sh", "/lab/deploy.sh");
    }

    @AfterAll
    static void shutdown() {
        k3s.stop();
    }

    @Test
    void goodReleaseSucceeds() throws Exception {
        assertEquals(0, goodRelease.getExitCode(), "the good release failed:\n" + goodRelease.getStderr());
        assertEquals("2", kubectl("-n", "shop", "get", "deploy", "web", "-o", "jsonpath={.status.availableReplicas}"));
    }

    @Test
    void badReleaseFailsTheJobAndIsRolledBack() throws Exception {
        assertNotEquals(0, badRelease.getExitCode(), "the pipeline reported success for an image that does not exist");
        assertEquals("busybox:1.37", kubectl("-n", "shop", "get", "deploy", "web", "-o",
                "jsonpath={.spec.template.spec.containers[0].image}"), "the broken release was left in place");
    }

    @Test
    void ciCredentialsAreLimitedToDeployingInShop() throws Exception {
        assertEquals("yes", canI("patch", "deployments", "-n", "shop"));
        assertEquals("no", canI("get", "secrets", "-n", "shop"), "CI can read the Secrets in shop");
        assertEquals("no", canI("patch", "deployments", "-n", "kube-system"), "CI can deploy outside shop");
        assertEquals("no", canI("create", "clusterrolebindings"), "CI can grant itself anything");
    }

    private static String canI(String... args) throws Exception {
        String[] command = Stream.concat(Stream.concat(Stream.of("kubectl", "auth", "can-i"), Arrays.stream(args)),
                Stream.of(CI)).toArray(String[]::new);
        return k3s.execInContainer(command).getStdout().trim();
    }

    private static String kubectl(String... args) throws Exception {
        String[] command = Stream.concat(Stream.of("kubectl"), Arrays.stream(args)).toArray(String[]::new);
        ExecResult result = k3s.execInContainer(command);
        assertEquals(0, result.getExitCode(), String.join(" ", command) + " failed: " + result.getStderr());
        return result.getStdout().trim();
    }
}
