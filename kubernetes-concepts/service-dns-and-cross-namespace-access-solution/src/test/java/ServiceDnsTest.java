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

class ServiceDnsTest {

    static final K3sContainer k3s = new K3sContainer(DockerImageName.parse("rancher/k3s:v1.36.4-k3s1"));

    @BeforeAll
    static void deploy() throws Exception {
        k3s.start();
        for (String f : new String[] {"k8s/shared.yaml", "k8s/team-a.yaml"}) {
            k3s.copyFileToContainer(MountableFile.forClasspathResource(f), "/lab/" + f);
        }
        kubectl("apply", "-f", "/lab/k8s/shared.yaml");
        kubectl("-n", "shared", "rollout", "status", "deploy/db", "--timeout=90s");
        kubectl("apply", "-f", "/lab/k8s/team-a.yaml");
        k3s.execInContainer("kubectl", "-n", "team-a", "rollout", "status", "deploy/api", "--timeout=60s");
        k3s.execInContainer("kubectl", "-n", "team-a", "rollout", "status", "deploy/legacy", "--timeout=30s");
    }

    @AfterAll
    static void shutdown() {
        k3s.stop();
    }

    @Test
    void apiReachesTheDatabase() throws Exception {
        ExecResult call = k3s.execInContainer("kubectl", "-n", "team-a", "exec", "deploy/api", "--",
                "sh", "-c", "wget -qO- -T 2 \"$DB_URL\"");
        assertEquals(0, call.getExitCode(), "api cannot reach its database: " + call.getStderr());
        assertEquals("db ok", call.getStdout().trim());
    }

    @Test
    void legacyReachesTheDatabase() throws Exception {
        ExecResult call = k3s.execInContainer("kubectl", "-n", "team-a", "exec", "deploy/legacy", "--",
                "wget", "-qO-", "-T", "2", "http://database:8080");
        assertEquals(0, call.getExitCode(), "legacy cannot reach http://database:8080: " + call.getStderr());
        assertEquals("db ok", call.getStdout().trim());
    }

    @Test
    void theDatabaseRunsOnlyOnce() throws Exception {
        assertEquals("1", kubectl("get", "pods", "-A", "-l", "app=db", "-o", "name").lines().count() + "");
        assertTrue(kubectl("get", "endpointslices", "-n", "team-a", "-o", "name").isEmpty(),
                "team-a should not need Services with endpoints of its own for the database");
        String manifest = kubectl("get", "svc", "-n", "team-a", "-o", "yaml");
        assertFalse(manifest.contains("clusterIP: 10."), "team-a should not have a ClusterIP Service for the database");
    }

    private static String kubectl(String... args) throws Exception {
        String[] command = Stream.concat(Stream.of("kubectl"), Arrays.stream(args)).toArray(String[]::new);
        ExecResult result = k3s.execInContainer(command);
        assertEquals(0, result.getExitCode(), String.join(" ", command) + " failed: " + result.getStderr());
        return result.getStdout().trim();
    }
}
