import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.containers.Network;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.k3s.K3sContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

class KubeconfigTest {

    static final Network network = Network.newNetwork();
    static final DockerImageName K3S = DockerImageName.parse("rancher/k3s:v1.36.4-k3s1");

    // Two real clusters. Their kubeconfigs both name everything "default", as k3s (and many installers) do.
    static final K3sContainer staging = cluster("staging");
    static final K3sContainer prod = cluster("prod");
    static ExecResult merge;

    @BeforeAll
    static void merge() throws Exception {
        staging.start();
        prod.start();
        // The script runs on a workstation that reaches both API servers: here, the staging node.
        staging.copyFileToContainer(Transferable.of(staging.generateInternalKubeConfigYaml("staging")), "/lab/staging.yaml");
        staging.copyFileToContainer(Transferable.of(prod.generateInternalKubeConfigYaml("prod")), "/lab/prod.yaml");
        staging.copyFileToContainer(MountableFile.forClasspathResource("merge.sh"), "/lab/merge.sh");
        merge = staging.execInContainer("sh", "/lab/merge.sh");
    }

    @AfterAll
    static void shutdown() {
        staging.stop();
        prod.stop();
        network.close();
    }

    @Test
    void scriptSucceeds() {
        assertEquals(0, merge.getExitCode(), "merge.sh failed:\n" + merge.getStderr());
    }

    @Test
    void eachContextReachesItsOwnCluster() throws Exception {
        String contexts = kubectl("config", "get-contexts", "-o", "name");
        assertEquals(nodeOf(staging), kubectl("--context", "staging", "get", "nodes", "-o", "name"),
                "context staging does not reach the staging cluster. Contexts in merged.yaml:\n" + contexts);
        assertEquals(nodeOf(prod), kubectl("--context", "prod", "get", "nodes", "-o", "name"),
                "context prod does not reach the prod cluster. Contexts in merged.yaml:\n" + contexts);
    }

    @Test
    void defaultContextIsStaging() throws Exception {
        assertEquals("staging", kubectl("config", "current-context"));
    }

    @Test
    void fileIsSelfContained() throws Exception {
        String raw = kubectl("config", "view", "--raw");
        assertTrue(!raw.contains("/lab/") && raw.contains("certificate-authority-data"),
                "merged.yaml should embed the certificates, not point at other files");
    }

    private static K3sContainer cluster(String name) {
        return new K3sContainer(K3S).withNetwork(network).withNetworkAliases(name)
                .withCommand("server", "--disable=traefik", "--tls-san=" + name);   // API certificate valid for that name
    }

    private static String nodeOf(K3sContainer cluster) throws Exception {
        ExecResult result = cluster.execInContainer("kubectl", "get", "nodes", "-o", "name");
        return result.getStdout().trim();
    }

    /** kubectl on the staging node, using only the merged file. */
    private static String kubectl(String... args) throws Exception {
        String[] command = Stream.concat(Stream.of("kubectl", "--kubeconfig", "/lab/merged.yaml"), Arrays.stream(args))
                .toArray(String[]::new);
        ExecResult result = staging.execInContainer(command);
        assertEquals(0, result.getExitCode(), String.join(" ", command) + " failed: " + result.getStderr());
        return result.getStdout().trim();
    }
}
