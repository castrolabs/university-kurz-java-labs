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

class KustomizeGeneratorsTest {

    static final K3sContainer k3s = new K3sContainer(DockerImageName.parse("rancher/k3s:v1.36.4-k3s1"));
    static final String OVERLAY = "/lab/kustomize/overlays/staging";
    static String greetingAfterChange;

    @BeforeAll
    static void deploy() throws Exception {
        k3s.start();
        k3s.copyFileToContainer(MountableFile.forClasspathResource("kustomize"), "/lab/kustomize");
        kubectl("create", "namespace", "example");
        kubectl("apply", "-k", OVERLAY);
        kubectl("-n", "example", "rollout", "status", "deploy/web", "--timeout=120s");

        // A configuration change, applied the way a pipeline would: edit the overlay, apply again.
        sh("grep -rl hi-staging /lab/kustomize | xargs sed -i 's/hi-staging/hi-v2/g'");
        kubectl("apply", "-k", OVERLAY);
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < 60_000) {
            ExecResult result = k3s.execInContainer("kubectl", "-n", "example", "exec", "deploy/web", "--",
                    "sh", "-c", "echo $GREETING");
            greetingAfterChange = result.getStdout().trim();
            if ("hi-v2".equals(greetingAfterChange)) {
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
    void baseAndOverlayValuesAreMerged() throws Exception {
        assertEquals("info", env("LOG_LEVEL"), "the base value was lost");
        assertEquals("staging", env("ENVIRONMENT"));
    }

    @Test
    void configMapNameCarriesAContentHash() throws Exception {
        String names = kubectl("-n", "example", "get", "configmap", "-o", "jsonpath={.items[*].metadata.name}");
        assertTrue(names.matches(".*\\bapp-config-[a-z0-9]{10}\\b.*"), "no hashed app-config ConfigMap in: " + names);
    }

    @Test
    void secretIsGeneratedFromTheEnvFile() throws Exception {
        assertEquals("from-env-file", env("DB_PASSWORD"));
        String names = kubectl("-n", "example", "get", "secret", "-o", "jsonpath={.items[*].metadata.name}");
        assertTrue(names.matches(".*\\bdb-credentials-[a-z0-9]{10}\\b.*"), "no hashed db-credentials Secret in: " + names);
    }

    @Test
    void changingAValueRollsOutTheDeployment() {
        assertEquals("hi-v2", greetingAfterChange,
                "the new GREETING never reached the Pod: nothing in the Pod template changed, so nothing restarted");
    }

    private static String env(String name) throws Exception {
        return kubectl("-n", "example", "exec", "deploy/web", "--", "sh", "-c", "echo $" + name);
    }

    private static void sh(String script) throws Exception {
        ExecResult result = k3s.execInContainer("sh", "-c", script);
        assertEquals(0, result.getExitCode(), script + " failed: " + result.getStderr());
    }

    private static String kubectl(String... args) throws Exception {
        String[] command = Stream.concat(Stream.of("kubectl"), Arrays.stream(args)).toArray(String[]::new);
        ExecResult result = k3s.execInContainer(command);
        assertEquals(0, result.getExitCode(), String.join(" ", command) + " failed: " + result.getStderr());
        return result.getStdout().trim();
    }
}
