using System.Diagnostics;

namespace ProcessLab;

public sealed record RunResult(int ExitCode, string StandardOutput, string StandardError, bool TimedOut);

public enum ExitKind { Success, Failed, Signaled }

public static class ShellRunner
{
    // TODO-00: build the ProcessStartInfo. Add every argument to ArgumentList (never
    // concatenate a string into Arguments), redirect stdout and stderr, turn
    // UseShellExecute off, and apply the working directory and environment when given.
    public static ProcessStartInfo Build(
        string fileName,
        IEnumerable<string> arguments,
        string? workingDirectory = null,
        IDictionary<string, string>? environment = null) => throw new NotImplementedException();

    // TODO-01: start the process and return its exit code with both streams as text.
    // Trap: reading StandardOutput to the end and only then StandardError deadlocks when
    // the child fills the stderr pipe first. Start both ReadToEndAsync calls before waiting.
    // A missing executable must surface as the Win32Exception from Process.Start.
    //
    // TODO-02: apply the timeout. Wait for exit with a CancellationToken; when it fires,
    // kill the whole tree (Kill(entireProcessTree: true)), wait for exit, and return the
    // result with TimedOut = true. A grandchild that survives keeps the pipes open.
    public static Task<RunResult> RunAsync(ProcessStartInfo psi, TimeSpan timeout) => throw new NotImplementedException();

    // TODO-03: on Unix, .NET reports a process killed by signal N as exit code 128 + N.
    // 0 is Success, 1..128 is Failed, above 128 is Signaled. SignalNumber returns N, or 0
    // when the process was not killed by a signal.
    public static ExitKind Classify(int exitCode) => throw new NotImplementedException();

    public static int SignalNumber(int exitCode) => throw new NotImplementedException();
}
