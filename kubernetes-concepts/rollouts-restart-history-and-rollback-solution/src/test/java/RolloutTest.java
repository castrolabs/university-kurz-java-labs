import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Arrays;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.k3s.K3sContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

class RolloutTest {

    static final K3sContainer k3s = new K3sContainer(DockerImageName.parse("rancher/k3s:v1.36.4-k3s1"));
    static ExecResult script;
    static String historyBefore;

    @BeforeAll
    static void run() throws Exception {
        k3s.start();
        k3s.copyFileToContainer(MountableFile.forClasspathResource("k8s/web.yaml"), "/lab/k8s/web.yaml");
        k3s.copyFileToContainer(MountableFile.forClasspathResource("rollback.sh"), "/lab/rollback.sh");
        kubectl("apply", "-f", "/lab/k8s/web.yaml");
        kubectl("rollout", "status", "deploy/web", "--timeout=90s");

        release("v2: new checkout page", "set", "env", "deploy/web", "VERSION=v2");
        kubectl("rollout", "status", "deploy/web", "--timeout=60s");
        release("v3: base image update", "set", "image", "deploy/web", "web=busybox:0.0-does-not-exist");
        Thread.sleep(3000);

        kubectl("patch", "configmap", "web-config", "-p", "{\"data\":{\"GREETING\":\"bonjour\"}}");
        historyBefore = kubectl("rollout", "history", "deploy/web");
        script = k3s.execInContainer("sh", "/lab/rollback.sh");
        k3s.execInContainer("kubectl", "rollout", "status", "deploy/web", "--timeout=60s");
    }

    private static void release(String cause, String... change) throws Exception {
        kubectl(change);
        // After the change: the Deployment controller copies it onto the ReplicaSet it currently runs.
        kubectl("annotate", "deploy/web", "kubernetes.io/change-cause=" + cause, "--overwrite");
    }

    @AfterAll
    static void shutdown() {
        k3s.stop();
    }

    @Test
    void scriptSucceeds() {
        assertEquals(0, script.getExitCode(), "rollback.sh failed:\n" + script.getStdout() + script.getStderr());
    }

    @Test
    void everyPodServesTheLastGoodVersionWithTheNewConfig() throws Exception {
        String pods = "";
        for (int i = 0; i < 30; i++) {   // wait for replaced Pods to finish terminating
            pods = kubectl("get", "pods", "-l", "app=web", "-o", "name");
            if (pods.lines().count() == 2) {
                break;
            }
            Thread.sleep(1000);
        }
        assertEquals(2, pods.lines().count(), "expected exactly 2 web Pods: " + pods);
        for (String pod : pods.lines().toList()) {
            assertEquals("v1 bonjour", kubectl("exec", pod, "--", "wget", "-qO-", "http://127.0.0.1:8080"),
                    pod + " serves the wrong version or config. History before your script:\n" + historyBefore);
        }
    }

    @Test
    void rollbackAndRestartAreNewRevisions() throws Exception {
        String history = kubectl("rollout", "history", "deploy/web");
        assertEquals("5", kubectl("get", "deploy", "web", "-o",
                "jsonpath={.metadata.annotations.deployment\\.kubernetes\\.io/revision}"),
                "expected one revision for the rollback and one for the restart:\n" + history);
        assertFalse(history.lines().anyMatch(l -> l.startsWith("1 ")),
                "rolling back to a revision moves it to the end of the history:\n" + history);
    }

    private static String kubectl(String... args) throws Exception {
        String[] command = Stream.concat(Stream.of("kubectl"), Arrays.stream(args)).toArray(String[]::new);
        ExecResult result = k3s.execInContainer(command);
        assertEquals(0, result.getExitCode(), String.join(" ", command) + " failed: " + result.getStderr());
        return result.getStdout().trim();
    }
}
