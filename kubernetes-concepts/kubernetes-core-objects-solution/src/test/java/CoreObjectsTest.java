import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.k3s.K3sContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CoreObjectsTest {

    static final K3sContainer k3s = new K3sContainer(DockerImageName.parse("rancher/k3s:v1.36.4-k3s1"));

    @BeforeAll
    static void deploy() throws Exception {
        k3s.start();
        k3s.copyFileToContainer(MountableFile.forClasspathResource("k8s/web.yaml"), "/lab/k8s/web.yaml");
        kubectl("apply", "-f", "/lab/k8s/web.yaml");
        kubectl("run", "client", "--image=busybox:1.37", "--restart=Never", "--", "sleep", "3600");
        kubectl("wait", "--for=condition=Ready", "pod/client", "--timeout=90s");
        k3s.execInContainer("kubectl", "-n", "shop", "wait", "--for=condition=Ready", "pod", "-l", "app=web",
                "--timeout=60s");
    }

    @AfterAll
    static void shutdown() {
        k3s.stop();
    }

    @Test
    @Order(1)
    void podsBelongToADeploymentThroughAReplicaSet() throws Exception {
        String owners = kubectl("-n", "shop", "get", "pods", "-l", "app=web", "-o",
                "jsonpath={range .items[*]}{.metadata.name} <- {.metadata.ownerReferences[0].kind}/{.metadata.ownerReferences[0].name}{\"\\n\"}{end}");
        assertTrue(!owners.isBlank() && owners.lines().allMatch(l -> l.contains("<- ReplicaSet/")),
                "every web Pod should be owned by a ReplicaSet:\n" + owners);
        String replicaSet = kubectl("-n", "shop", "get", "rs", "-l", "app=web", "-o",
                "jsonpath={.items[0].metadata.ownerReferences[0].kind}/{.items[0].metadata.ownerReferences[0].name}");
        assertEquals("Deployment/web", replicaSet);
        assertEquals("2", kubectl("-n", "shop", "get", "deploy", "web", "-o", "jsonpath={.status.readyReplicas}"));
    }

    @Test
    @Order(2)
    void serviceSpreadsTrafficOverEveryReplica() throws Exception {
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 30 && seen.size() < 2; i++) {
            ExecResult r = k3s.execInContainer("kubectl", "exec", "client", "--", "wget", "-qO-", "-T", "2",
                    "http://web.shop");
            if (r.getExitCode() == 0) {
                seen.add(r.getStdout().trim());
            }
        }
        assertEquals(2, seen.size(), "responses came from " + seen + ". Endpoints of the Service: "
                + kubectl("-n", "shop", "get", "endpointslices", "-l", "kubernetes.io/service-name=web", "-o",
                        "jsonpath={.items[*].endpoints[*].addresses[*]}"));
    }

    @Test
    @Order(3)
    void deletedPodsComeBack() throws Exception {
        kubectl("-n", "shop", "delete", "pods", "-l", "app=web", "--wait=false");
        Thread.sleep(2000);
        ExecResult ready = k3s.execInContainer("kubectl", "-n", "shop", "wait", "--for=condition=Ready", "pod",
                "-l", "app=web", "--timeout=60s");
        assertEquals(0, ready.getExitCode(), "no web Pod came back after the delete: " + ready.getStderr());
        assertEquals("200", k3s.execInContainer("sh", "-c", "kubectl exec client -- wget -S -qO /dev/null "
                + "http://web.shop 2>&1 | grep -o '200' | head -1").getStdout().trim());
    }

    private static String kubectl(String... args) throws Exception {
        String[] command = Stream.concat(Stream.of("kubectl"), Arrays.stream(args)).toArray(String[]::new);
        ExecResult result = k3s.execInContainer(command);
        assertEquals(0, result.getExitCode(), String.join(" ", command) + " failed: " + result.getStderr());
        return result.getStdout().trim();
    }
}
