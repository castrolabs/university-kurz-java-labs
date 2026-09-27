import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

class DatabaseBackupTest {

    static final K3sContainer k3s = new K3sContainer(DockerImageName.parse("rancher/k3s:v1.36.4-k3s1"));
    static String healthyRun;
    static String healthyRunLogs;
    static String insertStatementsInDumps;
    static String runWithDatabaseDown;

    @BeforeAll
    static void deploy() throws Exception {
        k3s.start();
        k3s.copyFileToContainer(MountableFile.forClasspathResource("k8s/db.yaml"), "/lab/k8s/db.yaml");
        k3s.copyFileToContainer(MountableFile.forClasspathResource("k8s/backup.yaml"), "/lab/k8s/backup.yaml");
        kubectl("create", "namespace", "example");
        kubectl("-n", "example", "create", "secret", "generic", "db-root", "--from-literal=password=root-pass");
        kubectl("apply", "-f", "/lab/k8s/db.yaml");
        kubectl("-n", "example", "rollout", "status", "deploy/db", "--timeout=240s");
        kubectl("-n", "example", "exec", "deploy/db", "--", "sh", "-c",
                "mariadb -uroot -p\"$MARIADB_ROOT_PASSWORD\" shop -e "
                        + "\"create table orders(id int primary key); insert into orders values (1),(2),(3);\"");
        kubectl("apply", "-f", "/lab/k8s/backup.yaml");

        // Run 1: the database is up.
        kubectl("-n", "example", "create", "job", "--from=cronjob/db-backup", "healthy-run");
        healthyRun = waitForJob("healthy-run");
        healthyRunLogs = k3s.execInContainer("kubectl", "-n", "example", "logs", "job/healthy-run").getStdout().trim();
        kubectl("-n", "example", "run", "reader", "--image=busybox:1.37", "--overrides",
                "{\"spec\":{\"containers\":[{\"name\":\"reader\",\"image\":\"busybox:1.37\",\"command\":[\"sleep\",\"3600\"],"
                        + "\"volumeMounts\":[{\"name\":\"b\",\"mountPath\":\"/backup\"}]}],"
                        + "\"volumes\":[{\"name\":\"b\",\"persistentVolumeClaim\":{\"claimName\":\"db-backups\"}}]}}");
        kubectl("-n", "example", "wait", "--for=condition=Ready", "pod/reader", "--timeout=60s");
        insertStatementsInDumps = kubectl("-n", "example", "exec", "reader", "--", "sh", "-c",
                "cat /backup/*.sql.gz 2>/dev/null | gzip -dc 2>/dev/null | grep -c 'INSERT INTO' || true");

        // Run 2: the database is down.
        kubectl("-n", "example", "scale", "deploy/db", "--replicas=0");
        kubectl("-n", "example", "wait", "--for=delete", "pod", "-l", "app=db", "--timeout=60s");
        kubectl("-n", "example", "create", "job", "--from=cronjob/db-backup", "down-run");
        runWithDatabaseDown = waitForJob("down-run");
    }

    @AfterAll
    static void shutdown() {
        k3s.stop();
    }

    @Test
    void backupCompletesWhileTheDatabaseIsUp() {
        assertEquals("Complete", healthyRun, "job/healthy-run: " + healthyRunLogs);
    }

    @Test
    void backupContainsTheData() {
        assertFalse(insertStatementsInDumps.isEmpty() || insertStatementsInDumps.equals("0"),
                "no INSERT INTO statements found in /backup/*.sql.gz");
    }

    @Test
    void backupFailsWhenTheDatabaseIsDown() {
        assertEquals("Failed", runWithDatabaseDown,
                "the dump failed but the Job did not: a monitoring system would think last night's backup worked");
    }

    @Test
    void passwordIsNotInTheManifest() throws Exception {
        String container = kubectl("get", "cronjob", "-n", "example", "db-backup", "-o",
                "jsonpath={.spec.jobTemplate.spec.template.spec.containers[0]}");
        assertFalse(container.contains("root-pass"), "the password is still written in the CronJob");
        assertTrue(container.contains("\"secretKeyRef\""), "read the password from the Secret db-root");
    }

    @Test
    void backupsNeverOverlap() throws Exception {
        assertEquals("Forbid", kubectl("-n", "example", "get", "cronjob", "db-backup", "-o",
                "jsonpath={.spec.concurrencyPolicy}"));
    }

    private static String waitForJob(String job) throws Exception {
        for (int i = 0; i < 60; i++) {
            String conditions = kubectl("-n", "example", "get", "job", job, "-o",
                    "jsonpath={range .status.conditions[?(@.status==\"True\")]}{.type} {end}");
            if (conditions.contains("Complete")) {
                return "Complete";
            }
            if (conditions.contains("Failed")) {
                return "Failed";
            }
            Thread.sleep(3000);
        }
        return "still running";
    }

    private static String kubectl(String... args) throws Exception {
        String[] command = Stream.concat(Stream.of("kubectl"), Arrays.stream(args)).toArray(String[]::new);
        ExecResult result = k3s.execInContainer(command);
        assertEquals(0, result.getExitCode(), String.join(" ", command) + " failed: " + result.getStderr());
        return result.getStdout().trim();
    }
}
