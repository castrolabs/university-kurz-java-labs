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

class RestoreDrillTest {

    static final K3sContainer k3s = new K3sContainer(DockerImageName.parse("rancher/k3s:v1.36.4-k3s1"));
    static String backupPhase;
    static String restorePhase;
    static String productionPod;
    static String marker;

    @BeforeAll
    static void drill() throws Exception {
        k3s.start();
        for (String f : new String[] {"k8s/storage.yaml", "k8s/velero.yaml", "k8s/shop.yaml", "k8s/backup.yaml",
                "k8s/restore.yaml"}) {
            k3s.copyFileToContainer(MountableFile.forClasspathResource(f), "/lab/" + f);
        }
        kubectl("apply", "-f", "/lab/k8s/storage.yaml");
        kubectl("apply", "-f", "/lab/k8s/shop.yaml");
        kubectl("-n", "storage", "rollout", "status", "deploy/s3", "--timeout=180s");
        for (int i = 0; i < 20 && !k3s.execInContainer("kubectl", "-n", "storage", "exec", "deploy/s3", "--", "sh", "-c",
                "echo 's3.bucket.create -name velero' | weed shell").getStdout().contains("created bucket"); i++) {
            Thread.sleep(3000);
        }
        kubectl("apply", "-f", "/lab/k8s/velero.yaml");
        for (int i = 0; i < 60 && k3s.execInContainer("kubectl", "-n", "velero", "get", "deploy", "velero")
                .getExitCode() != 0; i++) {
            Thread.sleep(3000);   // the Helm controller installs the chart
        }
        kubectl("-n", "velero", "rollout", "status", "deploy/velero", "--timeout=240s");
        kubectl("-n", "velero", "rollout", "status", "ds/node-agent", "--timeout=240s");
        waitFor("backupstoragelocation/default", "Available");

        kubectl("-n", "shop", "rollout", "status", "deploy/db", "--timeout=120s");
        marker = "order-" + System.nanoTime();
        kubectl("-n", "shop", "exec", "deploy/db", "--", "sh", "-c", "echo " + marker + " >> /data/orders");
        productionPod = kubectl("-n", "shop", "get", "pods", "-l", "app=db", "-o", "jsonpath={.items[0].metadata.uid}");

        kubectl("apply", "-f", "/lab/k8s/backup.yaml");
        backupPhase = waitFor("backup/shop-backup", "Completed", "PartiallyFailed", "Failed");
        kubectl("apply", "-f", "/lab/k8s/restore.yaml");
        restorePhase = waitFor("restore/shop-drill", "Completed", "PartiallyFailed", "Failed");
    }

    private static String waitFor(String object, String... phases) throws Exception {
        String phase = "";
        for (int i = 0; i < 80; i++) {
            phase = k3s.execInContainer("kubectl", "-n", "velero", "get", object, "-o", "jsonpath={.status.phase}")
                    .getStdout().trim();
            if (Arrays.asList(phases).contains(phase)) {
                break;
            }
            Thread.sleep(3000);
        }
        return phase;
    }

    @AfterAll
    static void shutdown() {
        k3s.stop();
    }

    @Test
    void backupIncludesTheVolumeData() throws Exception {
        assertEquals("Completed", backupPhase);
        String volumes = kubectl("-n", "velero", "get", "podvolumebackups", "-l", "velero.io/backup-name=shop-backup",
                "-o", "jsonpath={range .items[*]}{.spec.volume} {.status.phase}{\"\\n\"}{end}");
        assertTrue(volumes.contains("data Completed"), "the backup holds no volume data, only object definitions. "
                + "Pod volume backups: [" + volumes + "]");
    }

    @Test
    void drillRestoresTheDataNextToProduction() throws Exception {
        assertTrue(restorePhase.equals("Completed") || restorePhase.equals("PartiallyFailed"), "restore: " + restorePhase);
        ExecResult ready = k3s.execInContainer("kubectl", "-n", "shop-drill", "rollout", "status", "deploy/db",
                "--timeout=120s");
        assertEquals(0, ready.getExitCode(), "no restored database in shop-drill: " + ready.getStderr());
        assertTrue(kubectl("-n", "shop-drill", "exec", "deploy/db", "--", "cat", "/data/orders").contains(marker),
                "the restored database in shop-drill does not have the data written before the backup");
    }

    @Test
    void productionIsUntouched() throws Exception {
        assertEquals(productionPod, kubectl("-n", "shop", "get", "pods", "-l", "app=db", "-o",
                "jsonpath={.items[0].metadata.uid}"));
        assertTrue(kubectl("-n", "shop", "exec", "deploy/db", "--", "cat", "/data/orders").contains(marker));
    }

    private static String kubectl(String... args) throws Exception {
        String[] command = Stream.concat(Stream.of("kubectl"), Arrays.stream(args)).toArray(String[]::new);
        ExecResult result = k3s.execInContainer(command);
        assertEquals(0, result.getExitCode(), String.join(" ", command) + " failed: " + result.getStderr());
        return result.getStdout().trim();
    }
}
