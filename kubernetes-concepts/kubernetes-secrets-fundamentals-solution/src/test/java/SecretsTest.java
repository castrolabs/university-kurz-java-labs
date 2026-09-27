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

class SecretsTest {

    static final K3sContainer k3s = new K3sContainer(DockerImageName.parse("rancher/k3s:v1.36.4-k3s1"));
    static final String REPORTS = "--as=system:serviceaccount:shop:reports";
    static boolean apiReady;
    static String passwordAtStart;

    @BeforeAll
    static void deploy() throws Exception {
        k3s.start();
        for (String f : new String[] {"k8s/shop.yaml", "k8s/secrets.yaml"}) {
            k3s.copyFileToContainer(MountableFile.forClasspathResource(f), "/lab/" + f);
        }
        kubectl("apply", "-f", "/lab/k8s/shop.yaml");
        kubectl("apply", "-f", "/lab/k8s/secrets.yaml");
        kubectl("-n", "shop", "rollout", "status", "deploy/db", "--timeout=90s");
        apiReady = k3s.execInContainer("kubectl", "-n", "shop", "rollout", "status", "deploy/api", "--timeout=60s")
                .getExitCode() == 0;
        passwordAtStart = kubectl("-n", "shop", "get", "secret", "db-creds", "-o", "jsonpath={.data.password}");
    }

    @AfterAll
    static void shutdown() {
        k3s.stop();
    }

    @Test
    void apiLogsInToTheDatabase() throws Exception {
        assertTrue(apiReady, "the database rejects the api's password. The Secret holds (base64) " + passwordAtStart);
    }

    @Test
    void passwordCannotBeChangedInPlace() throws Exception {
        ExecResult patch = k3s.execInContainer("kubectl", "-n", "shop", "patch", "secret", "db-creds", "-p",
                "{\"stringData\":{\"password\":\"changed\"}}");
        assertNotEquals(0, patch.getExitCode(), "db-creds could be modified in place");
    }

    @Test
    void reportsReadOnlyTheirOwnSecret() throws Exception {
        assertEquals("yes", canI("get", "secret/report-creds"));
        assertEquals("no", canI("get", "secret/db-creds"), "reports can read the database password");
        assertEquals("no", canI("list", "secrets"), "reports can list (and so read) every Secret in shop");
    }

    private static String canI(String verb, String resource) throws Exception {
        return k3s.execInContainer("kubectl", "-n", "shop", "auth", "can-i", verb, resource, REPORTS).getStdout().trim();
    }

    private static String kubectl(String... args) throws Exception {
        String[] command = Stream.concat(Stream.of("kubectl"), Arrays.stream(args)).toArray(String[]::new);
        ExecResult result = k3s.execInContainer(command);
        assertEquals(0, result.getExitCode(), String.join(" ", command) + " failed: " + result.getStderr());
        return result.getStdout().trim();
    }
}
