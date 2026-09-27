import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.k3s.K3sContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

class BootstrapTest {

    static final K3sContainer k3s = new K3sContainer(DockerImageName.parse("rancher/k3s:v1.36.4-k3s1"));
    static ExecResult first;
    static ExecResult second;
    static ExecResult third;

    @BeforeAll
    static void bootstrap() throws Exception {
        k3s.start();
        k3s.copyFileToContainer(MountableFile.forClasspathResource("bootstrap.sh"), "/lab/bootstrap.sh");
        first = run("info", "first-Pa55word");
        second = run("debug", "second-Pa55word");   // a change: new log level, rotated password
        third = run("debug", "second-Pa55word");    // nothing changed
    }

    private static ExecResult run(String logLevel, String password) throws Exception {
        return k3s.execInContainer("env", "LOG_LEVEL=" + logLevel, "DB_PASSWORD=" + password, "sh", "/lab/bootstrap.sh");
    }

    @AfterAll
    static void shutdown() {
        k3s.stop();
    }

    @Test
    void everyRunSucceeds() {
        assertEquals(0, first.getExitCode(), "first run failed:\n" + first.getStderr());
        assertEquals(0, second.getExitCode(), "second run (new values) failed:\n" + second.getStderr());
        assertEquals(0, third.getExitCode(), "third run (same values) failed:\n" + third.getStderr());
    }

    @Test
    void theClusterHasTheLatestValues() throws Exception {
        assertEquals("debug", kubectl("-n", "team-a", "get", "configmap", "app-config", "-o", "jsonpath={.data.LOG_LEVEL}"));
        String password = kubectl("-n", "team-a", "get", "secret", "db-creds", "-o", "jsonpath={.data.password}");
        assertEquals("second-Pa55word", new String(Base64.getDecoder().decode(password), StandardCharsets.UTF_8));
    }

    @Test
    void theRestIsInPlace() throws Exception {
        assertEquals("prod", kubectl("get", "namespace", "team-a", "-o", "jsonpath={.metadata.labels.env}"));
        assertEquals("yes", kubectl("auth", "can-i", "create", "deployments", "-n", "team-a",
                "--as=system:serviceaccount:team-a:deployer"));
    }

    @Test
    void passwordsNeverReachTheOutput() {
        for (ExecResult run : new ExecResult[] {first, second, third}) {
            String output = run.getStdout() + run.getStderr();
            assertFalse(Stream.of("Pa55word", "Zmlyc3QtUGE1NXdvcmQ=", "c2Vjb25kLVBhNTV3b3Jk").anyMatch(output::contains),
                    "the password (or its base64) is in the script output:\n" + output);
        }
    }

    private static String kubectl(String... args) throws Exception {
        String[] command = Stream.concat(Stream.of("kubectl"), Arrays.stream(args)).toArray(String[]::new);
        ExecResult result = k3s.execInContainer(command);
        assertEquals(0, result.getExitCode(), String.join(" ", command) + " failed: " + result.getStderr());
        return result.getStdout().trim();
    }
}
