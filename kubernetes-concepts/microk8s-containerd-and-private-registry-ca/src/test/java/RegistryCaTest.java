import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Needs Multipass (https://canonical.com/multipass): the tests are skipped without it. Takes 5 to 10 minutes. */
class RegistryCaTest {

    static MicroK8sVm vm;
    static MicroK8sVm.Result trust;
    static MicroK8sVm.Result appReady;

    @BeforeAll
    static void setUp() throws Exception {
        assumeTrue(MicroK8sVm.multipassInstalled(), "Multipass is not installed");
        vm = new MicroK8sVm().launch();
        MicroK8sVm.Result install = vm.sudo("echo '127.0.0.1 registry.lab' >> /etc/hosts "
                + "&& snap install microk8s --classic --channel=1.35/stable && microk8s status --wait-ready --timeout 600");
        assertEquals(0, install.exitCode(), install.output());
        for (String f : new String[] {"registry.yaml", "app.yaml", "trust-registry.sh", "company-root-ca.crt"}) {
            vm.copy(f);
        }
        kubectl("apply -f /home/ubuntu/lab/registry.yaml");
        kubectl("-n registry wait --for=condition=Complete job/seed --timeout=300s");

        trust = vm.sudo("bash /home/ubuntu/lab/trust-registry.sh");
        kubectl("apply -f /home/ubuntu/lab/app.yaml");
        appReady = vm.sudo("microk8s kubectl wait --for=condition=Ready pod/app --timeout=90s");
    }

    @AfterAll
    static void shutdown() throws Exception {
        if (vm != null) {
            vm.close();
        }
    }

    @Test
    void scriptSucceeds() {
        assertEquals(0, trust.exitCode(), "trust-registry.sh failed:\n" + trust.output());
    }

    @Test
    void podPullsFromThePrivateRegistry() throws Exception {
        assertEquals(0, appReady.exitCode(), "pod/app did not start. Its latest pull error:\n"
                + vm.sudo("microk8s kubectl events --for pod/app --types=Warning --no-headers | grep 'Failed to pull' | tail -1").output());
    }

    @Test
    void tlsIsVerified() throws Exception {
        String config = vm.sudo("cat /var/snap/microk8s/current/args/certs.d/*/hosts.toml").output();
        assertFalse(config.contains("skip_verify = true"), "trust the CA instead of skipping verification");
    }

    private static String kubectl(String args) throws Exception {
        MicroK8sVm.Result r = vm.sudo("microk8s kubectl " + args);
        assertEquals(0, r.exitCode(), "kubectl " + args + " failed:\n" + r.output());
        return r.output().trim();
    }
}
