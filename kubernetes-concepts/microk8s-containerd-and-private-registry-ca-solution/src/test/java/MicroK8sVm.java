import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** A throwaway Ubuntu VM managed with Multipass, for the labs that need a real MicroK8s install. */
final class MicroK8sVm implements AutoCloseable {

    record Result(int exitCode, String output) {}

    final String name = "lab-mk8s-" + Long.toHexString(System.nanoTime());

    static boolean multipassInstalled() {
        try {
            return new ProcessBuilder("multipass", "version").start().waitFor(30, TimeUnit.SECONDS);
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    MicroK8sVm launch() throws Exception {
        Result r = run(15, "multipass", "launch", "24.04", "--name", name, "--cpus", "2", "--memory", "4G", "--disk", "12G");
        if (r.exitCode() != 0) {
            throw new IllegalStateException("multipass launch failed:\n" + r.output());
        }
        exec("mkdir", "-p", "/home/ubuntu/lab");
        return this;
    }

    /** Copies a file from the test classpath to /home/ubuntu/lab/ in the VM. */
    void copy(String resource) throws Exception {
        Path local = Path.of(MicroK8sVm.class.getResource("/" + resource).toURI());
        run(2, "multipass", "transfer", local.toString(), name + ":/home/ubuntu/lab/" + local.getFileName());
    }

    /** Runs a command as the ubuntu user (a new login session, like an SSH connection). */
    Result exec(String... command) throws Exception {
        List<String> all = new ArrayList<>(List.of("multipass", "exec", name, "--"));
        all.addAll(Arrays.asList(command));
        return run(20, all.toArray(String[]::new));
    }

    /** Runs a shell snippet as root. */
    Result sudo(String script) throws Exception {
        return exec("sudo", "bash", "-c", script);
    }

    private static Result run(int minutes, String... command) throws Exception {
        Process p = new ProcessBuilder(command).redirectErrorStream(true).start();
        byte[] out = p.getInputStream().readAllBytes();
        if (!p.waitFor(minutes, TimeUnit.MINUTES)) {
            p.destroyForcibly();
            return new Result(-1, "timed out: " + String.join(" ", command));
        }
        return new Result(p.exitValue(), new String(out, StandardCharsets.UTF_8).trim());
    }

    @Override
    public void close() throws Exception {
        run(5, "multipass", "delete", "--purge", name);
    }
}
