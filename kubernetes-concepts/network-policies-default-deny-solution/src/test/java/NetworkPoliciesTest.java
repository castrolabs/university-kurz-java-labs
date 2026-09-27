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

class NetworkPoliciesTest {

    static final K3sContainer k3s = new K3sContainer(DockerImageName.parse("rancher/k3s:v1.36.4-k3s1"));
    static String dbIp;
    static String cacheIp;

    @BeforeAll
    static void deploy() throws Exception {
        k3s.start();
        k3s.copyFileToContainer(MountableFile.forClasspathResource("k8s/workloads.yaml"), "/lab/k8s/workloads.yaml");
        k3s.copyFileToContainer(MountableFile.forClasspathResource("k8s/policies.yaml"), "/lab/k8s/policies.yaml");
        for (String ns : new String[] {"shared", "team-a", "team-b"}) {
            kubectl("create", "namespace", ns);
        }
        kubectl("apply", "-f", "/lab/k8s/workloads.yaml");
        kubectl("-n", "shared", "wait", "--for=condition=Ready", "pod/db", "pod/cache", "pod/impostor", "--timeout=120s");
        kubectl("-n", "team-a", "wait", "--for=condition=Ready", "pod/api", "--timeout=60s");
        kubectl("-n", "team-b", "wait", "--for=condition=Ready", "pod/client", "--timeout=60s");
        dbIp = kubectl("-n", "shared", "get", "svc", "db", "-o", "jsonpath={.spec.clusterIP}");
        cacheIp = kubectl("-n", "shared", "get", "svc", "cache", "-o", "jsonpath={.spec.clusterIP}");
        kubectl("apply", "-f", "/lab/k8s/policies.yaml");
        Thread.sleep(10_000); // let the network policy controller program the rules
    }

    @AfterAll
    static void shutdown() {
        k3s.stop();
    }

    @Test
    void apiPodInTeamAReachesDbByName() throws Exception {
        // Allow rules can take a few seconds more than deny rules to be programmed: retry for up to 30 s.
        String body = "";
        for (int i = 0; i < 10 && !body.equals("db"); i++) {
            body = get("team-a", "api", "db.shared");
        }
        assertEquals("db", body,
                "team-a/api cannot reach http://db.shared:8080 (DNS or egress blocked?)");
    }

    @Test
    void apiLabelInAnotherNamespaceIsNotEnough() throws Exception {
        assertNotEquals("db", get("shared", "impostor", dbIp), "shared/impostor (app=api) reached db");
    }

    @Test
    void teamBReachesNothingInShared() throws Exception {
        assertNotEquals("db", get("team-b", "client", "db.shared"), "team-b reached db");
        assertNotEquals("cache", get("team-b", "client", "cache.shared"), "team-b reached cache");
    }

    @Test
    void cacheIsClosedEvenInsideShared() throws Exception {
        assertNotEquals("cache", get("shared", "impostor", cacheIp), "shared/impostor reached cache");
    }

    private static String get(String namespace, String pod, String host) throws Exception {
        ExecResult result = k3s.execInContainer("kubectl", "-n", namespace, "exec", pod, "--",
                "wget", "-qO-", "-T", "3", "http://" + host + ":8080");
        return result.getStdout().trim();
    }

    private static String kubectl(String... args) throws Exception {
        String[] command = Stream.concat(Stream.of("kubectl"), Arrays.stream(args)).toArray(String[]::new);
        ExecResult result = k3s.execInContainer(command);
        assertEquals(0, result.getExitCode(), String.join(" ", command) + " failed: " + result.getStderr());
        return result.getStdout().trim();
    }
}
