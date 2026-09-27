import static org.junit.jupiter.api.Assertions.assertEquals;
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

class PodSecurityTest {

    static final K3sContainer k3s = new K3sContainer(DockerImageName.parse("rancher/k3s:v1.36.4-k3s1"));
    static ExecResult rootPod;
    static boolean webAvailable;
    static String replicaSetEvents = "";

    @BeforeAll
    static void deploy() throws Exception {
        k3s.start();
        for (String f : new String[] {"k8s/namespace.yaml", "k8s/web.yaml", "k8s/root-pod.yaml"}) {
            k3s.copyFileToContainer(MountableFile.forClasspathResource(f), "/lab/" + f);
        }
        kubectl("apply", "-f", "/lab/k8s/namespace.yaml");
        rootPod = k3s.execInContainer("kubectl", "apply", "-f", "/lab/k8s/root-pod.yaml");
        kubectl("apply", "-f", "/lab/k8s/web.yaml");
        webAvailable = k3s.execInContainer("kubectl", "-n", "prod", "rollout", "status", "deploy/web",
                "--timeout=90s").getExitCode() == 0;
        if (!webAvailable) {
            replicaSetEvents = kubectl("-n", "prod", "get", "events", "--field-selector", "reason=FailedCreate",
                    "-o", "jsonpath={.items[0].message}");
        }
    }

    @AfterAll
    static void shutdown() {
        k3s.stop();
    }

    @Test
    void namespaceEnforcesRestricted() throws Exception {
        assertEquals("restricted", label("pod-security.kubernetes.io/enforce"));
        assertEquals("latest", label("pod-security.kubernetes.io/enforce-version"));
        assertEquals("restricted", label("pod-security.kubernetes.io/warn"));
        assertEquals("restricted", label("pod-security.kubernetes.io/audit"));
    }

    @Test
    void rootPodIsRejected() {
        assertNotEquals(0, rootPod.getExitCode(), "a Pod running as root was accepted in prod");
        assertTrue(rootPod.getStderr().contains("violates PodSecurity"), rootPod.getStderr());
    }

    @Test
    void webRunsInProd() {
        assertTrue(webAvailable, "deploy/web has no available Pod. ReplicaSet says: " + replicaSetEvents);
    }

    @Test
    void webRunsAsNonRoot() throws Exception {
        assertTrue(webAvailable, "deploy/web has no available Pod");
        assertNotEquals("0", kubectl("-n", "prod", "exec", "deploy/web", "--", "id", "-u"));
        assertEquals("ok", kubectl("-n", "prod", "exec", "deploy/web", "--", "wget", "-qO-", "http://127.0.0.1:8080"));
    }

    private static String label(String key) throws Exception {
        return kubectl("get", "namespace", "prod", "-o", "jsonpath={.metadata.labels." + key.replace(".", "\\.") + "}");
    }

    private static String kubectl(String... args) throws Exception {
        String[] command = Stream.concat(Stream.of("kubectl"), Arrays.stream(args)).toArray(String[]::new);
        ExecResult result = k3s.execInContainer(command);
        assertEquals(0, result.getExitCode(), String.join(" ", command) + " failed: " + result.getStderr());
        return result.getStdout().trim();
    }
}
