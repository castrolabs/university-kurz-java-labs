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

class EventsTest {

    static final K3sContainer k3s = new K3sContainer(DockerImageName.parse("rancher/k3s:v1.36.4-k3s1"));
    static ExecResult script;
    static String allEvents;

    @BeforeAll
    static void run() throws Exception {
        k3s.start();
        for (String f : new String[] {"k8s/migrate.yaml", "k8s/report.yaml"}) {
            k3s.copyFileToContainer(MountableFile.forClasspathResource(f), "/lab/" + f);
        }
        k3s.copyFileToContainer(MountableFile.forClasspathResource("latest-warning.sh"), "/lab/latest-warning.sh");
        kubectl("create", "namespace", "shop");
        waitFor("serviceaccount/default", "-n", "shop");
        kubectl("apply", "-f", "/lab/k8s/migrate.yaml");
        kubectl("-n", "shop", "wait", "--for=condition=Failed", "job/migrate", "--timeout=90s");
        Thread.sleep(5000);
        kubectl("apply", "-f", "/lab/k8s/report.yaml");
        Thread.sleep(5000);
        allEvents = kubectl("-n", "shop", "get", "events", "--field-selector", "type=Warning", "-o",
                "custom-columns=REASON:.reason,OBJECT:.involvedObject.name,LAST_TIMESTAMP:.lastTimestamp,EVENT_TIME:.eventTime");
        script = k3s.execInContainer("sh", "/lab/latest-warning.sh", "shop");
    }

    private static void waitFor(String object, String... ns) throws Exception {
        for (int i = 0; i < 30 && k3s.execInContainer(Stream.concat(Stream.of("kubectl", "get", object),
                Arrays.stream(ns)).toArray(String[]::new)).getExitCode() != 0; i++) {
            Thread.sleep(1000);
        }
    }

    @AfterAll
    static void shutdown() {
        k3s.stop();
    }

    @Test
    void scriptSucceeds() {
        assertEquals(0, script.getExitCode(), "latest-warning.sh failed:\n" + script.getStderr());
    }

    @Test
    void printsExactlyOneLine() {
        assertEquals(1, script.getStdout().strip().lines().count(), "output:\n" + script.getStdout());
    }

    @Test
    void printsTheMostRecentWarning() {
        String line = script.getStdout().strip();
        assertTrue(line.contains("FailedScheduling") && line.toLowerCase().contains("pod/report")
                        && line.contains("Insufficient memory"),
                "expected the scheduler's warning about pod/report, got:\n" + line
                        + "\n\nAll warnings in shop, with their timestamps:\n" + allEvents);
    }

    private static String kubectl(String... args) throws Exception {
        String[] command = Stream.concat(Stream.of("kubectl"), Arrays.stream(args)).toArray(String[]::new);
        ExecResult result = k3s.execInContainer(command);
        assertEquals(0, result.getExitCode(), String.join(" ", command) + " failed: " + result.getStderr());
        return result.getStdout().trim();
    }
}
