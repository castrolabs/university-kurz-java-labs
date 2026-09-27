import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Arrays;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.k3s.K3sContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

class ServiceAccountTokensTest {

    static final K3sContainer k3s = new K3sContainer(DockerImageName.parse("rancher/k3s:v1.36.4-k3s1"));
    static final String SA_DIR = "/var/run/secrets/kubernetes.io/serviceaccount";

    @BeforeAll
    static void deploy() throws Exception {
        k3s.start();
        k3s.copyFileToContainer(MountableFile.forClasspathResource("k8s/app.yaml"), "/lab/k8s/app.yaml");
        kubectl("create", "namespace", "team-a");
        kubectl("-n", "team-a", "create", "configmap", "settings", "--from-literal=mode=demo");
        kubectl("-n", "team-a", "create", "secret", "generic", "db", "--from-literal=password=s3cret");
        kubectl("apply", "-f", "/lab/k8s/app.yaml");
        kubectl("-n", "team-a", "wait", "--for=condition=Ready", "pod/web", "pod/watcher", "--timeout=120s");
    }

    @AfterAll
    static void shutdown() {
        k3s.stop();
    }

    @Test
    void webHasNoApiToken() throws Exception {
        ExecResult ls = k3s.execInContainer("kubectl", "-n", "team-a", "exec", "web", "--", "ls", SA_DIR);
        assertNotEquals(0, ls.getExitCode(), "web still has a token mounted at " + SA_DIR);
    }

    @Test
    void watcherCanListConfigMaps() throws Exception {
        assertEquals("200", apiStatusFromWatcher("/api/v1/namespaces/team-a/configmaps"));
    }

    @Test
    void watcherCannotReadSecrets() throws Exception {
        assertEquals("403", apiStatusFromWatcher("/api/v1/namespaces/team-a/secrets"),
                "the watcher's token can read Secrets");
    }

    @Test
    void watcherRunsAsItsOwnServiceAccount() throws Exception {
        assertEquals("config-watcher", kubectl("-n", "team-a", "get", "pod", "watcher", "-o",
                "jsonpath={.spec.serviceAccountName}"));
    }

    @Test
    void defaultServiceAccountHasNoPermissions() throws Exception {
        ExecResult canI = k3s.execInContainer("kubectl", "auth", "can-i", "list", "configmaps", "-n", "team-a",
                "--as=system:serviceaccount:team-a:default");
        assertEquals("no", canI.getStdout().trim(), "every Pod using the default ServiceAccount inherits its permissions");
    }

    private static String apiStatusFromWatcher(String path) throws Exception {
        return kubectl("-n", "team-a", "exec", "watcher", "--", "sh", "-c",
                "curl -s -o /dev/null -w '%{http_code}' --cacert " + SA_DIR + "/ca.crt"
                        + " -H \"Authorization: Bearer $(cat " + SA_DIR + "/token)\""
                        + " https://$KUBERNETES_SERVICE_HOST:$KUBERNETES_SERVICE_PORT" + path); // no DNS dependency
    }

    private static String kubectl(String... args) throws Exception {
        String[] command = Stream.concat(Stream.of("kubectl"), Arrays.stream(args)).toArray(String[]::new);
        ExecResult result = k3s.execInContainer(command);
        assertEquals(0, result.getExitCode(), String.join(" ", command) + " failed: " + result.getStderr());
        return result.getStdout().trim();
    }
}
