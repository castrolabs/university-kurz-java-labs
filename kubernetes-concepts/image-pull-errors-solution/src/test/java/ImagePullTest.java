import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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

class ImagePullTest {

    static final Network network = Network.newNetwork();

    // A private registry with its own CA, reachable from the node as registry.lab.
    static final GenericContainer<?> registry = new GenericContainer<>(DockerImageName.parse("registry:3"))
            .withNetwork(network)
            .withNetworkAliases("registry.lab")
            .withCopyFileToContainer(MountableFile.forClasspathResource("registry/registry.crt"), "/certs/registry.crt")
            .withCopyFileToContainer(MountableFile.forClasspathResource("registry/registry.key"), "/certs/registry.key")
            .withEnv("REGISTRY_HTTP_ADDR", "0.0.0.0:443")
            .withEnv("REGISTRY_HTTP_TLS_CERTIFICATE", "/certs/registry.crt")
            .withEnv("REGISTRY_HTTP_TLS_KEY", "/certs/registry.key")
            .waitingFor(Wait.forLogMessage(".*listening on.*", 1));

    static final K3sContainer k3s = new K3sContainer(DockerImageName.parse("rancher/k3s:v1.36.4-k3s1"))
            .withNetwork(network)
            .withCopyFileToContainer(MountableFile.forClasspathResource("registry/ca.crt"), "/etc/rancher/k3s/lab-ca.crt")
            .withCopyFileToContainer(MountableFile.forClasspathResource("registries.yaml"),
                    "/etc/rancher/k3s/registries.yaml");

    static boolean webAvailable;
    static String tags;

    @BeforeAll
    static void deploy() throws Exception {
        registry.start();
        seed("mirror.gcr.io/library/busybox:1.37", "registry.lab/tools/busybox:1.37");
        tags = registry.execInContainer("wget", "--no-check-certificate", "-qO-",
                "https://localhost/v2/tools/busybox/tags/list").getStdout().trim();
        k3s.start();
        k3s.copyFileToContainer(MountableFile.forClasspathResource("k8s/web.yaml"), "/lab/k8s/web.yaml");
        kubectl("apply", "-f", "/lab/k8s/web.yaml");
        webAvailable = k3s.execInContainer("kubectl", "rollout", "status", "deploy/web", "--timeout=60s")
                .getExitCode() == 0;
    }

    private static void seed(String from, String to) {
        try (GenericContainer<?> crane = new GenericContainer<>(DockerImageName.parse("gcr.io/go-containerregistry/crane:latest"))
                .withNetwork(network)
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
    void webPullsFromThePrivateRegistry() throws Exception {
        assertTrue(webAvailable, "deploy/web is not running. Latest warning:\n"
                + k3s.execInContainer("sh", "-c", "kubectl events --types=Warning --no-headers | grep 'Failed to pull' | tail -1")
                        .getStdout().trim()
                + "\n\nTags in registry.lab/tools/busybox: " + tags);
        assertEquals("ok", kubectl("exec", "deploy/web", "--", "echo", "ok"));
    }

    @Test
    void tlsIsStillVerified() throws Exception {
        String config;
        try (InputStream in = ImagePullTest.class.getResourceAsStream("/registries.yaml")) {
            config = new String(in.readAllBytes(), StandardCharsets.UTF_8).lines()
                    .filter(l -> !l.strip().startsWith("#")).reduce("", (a, b) -> a + b + "\n");
        }
        assertFalse(config.contains("insecure_skip_verify"), "trust the CA instead of disabling verification");
        assertTrue(config.contains("/etc/rancher/k3s/lab-ca.crt"), "registries.yaml does not use the lab CA");
    }

    private static String kubectl(String... args) throws Exception {
        String[] command = Stream.concat(Stream.of("kubectl"), Arrays.stream(args)).toArray(String[]::new);
        ExecResult result = k3s.execInContainer(command);
        assertEquals(0, result.getExitCode(), String.join(" ", command) + " failed: " + result.getStderr());
        return result.getStdout().trim();
    }
}
