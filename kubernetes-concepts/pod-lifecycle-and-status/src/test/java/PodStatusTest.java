import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.k3s.K3sContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

class PodStatusTest {

    static final K3sContainer k3s = new K3sContainer(DockerImageName.parse("rancher/k3s:v1.36.4-k3s1"));

    static final Map<String, String> EXPECTED = new TreeMap<>(Map.of(
            "ok", "ready",
            "warming-up", "not-ready",
            "too-big", "unschedulable",
            "bad-image", "image-pull",
            "crashing", "crash-loop",
            "batch-done", "succeeded",
            "batch-failed", "failed"));

    @BeforeAll
    static void deploy() throws Exception {
        k3s.start();
        k3s.copyFileToContainer(MountableFile.forClasspathResource("k8s/pods.yaml"), "/lab/k8s/pods.yaml");
        k3s.copyFileToContainer(MountableFile.forClasspathResource("classify.sh"), "/lab/classify.sh");
        kubectl("create", "namespace", "lab");
        for (int i = 0; i < 30 && k3s.execInContainer("kubectl", "-n", "lab", "get", "sa", "default").getExitCode() != 0; i++) {
            Thread.sleep(1000);
        }
        kubectl("apply", "-f", "/lab/k8s/pods.yaml");
        kubectl("-n", "lab", "wait", "--for=condition=Ready", "pod/ok", "--timeout=90s");
        kubectl("-n", "lab", "wait", "--for=jsonpath={.status.phase}=Succeeded", "pod/batch-done", "--timeout=60s");
        kubectl("-n", "lab", "wait", "--for=jsonpath={.status.phase}=Failed", "pod/batch-failed", "--timeout=60s");
        // A crash-looping container is Running (and even Ready) for an instant at every restart. After the
        // third restart the back-off is 40 s, longer than the runs below.
        kubectl("-n", "lab", "wait", "--for=jsonpath={.status.containerStatuses[0].restartCount}=3", "pod/crashing",
                "--timeout=120s");
        kubectl("-n", "lab", "wait", "--for=condition=Ready=false", "pod/crashing", "--timeout=30s");
    }

    @AfterAll
    static void shutdown() {
        k3s.stop();
    }

    @Test
    void classifiesEveryPodTheSameWayEachTime() throws Exception {
        for (int run = 1; run <= 4; run++) {   // a few seconds apart: the STATUS column changes in between
            ExecResult result = k3s.execInContainer("sh", "/lab/classify.sh", "lab");
            assertEquals(0, result.getExitCode(), "classify.sh failed: " + result.getStderr());
            Map<String, String> actual = result.getStdout().strip().lines()
                    .map(l -> l.strip().split("\\s+"))
                    .collect(Collectors.toMap(p -> p[0], p -> p.length > 1 ? p[1] : "", (a, b) -> a, TreeMap::new));
            assertEquals(EXPECTED, actual, "run " + run + ", kubectl get pods showed:\n"
                    + kubectl("-n", "lab", "get", "pods"));
            Thread.sleep(3000);
        }
    }

    private static String kubectl(String... args) throws Exception {
        String[] command = Stream.concat(Stream.of("kubectl"), Arrays.stream(args)).toArray(String[]::new);
        ExecResult result = k3s.execInContainer(command);
        assertEquals(0, result.getExitCode(), String.join(" ", command) + " failed: " + result.getStderr());
        return result.getStdout().trim();
    }
}
