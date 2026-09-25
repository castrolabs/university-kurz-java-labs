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

class ProjectedSecretsTest {

    static final K3sContainer k3s = new K3sContainer(DockerImageName.parse("rancher/k3s:v1.36.4-k3s1"));

    @BeforeAll
    static void deploy() throws Exception {
        k3s.start();
        copy("secrets/example/alpha.env");
        copy("secrets/example/beta.env");
        copy("k8s/pod.yaml");
        copy("k8s/naive-pod.yaml");

        kubectl("create", "namespace", "example");
        kubectl("-n", "example", "create", "secret", "generic", "tenant-alpha",
                "--from-env-file=/lab/secrets/example/alpha.env");
        kubectl("-n", "example", "create", "secret", "generic", "tenant-beta",
                "--from-env-file=/lab/secrets/example/beta.env");
        kubectl("apply", "-f", "/lab/k8s/pod.yaml");
        kubectl("apply", "-f", "/lab/k8s/naive-pod.yaml");
        kubectl("-n", "example", "wait", "--for=condition=Ready", "pod/app", "pod/naive", "--timeout=180s");
    }

    @AfterAll
    static void shutdown() {
        k3s.stop();
    }

    @Test
    void naiveProjectionSilentlyLetsTheLastSourceWin() throws Exception {
        // Both Secrets have a DB_PASSWORD key: the later source overwrites the earlier one, with no error or event.
        assertEquals("beta-s3cret", cat("naive", "/run/secrets/DB_PASSWORD"));
    }

    @Test
    void alphaCredentialsLiveUnderAlphaDirectory() throws Exception {
        assertEquals("alpha_app", cat("app", "/run/secrets/alpha/DB_USER"));
        assertEquals("alpha-s3cret", cat("app", "/run/secrets/alpha/DB_PASSWORD"));
    }

    @Test
    void betaCredentialsLiveUnderBetaDirectory() throws Exception {
        assertEquals("beta_app", cat("app", "/run/secrets/beta/DB_USER"));
        assertEquals("beta-s3cret", cat("app", "/run/secrets/beta/DB_PASSWORD"));
    }

    @Test
    void tenantSpecificKeysAreNotDropped() throws Exception {
        // items is an allow-list: a key you forget to list simply never reaches the volume.
        assertEquals("ak-alpha-111", cat("app", "/run/secrets/alpha/ALPHA_API_KEY"));
        assertEquals("wt-beta-222", cat("app", "/run/secrets/beta/BETA_WEBHOOK_TOKEN"));
    }

    @Test
    void mountIsReadOnly() throws Exception {
        ExecResult write = k3s.execInContainer("kubectl", "-n", "example", "exec", "app", "--",
                "touch", "/run/secrets/alpha/tampered");
        assertNotEquals(0, write.getExitCode(), "the application must not be able to write into its secrets");
    }

    private static String cat(String pod, String path) throws Exception {
        return kubectl("-n", "example", "exec", pod, "--", "cat", path);
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
