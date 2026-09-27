import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.k3s.K3sContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

class ImagesTest {

    static final String RELEASE =
            "registry.lab/mirror/busybox@sha256:bdf57e528e45e4433820e045b29b4597825a1c9e38353532d90a01445013f82e";

    // Only kubectl's built-in kustomize is needed; the k3s image provides it.
    static final K3sContainer k3s = new K3sContainer(DockerImageName.parse("rancher/k3s:v1.36.4-k3s1"));
    static ExecResult prod;
    static Set<String> images = new TreeSet<>();

    @BeforeAll
    static void render() throws Exception {
        k3s.start();
        k3s.copyFileToContainer(MountableFile.forClasspathResource("kustomize"), "/lab/kustomize");
        prod = k3s.execInContainer("kubectl", "kustomize", "/lab/kustomize/overlays/prod");
        Matcher image = Pattern.compile("image: \"?([^\"\\s]+)").matcher(prod.getStdout());
        while (image.find()) {
            images.add(image.group(1));
        }
    }

    @AfterAll
    static void shutdown() {
        k3s.stop();
    }

    @Test
    void everyRoleRunsTheSameImage() {
        assertEquals(0, prod.getExitCode(), "kubectl kustomize overlays/prod failed:\n" + prod.getStderr());
        assertEquals(1, images.size(), "the roles run different images: " + images);
    }

    @Test
    void prodRunsTheReleaseFromTheMirror() {
        assertEquals(Set.of(RELEASE), images);
    }

    @Test
    void rolesStillDifferByTheirArguments() {
        for (String role : new String[] {"role=api", "role=worker", "role=migrate"}) {
            assertEquals(true, prod.getStdout().contains(role), "missing " + role);
        }
    }
}
