import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.k3s.K3sContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

class OverlaysTest {

    // Only kubectl's built-in kustomize is needed; the k3s image provides it.
    static final K3sContainer k3s = new K3sContainer(DockerImageName.parse("rancher/k3s:v1.36.4-k3s1"));
    static ExecResult prod;
    static ExecResult dev;

    @BeforeAll
    static void render() throws Exception {
        k3s.start();
        k3s.copyFileToContainer(MountableFile.forClasspathResource("kustomize"), "/lab/kustomize");
        // A new release changes the base. Every overlay must pick it up without being edited.
        k3s.execInContainer("sed", "-i", "s#busybox:1.37#busybox:1.38#", "/lab/kustomize/base/web.yaml");
        prod = k3s.execInContainer("kubectl", "kustomize", "/lab/kustomize/overlays/prod");
        dev = k3s.execInContainer("kubectl", "kustomize", "/lab/kustomize/overlays/dev");
    }

    @AfterAll
    static void shutdown() {
        k3s.stop();
    }

    @Test
    void prodRendersAsSpecified() {
        assertEquals(0, prod.getExitCode(), "kubectl kustomize overlays/prod failed:\n" + prod.getStderr());
        String yaml = prod.getStdout();
        assertTrue(yaml.contains("name: prod-web") && yaml.contains("namespace: shop-prod"), yaml);
        assertTrue(yaml.contains("replicas: 3"), "prod needs 3 replicas:\n" + yaml);
        assertTrue(yaml.contains("value: warn"), "prod needs LOG_LEVEL=warn:\n" + yaml);
        assertTrue(yaml.contains("prometheus.io/scrape: \"true\""), "prod must be scraped:\n" + yaml);
    }

    @Test
    void devRendersAsSpecified() {
        assertEquals(0, dev.getExitCode(), "kubectl kustomize overlays/dev failed:\n" + dev.getStderr());
        String yaml = dev.getStdout();
        assertTrue(yaml.contains("name: dev-web") && yaml.contains("namespace: shop-dev"), yaml);
        assertTrue(yaml.contains("replicas: 1") && yaml.contains("value: debug"), "dev: 1 replica, LOG_LEVEL=debug:\n" + yaml);
        assertFalse(yaml.contains("prometheus.io"), "dev must not be scraped:\n" + yaml);
    }

    @Test
    void everyOverlayFollowsTheBase() {
        assertTrue(prod.getStdout().contains("image: busybox:1.38"), "prod did not pick up the new base image");
        assertTrue(dev.getStdout().contains("image: busybox:1.38"), "dev did not pick up the new base image");
    }
}
