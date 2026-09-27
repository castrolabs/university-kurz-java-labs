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

class JobsTest {

    static final K3sContainer k3s = new K3sContainer(DockerImageName.parse("rancher/k3s:v1.36.4-k3s1"));
    static String applyErrors;
    static String migrate = "";
    static String flakyImport = "";
    static String report = "";
    static boolean cleanupCompleted;
    static boolean cleanupDeleted;

    @BeforeAll
    static void deploy() throws Exception {
        k3s.start();
        k3s.copyFileToContainer(MountableFile.forClasspathResource("k8s/jobs.yaml"), "/lab/k8s/jobs.yaml");
        kubectl("create", "namespace", "example");
        applyErrors = k3s.execInContainer("kubectl", "apply", "-f", "/lab/k8s/jobs.yaml").getStderr().trim();

        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < 90_000) {
            migrate = condition("migrate");
            flakyImport = condition("flaky-import");
            report = condition("report");
            String cleanup = condition("cleanup");
            cleanupCompleted |= cleanup.contains("Complete");
            cleanupDeleted |= cleanupCompleted && cleanup.equals("<deleted>");
            if (migrate.contains("Complete") && flakyImport.contains("Failed") && report.contains("Failed")
                    && cleanupDeleted) {
                break;
            }
            Thread.sleep(2000);
        }
    }

    @AfterAll
    static void shutdown() {
        k3s.stop();
    }

    @Test
    void migrateIsAcceptedAndCompletes() {
        assertTrue(migrate.contains("Complete"), "job/migrate: " + (migrate.equals("<deleted>") ? "not created: " + applyErrors : migrate));
    }

    @Test
    void flakyImportFailsAfterTwoAttempts() throws Exception {
        assertTrue(flakyImport.contains("Failed/BackoffLimitExceeded"),
                "job/flaky-import was not declared failed within 90 s: " + flakyImport);
        assertEquals("2", kubectl("-n", "example", "get", "job", "flaky-import", "-o", "jsonpath={.status.failed}"),
                "expected exactly 2 failed attempts");
    }

    @Test
    void hungReportIsStoppedByItsDeadline() {
        assertTrue(report.contains("Failed/DeadlineExceeded"), "job/report is still running: " + report);
    }

    @Test
    void finishedCleanupJobIsDeleted() {
        assertTrue(cleanupCompleted, "job/cleanup never completed");
        assertTrue(cleanupDeleted, "job/cleanup still exists after it finished");
    }

    // "<type>/<reason>" of the Job's conditions, "<deleted>" if the Job does not exist.
    private static String condition(String job) throws Exception {
        ExecResult result = k3s.execInContainer("kubectl", "-n", "example", "get", "job", job, "-o",
                "jsonpath={range .status.conditions[?(@.status==\"True\")]}{.type}/{.reason} {end}");
        return result.getExitCode() == 0 ? result.getStdout().trim() : "<deleted>";
    }

    private static String kubectl(String... args) throws Exception {
        String[] command = Stream.concat(Stream.of("kubectl"), Arrays.stream(args)).toArray(String[]::new);
        ExecResult result = k3s.execInContainer(command);
        assertEquals(0, result.getExitCode(), String.join(" ", command) + " failed: " + result.getStderr());
        return result.getStdout().trim();
    }
}
