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

class AcmeTest {

    static final String CERT_MANAGER = "https://github.com/cert-manager/cert-manager/releases/download/v1.21.2/cert-manager.yaml";

    // Testcontainers starts k3s with --disable=traefik; this command keeps the bundled Traefik.
    static final K3sContainer k3s = new K3sContainer(DockerImageName.parse("rancher/k3s:v1.36.4-k3s1"))
            .withCommand("server");
    static boolean ready;
    static String traefik;

    @BeforeAll
    static void deploy() throws Exception {
        k3s.start();
        for (String f : new String[] {"k8s/coredns-custom.yaml", "k8s/pebble.yaml", "k8s/tls.yaml"}) {
            k3s.copyFileToContainer(MountableFile.forClasspathResource(f), "/lab/" + f);
        }
        for (int i = 0; i < 60 && k3s.execInContainer("kubectl", "-n", "kube-system", "get", "svc", "traefik")
                .getExitCode() != 0; i++) {
            Thread.sleep(3000);   // Traefik is installed by a Helm job after the cluster starts
        }
        traefik = kubectl("-n", "kube-system", "get", "svc", "traefik", "-o", "jsonpath={.spec.clusterIP}");
        k3s.execInContainer("sed", "-i", "s/TRAEFIK_IP/" + traefik + "/", "/lab/k8s/coredns-custom.yaml");
        kubectl("apply", "-f", "/lab/k8s/coredns-custom.yaml");
        kubectl("-n", "kube-system", "rollout", "restart", "deploy/coredns");   // load it now, not in a minute or two
        kubectl("apply", "-f", CERT_MANAGER);
        for (String d : new String[] {"cert-manager", "cert-manager-cainjector", "cert-manager-webhook"}) {
            kubectl("-n", "cert-manager", "rollout", "status", "deploy/" + d, "--timeout=180s");
        }
        kubectl("-n", "kube-system", "rollout", "status", "deploy/traefik", "--timeout=180s");
        kubectl("apply", "-f", "/lab/k8s/pebble.yaml");
        kubectl("-n", "cert-manager", "rollout", "status", "deploy/pebble", "--timeout=120s");
        kubectl("-n", "web", "rollout", "status", "deploy/shop", "--timeout=90s");
        kubectl("wait", "--for=condition=Ready", "pod/client", "--timeout=120s");
        for (int i = 0; i < 30 && !k3s.execInContainer("kubectl", "-n", "web", "exec", "deploy/shop", "--",
                "nslookup", "www.shop.lab").getStdout().contains(traefik); i++) {
            Thread.sleep(2000);
        }
        for (int i = 0; i < 20; i++) {   // the webhook answers a little after its Pod is ready
            ExecResult apply = k3s.execInContainer("kubectl", "apply", "-f", "/lab/k8s/tls.yaml");
            if (apply.getExitCode() == 0) {
                break;
            }
            Thread.sleep(3000);
        }
        ready = k3s.execInContainer("kubectl", "-n", "web", "wait", "--for=condition=Ready", "certificate/shop",
                "--timeout=180s").getExitCode() == 0;
    }

    @AfterAll
    static void shutdown() {
        k3s.stop();
    }

    @Test
    void certificateIsIssuedByTheAcmeCa() throws Exception {
        assertTrue(ready, "certificate/shop is not Ready.\n" + status());
        String names = kubectl("-n", "web", "get", "certificate", "shop", "-o", "jsonpath={.spec.dnsNames}");
        assertTrue(names.contains("\"shop.lab\"") && names.contains("\"www.shop.lab\""), "dnsNames: " + names);
    }

    @Test
    void bothHostsServeTheIssuedCertificate() throws Exception {
        for (String host : new String[] {"shop.lab", "www.shop.lab"}) {
            ExecResult served = null;
            for (int i = 0; i < 10; i++) {   // Traefik picks up new certificates within a few seconds
                served = k3s.execInContainer("kubectl", "exec", "client", "--", "sh", "-c",
                        "curl -skv --max-time 5 --resolve " + host + ":443:" + traefik + " https://" + host
                                + "/ 2>&1 | grep -E 'issuer:|^shop'");
                if (served.getStdout().contains("Pebble")) {
                    break;
                }
                Thread.sleep(3000);
            }
            assertTrue(served.getStdout().contains("Pebble") && served.getStdout().contains("shop"),
                    host + " did not serve an ACME-issued certificate:\n" + served.getStdout());
        }
    }

    private static String status() throws Exception {
        return k3s.execInContainer("sh", "-c", "for k in certificate certificaterequest order challenge; do "
                + "kubectl -n web get $k -o jsonpath='{range .items[*]}{.kind}/{.metadata.name}: {.status.state} "
                + "{.status.reason}{range .status.conditions[*]}{.type}={.status} {.message}{end}{\"\\n\"}{end}'; done; "
                + "echo; echo 'Warning events in web:'; kubectl -n web events --types=Warning --no-headers | tail -3")
                .getStdout().trim();
    }

    private static String kubectl(String... args) throws Exception {
        String[] command = Stream.concat(Stream.of("kubectl"), Arrays.stream(args)).toArray(String[]::new);
        ExecResult result = k3s.execInContainer(command);
        assertEquals(0, result.getExitCode(), String.join(" ", command) + " failed: " + result.getStderr());
        return result.getStdout().trim();
    }
}
