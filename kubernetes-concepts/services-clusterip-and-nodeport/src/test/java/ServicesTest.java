import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Arrays;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.k3s.K3sContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

class ServicesTest {

    static final K3sContainer k3s = new K3sContainer(DockerImageName.parse("rancher/k3s:v1.36.4-k3s1"));
    static ExecResult apply;

    @BeforeAll
    static void deploy() throws Exception {
        k3s.start();
        for (String f : new String[] {"k8s/shop.yaml", "k8s/client.yaml"}) {
            k3s.copyFileToContainer(MountableFile.forClasspathResource(f), "/lab/" + f);
        }
        kubectl("apply", "-f", "/lab/k8s/client.yaml");
        apply = k3s.execInContainer("kubectl", "apply", "-f", "/lab/k8s/shop.yaml");
        kubectl("rollout", "status", "deploy/shop", "--timeout=90s");
        kubectl("wait", "--for=condition=Ready", "pod/client", "--timeout=60s");
    }

    @AfterAll
    static void shutdown() {
        k3s.stop();
    }

    @Test
    void clusterIpServiceHasEndpoints() throws Exception {
        String addresses = kubectl("get", "endpointslices", "-l", "kubernetes.io/service-name=shop",
                "-o", "jsonpath={.items[*].endpoints[*].addresses[*]}");
        assertFalse(addresses.isBlank(), "Service shop selects no Pods");
        assertEquals(2, addresses.split(" ").length, "Service shop should route to both replicas: " + addresses);
    }

    @Test
    void clusterIpServiceAnswers() throws Exception {
        assertEquals("shop ok", get("http://shop"));
    }

    @Test
    void nodePortServiceAnswersOnEveryNode() throws Exception {
        assertEquals(0, apply.getExitCode(), "kubectl apply failed: " + apply.getStderr());
        assertEquals("30080", kubectl("get", "svc", "shop-public", "-o", "jsonpath={.spec.ports[0].nodePort}"));
        String nodeIp = kubectl("get", "nodes", "-o",
                "jsonpath={.items[0].status.addresses[?(@.type==\"InternalIP\")].address}");
        assertEquals("shop ok", get("http://" + nodeIp + ":30080"));
    }

    private static String get(String url) throws Exception {
        ExecResult response = null;
        for (int i = 0; i < 5; i++) {
            response = k3s.execInContainer("kubectl", "exec", "client", "--", "wget", "-qO-", "-T", "3", url);
            if (response.getExitCode() == 0) {
                return response.getStdout().trim();
            }
            Thread.sleep(2000);
        }
        return "no answer from " + url + ": " + response.getStderr().trim();
    }

    private static String kubectl(String... args) throws Exception {
        String[] command = Stream.concat(Stream.of("kubectl"), Arrays.stream(args)).toArray(String[]::new);
        ExecResult result = k3s.execInContainer(command);
        assertEquals(0, result.getExitCode(), String.join(" ", command) + " failed: " + result.getStderr());
        return result.getStdout().trim();
    }
}
