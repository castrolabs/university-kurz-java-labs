import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.Arrays;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.startupcheck.OneShotStartupCheckStrategy;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.k3s.K3sContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

class PullSecretsTest {

    static final Network network = Network.newNetwork();

    // A private registry (TLS with a CA the node already trusts) that requires a login.
    static final GenericContainer<?> registry = new GenericContainer<>(DockerImageName.parse("registry:3"))
            .withNetwork(network)
            .withNetworkAliases("registry.lab")
            .withCopyFileToContainer(MountableFile.forClasspathResource("registry/registry.crt"), "/certs/registry.crt")
            .withCopyFileToContainer(MountableFile.forClasspathResource("registry/registry.key"), "/certs/registry.key")
            .withCopyFileToContainer(MountableFile.forClasspathResource("registry/htpasswd"), "/auth/htpasswd")
            .withEnv("REGISTRY_HTTP_ADDR", "0.0.0.0:443")
            .withEnv("REGISTRY_HTTP_TLS_CERTIFICATE", "/certs/registry.crt")
            .withEnv("REGISTRY_HTTP_TLS_KEY", "/certs/registry.key")
            .withEnv("REGISTRY_AUTH", "htpasswd")
            .withEnv("REGISTRY_AUTH_HTPASSWD_REALM", "registry.lab")
            .withEnv("REGISTRY_AUTH_HTPASSWD_PATH", "/auth/htpasswd")
            .waitingFor(Wait.forLogMessage(".*listening on.*", 1));

    static final K3sContainer k3s = new K3sContainer(DockerImageName.parse("rancher/k3s:v1.36.4-k3s1"))
            .withNetwork(network)
            .withCopyFileToContainer(MountableFile.forClasspathResource("registry/ca.crt"), "/etc/rancher/k3s/lab-ca.crt")
            .withCopyFileToContainer(MountableFile.forClasspathResource("registry/registries.yaml"),
                    "/etc/rancher/k3s/registries.yaml");

    static boolean firstRollout;
    static String firstWarnings = "";
    static String versionAfterRepush = "";

    @BeforeAll
    static void deploy() throws Exception {
        registry.start();
        push("mirror.gcr.io/library/busybox:1.36", "registry.lab/team-a/web:1.0");
        k3s.start();
        k3s.copyFileToContainer(MountableFile.forClasspathResource("k8s/app.yaml"), "/lab/k8s/app.yaml");
        kubectl("apply", "-f", "/lab/k8s/app.yaml");
        firstRollout = rollout();
        if (!firstRollout) {
            firstWarnings = kubectl("-n", "team-a", "events", "--types=Warning");
            return;
        }
        // A "hotfix" pushed to the same tag, then a restart.
        push("mirror.gcr.io/library/busybox:1.37", "registry.lab/team-a/web:1.0");
        kubectl("-n", "team-a", "rollout", "restart", "deploy/web");
        if (rollout()) {
            versionAfterRepush = kubectl("-n", "team-a", "exec", "deploy/web", "--", "sh", "-c", "busybox | head -1");
        }
    }

    private static boolean rollout() throws Exception {
        return k3s.execInContainer("kubectl", "-n", "team-a", "rollout", "status", "deploy/web", "--timeout=60s")
                .getExitCode() == 0;
    }

    private static void push(String from, String to) {
        try (GenericContainer<?> crane = new GenericContainer<>(DockerImageName.parse("gcr.io/go-containerregistry/crane:latest"))
                .withNetwork(network)
                .withEnv("DOCKER_CONFIG", "/cfg")
                .withCopyFileToContainer(MountableFile.forClasspathResource("registry/crane-config.json"), "/cfg/config.json")
                .withCommand("copy", from, to, "--insecure")
                .withStartupCheckStrategy(new OneShotStartupCheckStrategy().withTimeout(Duration.ofMinutes(3)))) {
            crane.start();
        }
    }

    @AfterAll
    static void shutdown() {
        k3s.stop();
        registry.stop();
        network.close();
    }

    @Test
    void webPullsWithCredentials() {
        assertTrue(firstRollout, "deploy/web never started. Warnings in team-a:\n" + firstWarnings);
    }

    @Test
    void restartRunsTheRepushedImage() {
        assertTrue(firstRollout, "deploy/web never started");
        assertTrue(versionAfterRepush.contains("v1.37"),
                "after the re-push and restart, the Pod still runs the old image: " + versionAfterRepush);
    }

    private static String kubectl(String... args) throws Exception {
        String[] command = Stream.concat(Stream.of("kubectl"), Arrays.stream(args)).toArray(String[]::new);
        ExecResult result = k3s.execInContainer(command);
        assertEquals(0, result.getExitCode(), String.join(" ", command) + " failed: " + result.getStderr());
        return result.getStdout().trim();
    }
}
