import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Needs Multipass (https://canonical.com/multipass): the tests are skipped without it. Takes 5 to 10 minutes. */
class MicroK8sAddonsTest {

    static MicroK8sVm vm;
    static MicroK8sVm.Result addons;

    @BeforeAll
    static void setUp() throws Exception {
        assumeTrue(MicroK8sVm.multipassInstalled(), "Multipass is not installed");
        vm = new MicroK8sVm().launch();
        MicroK8sVm.Result install = vm.sudo("snap install microk8s --classic --channel=1.35/stable "
                + "&& microk8s status --wait-ready --timeout 600");
        assertEquals(0, install.exitCode(), install.output());
        for (String f : new String[] {"addons.sh", "storageclass.yaml", "db.yaml"}) {
            vm.copy(f);
        }
        addons = vm.sudo("bash /home/ubuntu/lab/addons.sh");
        vm.sudo("microk8s kubectl -n kube-system rollout status deploy/coredns --timeout=180s; "
                + "microk8s kubectl -n kube-system rollout status deploy/hostpath-provisioner --timeout=180s");
    }

    @AfterAll
    static void shutdown() throws Exception {
        if (vm != null) {
            vm.close();
        }
    }

    @Test
    void scriptSucceeds() {
        assertEquals(0, addons.exitCode(), "addons.sh failed:\n" + addons.output());
    }

    @Test
    void coreDnsForwardsToTheChosenResolvers() throws Exception {
        String corefile = kubectl("-n kube-system get configmap coredns -o jsonpath='{.data.Corefile}'");
        assertTrue(corefile.replaceAll("[ \\t]+", " ").contains("forward . 1.1.1.1 8.8.8.8"),
                "CoreDNS still forwards elsewhere:\n" + corefile);
        MicroK8sVm.Result lookup = vm.sudo("microk8s kubectl run dnstest --rm -i --restart=Never --image=busybox:1.37 "
                + "-- nslookup example.com");
        assertTrue(lookup.output().contains("Address"), "external names do not resolve from a Pod:\n" + lookup.output());
    }

    @Test
    void thereIsExactlyOneDefaultStorageClass() throws Exception {
        String defaults = kubectl("get storageclass -o jsonpath='{range .items[?(@.metadata.annotations.storageclass\\.kubernetes\\.io/is-default-class==\"true\")]}{.metadata.name} {end}'");
        assertEquals("microk8s-hostpath", defaults.trim(), "default StorageClasses: " + defaults);
    }

    @Test
    void databaseDataSurvivesDeletingItsClaim() throws Exception {
        kubectl("apply -f /home/ubuntu/lab/db.yaml");
        MicroK8sVm.Result ready = vm.sudo("microk8s kubectl wait --for=condition=Ready pod/db --timeout=180s");
        assertEquals(0, ready.exitCode(), ready.output());
        String pv = kubectl("get pvc db-data -o jsonpath='{.spec.volumeName}'");
        String path = kubectl("get pv " + pv + " -o jsonpath='{.spec.hostPath.path}'");
        assertTrue(path.startsWith("/mnt/db-volumes/"), "the volume is not under /mnt/db-volumes: " + path);

        kubectl("delete pod db --wait=true");
        kubectl("delete pvc db-data --wait=true");
        Thread.sleep(10000);
        assertEquals("Released", kubectl("get pv " + pv + " -o jsonpath='{.status.phase}'"), "the PersistentVolume is gone");
        assertEquals("keep-me", vm.sudo("cat " + path + "/marker").output(), "the data was deleted with the claim");
    }

    private static String kubectl(String args) throws Exception {
        MicroK8sVm.Result r = vm.sudo("microk8s kubectl " + args);
        assertEquals(0, r.exitCode(), "kubectl " + args + " failed:\n" + r.output());
        return r.output().trim();
    }
}
