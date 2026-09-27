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

class KustomizePatchesTest {

    static final K3sContainer k3s = new K3sContainer(DockerImageName.parse("rancher/k3s:v1.36.4-k3s1"));
    static final String OVERLAY = "/lab/kustomize/overlays/production";
    static ExecResult build;

    @BeforeAll
    static void deploy() throws Exception {
        k3s.start();
        k3s.copyFileToContainer(MountableFile.forClasspathResource("kustomize"), "/lab/kustomize");
        kubectl("create", "namespace", "example");
        build = k3s.execInContainer("kubectl", "kustomize", OVERLAY);
        if (build.getExitCode() == 0) {
            kubectl("apply", "-k", OVERLAY);
        }
    }

    @AfterAll
    static void shutdown() {
        k3s.stop();
    }

    @Test
    void overlayBuilds() {
        assertEquals(0, build.getExitCode(), "kubectl kustomize failed: " + build.getStderr());
    }

    @Test
    void everyDeploymentGetsTimezone() throws Exception {
        assertBuilt();
        for (String name : new String[] {"web", "worker", "worker-emails"}) {
            assertEquals("UTC", field(name, "{.spec.template.spec.containers[0].env[?(@.name==\"TZ\")].value}"),
                    "deployment/" + name + " has no TZ=UTC");
        }
        assertEquals("8080", field("web", "{.spec.template.spec.containers[0].env[?(@.name==\"PORT\")].value}"),
                "web lost its own PORT variable");
    }

    @Test
    void workerKeepsSharedConfigAndGetsItsSecret() throws Exception {
        assertBuilt();
        String envFrom = field("worker", "{.spec.template.spec.containers[0].envFrom}");
        assertTrue(envFrom.contains("\"app-config-"), "worker lost the app-config reference: " + envFrom);
        assertTrue(envFrom.contains("\"worker-secrets\""), "worker has no worker-secrets reference: " + envFrom);
    }

    @Test
    void everyWorkerHasThreeReplicas() throws Exception {
        assertBuilt();
        assertEquals("3", field("worker", "{.spec.replicas}"), "deployment/worker");
        assertEquals("3", field("worker-emails", "{.spec.replicas}"), "deployment/worker-emails");
        assertEquals("2", field("web", "{.spec.replicas}"), "web is not a worker and keeps 2 replicas");
    }

    private static void assertBuilt() {
        assertEquals(0, build.getExitCode(), "the overlay does not build yet: " + build.getStderr());
    }

    private static String field(String deployment, String jsonpath) throws Exception {
        return kubectl("-n", "example", "get", "deploy", deployment, "-o", "jsonpath=" + jsonpath);
    }

    private static String kubectl(String... args) throws Exception {
        String[] command = Stream.concat(Stream.of("kubectl"), Arrays.stream(args)).toArray(String[]::new);
        ExecResult result = k3s.execInContainer(command);
        assertEquals(0, result.getExitCode(), String.join(" ", command) + " failed: " + result.getStderr());
        return result.getStdout().trim();
    }
}
