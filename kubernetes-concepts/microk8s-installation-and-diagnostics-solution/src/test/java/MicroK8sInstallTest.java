import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Needs Multipass (https://canonical.com/multipass): the tests are skipped without it. Takes 5 to 10 minutes. */
class MicroK8sInstallTest {

    static MicroK8sVm vm;
    static MicroK8sVm.Result install;
    static String readyRightAfter;

    @BeforeAll
    static void install() throws Exception {
        assumeTrue(MicroK8sVm.multipassInstalled(), "Multipass is not installed");
        vm = new MicroK8sVm().launch();
        vm.copy("install.sh");
        install = vm.sudo("bash /home/ubuntu/lab/install.sh");
        readyRightAfter = vm.sudo("microk8s kubectl get nodes -o jsonpath='{.items[0].status.conditions[?(@.type==\"Ready\")].status}'")
                .output();
    }

    @AfterAll
    static void shutdown() throws Exception {
        if (vm != null) {
            vm.close();
        }
    }

    @Test
    void installSucceeds() {
        assertEquals(0, install.exitCode(), "install.sh failed:\n" + install.output());
    }

    @Test
    void tracksAPinnedChannel() throws Exception {
        String snap = vm.exec("snap", "list", "microk8s").output();
        assertTrue(snap.contains("1.34/stable") && snap.contains("v1.34."), "microk8s should be 1.34 and track 1.34/stable:\n" + snap);
    }

    @Test
    void clusterIsReadyWhenTheScriptReturns() {
        assertEquals("True", readyRightAfter, "the node was not Ready yet when install.sh returned");
    }

    @Test
    void ubuntuRunsKubectlWithoutSudo() throws Exception {
        MicroK8sVm.Result nodes = vm.exec("microk8s", "kubectl", "get", "nodes");
        assertEquals(0, nodes.exitCode(), "as ubuntu:\n" + nodes.output());
    }

    @Test
    void diagnosticsReportIsCollected() throws Exception {
        MicroK8sVm.Result list = vm.exec("tar", "tzf", "/home/ubuntu/diagnostics.tar.gz");
        assertEquals(0, list.exitCode(), "no readable /home/ubuntu/diagnostics.tar.gz:\n" + list.output());
        assertTrue(list.output().contains("snap.microk8s.daemon-kubelite"), "not an inspection report:\n" + list.output());
    }
}
