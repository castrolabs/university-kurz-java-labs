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

class ConfigMapEnvTest {

    static final K3sContainer k3s = new K3sContainer(DockerImageName.parse("rancher/k3s:v1.36.4-k3s1"));
    static boolean ready;
    static String status;

    @BeforeAll
    static void deploy() throws Exception {
        k3s.start();
        k3s.copyFileToContainer(MountableFile.forClasspathResource("k8s/app.yaml"), "/lab/k8s/app.yaml");

        kubectl("create", "namespace", "example");
        kubectl("-n", "example", "create", "configmap", "app-config",
                "--from-literal=APP_MODE=production", "--from-literal=LOG_LEVEL=info");
        kubectl("-n", "example", "create", "secret", "generic", "db-credentials",
                "--from-literal=username=app", "--from-literal=password=s3cret");
        // An unrelated Service that happens to be called "redis". It exists before the Pod starts.
        kubectl("-n", "example", "create", "service", "clusterip", "redis", "--tcp=6379:6379");
        kubectl("apply", "-f", "/lab/k8s/app.yaml");

        ready = waitHealthy("app");
        status = kubectl("-n", "example", "get", "pod", "app", "-o",
                "jsonpath={.status.containerStatuses[0].state}{.status.containerStatuses[0].lastState}");
    }

    @AfterAll
    static void shutdown() {
        k3s.stop();
    }

    @Test
    void appStartsAndStaysUp() {
        assertTrue(ready, "pod/app is not running: " + status);
    }

    @Test
    void dbPasswordComesFromTheSecret() throws Exception {
        assertTrue(ready, "pod/app is not running: " + status);
        assertEquals("s3cret", env("DB_PASSWORD"));
    }

    @Test
    void appModeComesFromTheConfigMap() throws Exception {
        assertTrue(ready, "pod/app is not running: " + status);
        assertEquals("production", env("APP_MODE"));
    }

    @Test
    void logLevelIsOverriddenForThisPodOnly() throws Exception {
        assertTrue(ready, "pod/app is not running: " + status);
        assertEquals("debug", env("LOG_LEVEL"));
        assertEquals("info", kubectl("-n", "example", "get", "configmap", "app-config",
                "-o", "jsonpath={.data.LOG_LEVEL}"), "do not change the shared ConfigMap");
    }

    @Test
    void noDockerLinkVariablesForServices() throws Exception {
        assertTrue(ready, "pod/app is not running: " + status);
        assertEquals("", env("REDIS_SERVICE_HOST"), "Service environment variables are still injected");
    }

    private static String env(String name) throws Exception {
        return kubectl("-n", "example", "exec", "app", "--", "sh", "-c", "echo \"$" + name + "\"");
    }

    private static boolean waitHealthy(String pod) throws Exception {
        ExecResult result = k3s.execInContainer("kubectl", "-n", "example", "wait", "--for=condition=Ready",
                "pod/" + pod, "--timeout=90s");
        if (result.getExitCode() != 0) {
            return false;
        }
        // Without a readiness probe a container is "Ready" the moment it starts, even if it exits a second later.
        Thread.sleep(5000);
        return kubectl("-n", "example", "get", "pod", pod, "-o",
                "jsonpath={.status.containerStatuses[0].ready}/{.status.containerStatuses[0].restartCount}")
                .equals("true/0");
    }

    private static String kubectl(String... args) throws Exception {
        String[] command = Stream.concat(Stream.of("kubectl"), Arrays.stream(args)).toArray(String[]::new);
        ExecResult result = k3s.execInContainer(command);
        assertEquals(0, result.getExitCode(), String.join(" ", command) + " failed: " + result.getStderr());
        return result.getStdout().trim();
    }
}
