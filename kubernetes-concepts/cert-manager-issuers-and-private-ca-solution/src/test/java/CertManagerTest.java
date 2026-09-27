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

class CertManagerTest {

    static final String CERT_MANAGER = "https://github.com/cert-manager/cert-manager/releases/download/v1.21.2/cert-manager.yaml";

    // Testcontainers starts k3s with --disable=traefik; this command keeps the bundled Traefik.
    static final K3sContainer k3s = new K3sContainer(DockerImageName.parse("rancher/k3s:v1.36.4-k3s1"))
            .withCommand("server");
    static ExecResult applyTls;
    static boolean certificateReady;

    @BeforeAll
    static void deploy() throws Exception {
        k3s.start();
        for (String f : new String[] {"k8s/apps.yaml", "k8s/tls.yaml"}) {
            k3s.copyFileToContainer(MountableFile.forClasspathResource(f), "/lab/" + f);
        }
        kubectl("apply", "-f", CERT_MANAGER);
        for (String d : new String[] {"cert-manager", "cert-manager-cainjector", "cert-manager-webhook"}) {
            kubectl("-n", "cert-manager", "rollout", "status", "deploy/" + d, "--timeout=180s");
        }
        kubectl("apply", "-f", "/lab/k8s/apps.yaml");
        kubectl("-n", "web", "rollout", "status", "deploy/shop", "--timeout=90s");
        kubectl("wait", "--for=condition=Ready", "pod/client", "--timeout=120s");
        for (int i = 0; i < 20; i++) {   // the webhook answers a little after its Pod is ready
            applyTls = k3s.execInContainer("kubectl", "apply", "-f", "/lab/k8s/tls.yaml");
            if (applyTls.getExitCode() == 0 || !applyTls.getStderr().contains("webhook")) {
                break;
            }
            Thread.sleep(3000);
        }
        certificateReady = k3s.execInContainer("kubectl", "-n", "web", "wait", "--for=condition=Ready",
                "certificate/shop", "--timeout=60s").getExitCode() == 0;
    }

    @AfterAll
    static void shutdown() {
        k3s.stop();
    }

    @Test
    void certificateIsIssued() throws Exception {
        assertEquals(0, applyTls.getExitCode(), "kubectl apply failed: " + applyTls.getStderr());
        assertTrue(certificateReady, "certificate/shop is not Ready.\nIssuers:\n" + conditions("issuer,clusterissuer")
                + "\nCertificate:\n" + conditions("certificate"));
    }

    @Test
    void httpsIsTrustedWithThePrivateCa() throws Exception {
        String traefik = kubectl("-n", "kube-system", "get", "svc", "traefik", "-o", "jsonpath={.spec.clusterIP}");
        ExecResult response = null;
        for (int i = 0; i < 10; i++) {   // Traefik picks up new certificates within a few seconds
            response = k3s.execInContainer("kubectl", "exec", "client", "--", "curl", "-sS", "--max-time", "5",
                    "--cacert", "/ca/ca.crt", "--resolve", "shop.lab:443:" + traefik, "https://shop.lab/");
            if (response.getExitCode() == 0) {
                break;
            }
            Thread.sleep(3000);
        }
        if (response.getExitCode() != 0) {
            String served = k3s.execInContainer("kubectl", "exec", "client", "--", "sh", "-c", "curl -skv -o /dev/null"
                    + " --resolve shop.lab:443:" + traefik + " https://shop.lab/ 2>&1 | grep -E 'subject:|issuer:|subjectAltName'")
                    .getStdout().trim();
            assertEquals(0, response.getExitCode(), "https://shop.lab failed: " + response.getStderr().lines().findFirst()
                    .orElse("") + "\nCertificate served by Traefik:\n" + served);
        }
        assertEquals("shop", response.getStdout().trim());
    }

    private static String conditions(String kinds) throws Exception {
        return k3s.execInContainer("kubectl", "get", kinds, "-A", "-o",
                "jsonpath={range .items[*]}{.kind}/{.metadata.name}: {range .status.conditions[*]}{.type}={.status} {.message}{end}{\"\\n\"}{end}")
                .getStdout().trim();
    }

    private static String kubectl(String... args) throws Exception {
        String[] command = Stream.concat(Stream.of("kubectl"), Arrays.stream(args)).toArray(String[]::new);
        ExecResult result = k3s.execInContainer(command);
        assertEquals(0, result.getExitCode(), String.join(" ", command) + " failed: " + result.getStderr());
        return result.getStdout().trim();
    }
}
