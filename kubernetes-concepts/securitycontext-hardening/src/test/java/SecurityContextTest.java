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

class SecurityContextTest {

    static final K3sContainer k3s = new K3sContainer(DockerImageName.parse("rancher/k3s:v1.36.4-k3s1"));
    static boolean webAvailable;
    static String webLogs = "";

    @BeforeAll
    static void deploy() throws Exception {
        k3s.start();
        for (String f : new String[] {"k8s/web.yaml", "k8s/client.yaml"}) {
            k3s.copyFileToContainer(MountableFile.forClasspathResource(f), "/lab/" + f);
        }
        kubectl("apply", "-f", "/lab/k8s/web.yaml", "-f", "/lab/k8s/client.yaml");
        webAvailable = k3s.execInContainer("kubectl", "rollout", "status", "deploy/web", "--timeout=90s")
                .getExitCode() == 0;
        if (!webAvailable) {
            webLogs = k3s.execInContainer("kubectl", "logs", "deploy/web", "--tail=5").getStdout();
        }
        kubectl("wait", "--for=condition=Ready", "pod/client", "--timeout=60s");
    }

    @AfterAll
    static void shutdown() {
        k3s.stop();
    }

    @Test
    void rootFilesystemIsReadOnly() throws Exception {
        assertEquals("true", kubectl("get", "deploy", "web", "-o",
                "jsonpath={.spec.template.spec.containers[0].securityContext.readOnlyRootFilesystem}"));
        requireWeb();
        ExecResult write = k3s.execInContainer("kubectl", "exec", "deploy/web", "--", "touch", "/usr/share/nginx/html/x");
        assertNotEquals(0, write.getExitCode(), "the container could write to its root filesystem");
    }

    @Test
    void processHasNoPrivileges() throws Exception {
        requireWeb();
        String status = kubectl("exec", "deploy/web", "--", "cat", "/proc/1/status");
        assertTrue(status.contains("CapEff:\t0000000000000000"), "PID 1 still has capabilities:\n" + status);
        assertTrue(status.contains("NoNewPrivs:\t1"), "privilege escalation is allowed");
        assertTrue(status.contains("Seccomp:\t2"), "no seccomp filter is applied");
        assertNotEquals("0", kubectl("exec", "deploy/web", "--", "id", "-u"), "the container runs as root");
    }

    @Test
    void serviceAnswers() throws Exception {
        requireWeb();
        ExecResult response = null;
        for (int i = 0; i < 10; i++) {
            response = k3s.execInContainer("kubectl", "exec", "client", "--", "wget", "-qO-", "-T", "3", "http://web");
            if (response.getExitCode() == 0) {
                break;
            }
            Thread.sleep(2000);
        }
        assertEquals(0, response.getExitCode(), "http://web did not answer: " + response.getStderr());
        assertTrue(response.getStdout().contains("Welcome to nginx"), response.getStdout());
    }

    private static void requireWeb() {
        assertTrue(webAvailable, "deploy/web never became available. Last log lines:\n" + webLogs);
    }

    private static String kubectl(String... args) throws Exception {
        String[] command = Stream.concat(Stream.of("kubectl"), Arrays.stream(args)).toArray(String[]::new);
        ExecResult result = k3s.execInContainer(command);
        assertEquals(0, result.getExitCode(), String.join(" ", command) + " failed: " + result.getStderr());
        return result.getStdout().trim();
    }
}
