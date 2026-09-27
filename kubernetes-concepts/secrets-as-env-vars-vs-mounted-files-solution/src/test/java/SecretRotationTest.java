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

class SecretRotationTest {

    static final K3sContainer k3s = new K3sContainer(DockerImageName.parse("rancher/k3s:v1.36.4-k3s1"));
    static final String FILE = "/etc/app/secrets/DB_PASSWORD";
    static boolean ready;
    static String passwordBeforeRotation;
    static long secondsUntilRotated = -1;

    @BeforeAll
    static void deployAndRotate() throws Exception {
        k3s.start();
        k3s.copyFileToContainer(MountableFile.forClasspathResource("k8s/app.yaml"), "/lab/k8s/app.yaml");
        k3s.copyFileToContainer(MountableFile.forClasspathResource("k8s/naive-app.yaml"), "/lab/k8s/naive-app.yaml");

        kubectl("create", "namespace", "example");
        kubectl("-n", "example", "create", "secret", "generic", "db-credentials", "--from-literal=DB_PASSWORD=old-111");
        kubectl("apply", "-f", "/lab/k8s/app.yaml");
        kubectl("apply", "-f", "/lab/k8s/naive-app.yaml");
        kubectl("-n", "example", "wait", "--for=condition=Ready", "pod/naive-app", "--timeout=120s");
        ready = k3s.execInContainer("kubectl", "-n", "example", "wait", "--for=condition=Ready", "pod/app",
                "--timeout=60s").getExitCode() == 0;
        if (!ready) {
            return;
        }
        passwordBeforeRotation = readFile("app");

        // The rotation: same Secret name, new value, no Pod restart.
        k3s.execInContainer("sh", "-c", "kubectl -n example create secret generic db-credentials "
                + "--from-literal=DB_PASSWORD=new-222 --dry-run=client -o yaml | kubectl apply -f -");
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < 150_000) {
            if ("new-222".equals(readFile("app"))) {
                secondsUntilRotated = (System.currentTimeMillis() - start) / 1000;
                break;
            }
            Thread.sleep(3000);
        }
    }

    @AfterAll
    static void shutdown() {
        k3s.stop();
    }

    @Test
    void naiveEnvVarAndSubPathKeepTheOldPassword() throws Exception {
        // Environment variables are copied into the process at start; subPath mounts are a one-time snapshot.
        assertEquals("old-111", kubectl("-n", "example", "exec", "naive-app", "--", "sh", "-c", "echo \"$DB_PASSWORD\""));
        assertEquals("old-111", readFile("naive-app"));
    }

    @Test
    void appReadsThePasswordFromTheFile() {
        assertTrue(ready, "pod/app did not become Ready");
        assertEquals("old-111", passwordBeforeRotation);
    }

    @Test
    void passwordIsNotInTheProcessEnvironment() throws Exception {
        assertTrue(ready, "pod/app did not become Ready");
        assertEquals("", kubectl("-n", "example", "exec", "app", "--", "sh", "-c", "echo \"$DB_PASSWORD\""),
                "DB_PASSWORD is still set as an environment variable");
    }

    @Test
    void mountedFileFollowsTheRotation() {
        assertTrue(ready, "pod/app did not become Ready");
        assertTrue(secondsUntilRotated >= 0,
                FILE + " still has the old password 150 s after the rotation (subPath mounts are never updated)");
        System.out.println("kubelet refreshed the mounted Secret after about " + secondsUntilRotated + " s");
    }

    private static String readFile(String pod) throws Exception {
        ExecResult result = k3s.execInContainer("kubectl", "-n", "example", "exec", pod, "--", "cat", FILE);
        return result.getStdout().trim();
    }

    private static String kubectl(String... args) throws Exception {
        String[] command = Stream.concat(Stream.of("kubectl"), Arrays.stream(args)).toArray(String[]::new);
        ExecResult result = k3s.execInContainer(command);
        assertEquals(0, result.getExitCode(), String.join(" ", command) + " failed: " + result.getStderr());
        return result.getStdout().trim();
    }
}
