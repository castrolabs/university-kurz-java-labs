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

class IngressTest {

    // Testcontainers starts k3s with --disable=traefik; this command keeps the bundled Traefik.
    static final K3sContainer k3s = new K3sContainer(DockerImageName.parse("rancher/k3s:v1.36.4-k3s1"))
            .withCommand("server")
            .withCopyFileToContainer(MountableFile.forClasspathResource("k8s/traefik-config.yaml"),
                    "/var/lib/rancher/k3s/server/manifests/traefik-config.yaml");

    @BeforeAll
    static void deploy() throws Exception {
        k3s.start();
        for (String f : new String[] {"k8s/apps.yaml", "k8s/routes.yaml"}) {
            k3s.copyFileToContainer(MountableFile.forClasspathResource(f), "/lab/" + f);
        }
        for (int i = 0; i < 60 && k3s.execInContainer("kubectl", "-n", "kube-system", "get", "gateway",
                "traefik-gateway").getExitCode() != 0; i++) {
            Thread.sleep(3000);   // Traefik is installed by a Helm job after the cluster starts
        }
        kubectl("-n", "kube-system", "rollout", "status", "deploy/traefik", "--timeout=180s");
        kubectl("apply", "-f", "/lab/k8s/apps.yaml");
        kubectl("-n", "web", "rollout", "status", "deploy/shop", "--timeout=90s");
        kubectl("-n", "web", "rollout", "status", "deploy/blog", "--timeout=90s");
        kubectl("wait", "--for=condition=Ready", "pod/client", "--timeout=60s");
        kubectl("apply", "-f", "/lab/k8s/routes.yaml");
    }

    @AfterAll
    static void shutdown() {
        k3s.stop();
    }

    @Test
    void shopServesEveryPage() throws Exception {
        assertEquals("200 shop", get("shop.lab", "/"));
        assertEquals("200 cart", get("shop.lab", "/cart/"));
    }

    @Test
    void blogIsServedThroughTheGatewayApi() throws Exception {
        assertFalse(kubectl("get", "ingress", "-A", "-o", "jsonpath={..host}").contains("blog.lab"),
                "blog.lab must be routed by an HTTPRoute, not an Ingress");
        assertEquals("200 blog", get("blog.lab", "/"), "HTTPRoute blog status:\n" + kubectl("-n", "web", "get",
                "httproute", "blog", "-o", "jsonpath={range .status.parents[*].conditions[*]}{.type}={.status} {.reason}{\"\\n\"}{end}"));
    }

    @Test
    void unknownHostsGetA404() throws Exception {
        assertEquals("404", get("other.lab", "/").split(" ")[0]);
    }

    @Test
    void platformGatewayIsUnchanged() throws Exception {
        assertEquals("Same", kubectl("-n", "kube-system", "get", "gateway", "traefik-gateway", "-o",
                "jsonpath={.spec.listeners[0].allowedRoutes.namespaces.from}"));
    }

    /** Sends a request to Traefik with the given Host header; returns "<status> <last line of the body>". */
    private static String get(String host, String path) throws Exception {
        String result = "";
        for (int i = 0; i < 10; i++) {   // routes take a few seconds to be picked up
            String raw = kubectl("exec", "client", "--", "sh", "-c", "(printf 'GET " + path
                    + " HTTP/1.0\\r\\nHost: " + host + "\\r\\n\\r\\n'; sleep 2) | nc -w 3 traefik.kube-system 80");
            String[] lines = raw.replace("\r", "").strip().split("\n");
            result = (lines[0].split(" ").length > 1 ? lines[0].split(" ")[1] : "?") + " " + lines[lines.length - 1];
            if (result.startsWith("200")) {
                break;
            }
            Thread.sleep(2000);
        }
        return result;
    }

    private static String kubectl(String... args) throws Exception {
        String[] command = Stream.concat(Stream.of("kubectl"), Arrays.stream(args)).toArray(String[]::new);
        ExecResult result = k3s.execInContainer(command);
        assertEquals(0, result.getExitCode(), String.join(" ", command) + " failed: " + result.getStderr());
        return result.getStdout().trim();
    }
}
