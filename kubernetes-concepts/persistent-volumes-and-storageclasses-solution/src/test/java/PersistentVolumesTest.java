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

class PersistentVolumesTest {

    static final K3sContainer k3s = new K3sContainer(DockerImageName.parse("rancher/k3s:v1.36.4-k3s1"));
    static boolean running;
    static String events = "";
    static String linesAfterRestart = "";
    static String pvPhaseAfterPvcDeletion = "";

    @BeforeAll
    static void deploy() throws Exception {
        k3s.start();
        k3s.copyFileToContainer(MountableFile.forClasspathResource("k8s/storage.yaml"), "/lab/k8s/storage.yaml");
        kubectl("create", "namespace", "example");
        kubectl("apply", "-f", "/lab/k8s/storage.yaml");
        running = k3s.execInContainer("kubectl", "-n", "example", "rollout", "status", "deploy/notes",
                "--timeout=90s").getExitCode() == 0;
        if (!running) {
            events = kubectl("-n", "example", "get", "events", "-o",
                    "jsonpath={range .items[?(@.type==\"Warning\")]}{.message}{\"\\n\"}{end}");
            return;
        }
        String pv = kubectl("-n", "example", "get", "pvc", "notes-data", "-o", "jsonpath={.spec.volumeName}");

        // A Pod restart: the Deployment creates a new Pod, which appends a second line.
        kubectl("-n", "example", "delete", "pod", "-l", "app=notes", "--wait=true");
        kubectl("-n", "example", "rollout", "status", "deploy/notes", "--timeout=90s");
        Thread.sleep(3000);
        linesAfterRestart = kubectl("-n", "example", "exec", "deploy/notes", "--", "sh", "-c",
                "wc -l < /data/notes.txt");

        // An accidental claim deletion.
        kubectl("-n", "example", "scale", "deploy/notes", "--replicas=0");
        kubectl("-n", "example", "wait", "--for=delete", "pod", "-l", "app=notes", "--timeout=60s");
        kubectl("-n", "example", "delete", "pvc", "notes-data", "--wait=true");
        Thread.sleep(10_000);
        ExecResult phase = k3s.execInContainer("kubectl", "get", "pv", pv, "-o", "jsonpath={.status.phase}");
        pvPhaseAfterPvcDeletion = phase.getExitCode() == 0 ? phase.getStdout().trim() : "<deleted>";
    }

    @AfterAll
    static void shutdown() {
        k3s.stop();
    }

    @Test
    void claimBindsAndThePodRuns() {
        assertTrue(running, "deploy/notes never became available. Warning events:\n" + events);
    }

    @Test
    void dataSurvivesAPodRestart() {
        assertTrue(running, "deploy/notes never became available");
        assertEquals("2", linesAfterRestart,
                "/data/notes.txt should hold one line per start; the file lives outside the volume");
    }

    @Test
    void volumeSurvivesDeletingTheClaim() {
        assertTrue(running, "deploy/notes never became available");
        assertEquals("Released", pvPhaseAfterPvcDeletion,
                "the PersistentVolume (and its data) was deleted together with the claim");
    }

    private static String kubectl(String... args) throws Exception {
        String[] command = Stream.concat(Stream.of("kubectl"), Arrays.stream(args)).toArray(String[]::new);
        ExecResult result = k3s.execInContainer(command);
        assertEquals(0, result.getExitCode(), String.join(" ", command) + " failed: " + result.getStderr());
        return result.getStdout().trim();
    }
}
