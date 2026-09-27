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

class VolumePermissionsTest {

    static final K3sContainer k3s = new K3sContainer(DockerImageName.parse("rancher/k3s:v1.36.4-k3s1"));
    static boolean loaded;
    static String status = "";

    @BeforeAll
    static void deploy() throws Exception {
        k3s.start();
        k3s.copyFileToContainer(MountableFile.forClasspathResource("k8s/app.yaml"), "/lab/k8s/app.yaml");
        kubectl("create", "namespace", "example");
        kubectl("-n", "example", "create", "secret", "generic", "app-tls", "--from-literal=tls.key=PRIVATE-KEY");
        kubectl("apply", "-f", "/lab/k8s/app.yaml");
        long start = System.currentTimeMillis();
        while (!loaded && System.currentTimeMillis() - start < 60_000) {
            loaded = k3s.execInContainer("kubectl", "-n", "example", "logs", "app").getStdout().contains("key loaded");
            Thread.sleep(2000);
        }
        status = kubectl("-n", "example", "get", "pod", "app", "-o",
                "jsonpath={.status.containerStatuses[0].state}{.status.containerStatuses[0].lastState}");
        ExecResult logs = k3s.execInContainer("kubectl", "-n", "example", "logs", "app", "--previous");
        if (logs.getExitCode() == 0) {
            status += " previous logs: " + logs.getStdout().trim();
        }
    }

    @AfterAll
    static void shutdown() {
        k3s.stop();
    }

    @Test
    void appLoadsItsKey() {
        assertTrue(loaded, "pod/app never logged 'key loaded': " + status);
    }

    @Test
    void appRunsAsUser1000() throws Exception {
        assertTrue(loaded, "pod/app is not running: " + status);
        assertEquals("1000:1000", exec("echo $(id -u):$(id -g)"));
    }

    @Test
    void keyIsNotWorldReadable() throws Exception {
        assertTrue(loaded, "pod/app is not running: " + status);
        String mode = exec("stat -L -c %a /etc/tls/tls.key");
        assertTrue(mode.endsWith("0"), "tls.key has mode " + mode + ": other users can read it");
    }

    private static String exec(String script) throws Exception {
        return kubectl("-n", "example", "exec", "app", "--", "sh", "-c", script);
    }

    private static String kubectl(String... args) throws Exception {
        String[] command = Stream.concat(Stream.of("kubectl"), Arrays.stream(args)).toArray(String[]::new);
        ExecResult result = k3s.execInContainer(command);
        assertEquals(0, result.getExitCode(), String.join(" ", command) + " failed: " + result.getStderr());
        return result.getStdout().trim();
    }
}
