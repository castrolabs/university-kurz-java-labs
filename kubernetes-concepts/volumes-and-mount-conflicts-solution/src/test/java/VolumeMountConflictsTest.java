import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

class VolumeMountConflictsTest {

    static final K3sContainer k3s = new K3sContainer(DockerImageName.parse("rancher/k3s:v1.36.4-k3s1"));

    @BeforeAll
    static void deploy() throws Exception {
        k3s.start();
        copy("k8s/app.yaml");
        copy("k8s/web.yaml");
        copy("k8s/naive-app.yaml");

        kubectl("create", "namespace", "example");
        kubectl("-n", "example", "create", "secret", "generic", "app-secrets", "--from-literal=API_KEY=k-123");
        kubectl("-n", "example", "create", "configmap", "nginx-extra",
                "--from-literal=extra.conf=server { listen 8081; location / { return 200 \"extra\\n\"; } }");
        kubectl("apply", "-f", "/lab/k8s/naive-app.yaml");
        kubectl("apply", "-f", "/lab/k8s/app.yaml");
        kubectl("apply", "-f", "/lab/k8s/web.yaml");
    }

    @AfterAll
    static void shutdown() {
        k3s.stop();
    }

    @Test
    void readOnlySecretAtRunSecretsBreaksTheTokenMountOnUbuntu() throws Exception {
        // On Debian/Ubuntu images /var/run is a symlink to /run, so the token mount point
        // /var/run/secrets/kubernetes.io/serviceaccount would have to be created inside the read-only Secret volume.
        String message = waitForTerminationMessage("naive-app");
        assertTrue(message.contains("read-only file system"), "expected a StartError, got: " + message);
    }

    @Test
    void appStartsWithItsSecretAtRunSecrets() throws Exception {
        assertTrue(waitReady("app"), "pod/app is not Ready: " + describe("app"));
        assertEquals("k-123", kubectl("-n", "example", "exec", "app", "--", "cat", "/run/secrets/API_KEY"));
    }

    @Test
    void appHasNoServiceAccountToken() throws Exception {
        assertTrue(waitReady("app"), "pod/app is not Ready: " + describe("app"));
        ExecResult ls = k3s.execInContainer("kubectl", "-n", "example", "exec", "app", "--",
                "ls", "/run/secrets/kubernetes.io");
        assertFalse(ls.getExitCode() == 0, "the app does not call the API, so it should not get a token");
    }

    @Test
    void extraServerBlockAnswers() throws Exception {
        assertTrue(waitReady("web"), "pod/web is not Ready: " + describe("web"));
        assertEquals("extra", httpGet(8081));
    }

    @Test
    void defaultSiteStillWorks() throws Exception {
        // Mounting the ConfigMap over /etc/nginx/conf.d would hide default.conf and break port 80.
        assertTrue(waitReady("web"), "pod/web is not Ready: " + describe("web"));
        assertTrue(httpGet(80).contains("Welcome to nginx!"), "the image's default site on port 80 is gone");
    }

    private static String httpGet(int port) throws Exception {
        return kubectl("-n", "example", "exec", "web", "--", "wget", "-qO-", "-T", "5", "http://127.0.0.1:" + port);
    }

    private static boolean waitReady(String pod) throws Exception {
        ExecResult result = k3s.execInContainer("kubectl", "-n", "example", "wait", "--for=condition=Ready",
                "pod/" + pod, "--timeout=90s");
        if (result.getExitCode() != 0) {
            return false;
        }
        // A container without a readiness probe is "Ready" the moment it starts, even if it crashes a second later.
        Thread.sleep(5000);
        String state = kubectl("-n", "example", "get", "pod", pod, "-o",
                "jsonpath={.status.containerStatuses[0].ready}/{.status.containerStatuses[0].restartCount}");
        return state.equals("true/0");
    }

    private static String waitForTerminationMessage(String pod) throws Exception {
        for (int i = 0; i < 60; i++) {
            String message = kubectl("-n", "example", "get", "pod", pod, "-o",
                    "jsonpath={.status.containerStatuses[0].lastState.terminated.message}"
                            + "{.status.containerStatuses[0].state.waiting.message}");
            if (!message.isEmpty()) {
                return message;
            }
            Thread.sleep(2000);
        }
        return "";
    }

    private static String describe(String pod) throws Exception {
        return kubectl("-n", "example", "get", "pod", pod, "-o",
                "jsonpath={.status.containerStatuses[0].state}{.status.containerStatuses[0].lastState}");
    }

    private static void copy(String resource) {
        k3s.copyFileToContainer(MountableFile.forClasspathResource(resource), "/lab/" + resource);
    }

    private static String kubectl(String... args) throws Exception {
        String[] command = Stream.concat(Stream.of("kubectl"), Arrays.stream(args)).toArray(String[]::new);
        ExecResult result = k3s.execInContainer(command);
        assertEquals(0, result.getExitCode(), String.join(" ", command) + " failed: " + result.getStderr());
        return result.getStdout().trim();
    }
}
