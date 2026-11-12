using System.Diagnostics;

namespace ProcessLab;

public sealed record RunResult(int ExitCode, string StandardOutput, string StandardError, bool TimedOut);

public enum ExitKind { Success, Failed, Signaled }

public static class ShellRunner
{
    public static ProcessStartInfo Build(
        string fileName,
        IEnumerable<string> arguments,
        string? workingDirectory = null,
        IDictionary<string, string>? environment = null)
    {
        var psi = new ProcessStartInfo(fileName)
        {
            RedirectStandardOutput = true,
            RedirectStandardError = true,
            UseShellExecute = false,
            CreateNoWindow = true,
        };
        foreach (var a in arguments) psi.ArgumentList.Add(a);
        if (workingDirectory is not null) psi.WorkingDirectory = workingDirectory;
        if (environment is not null)
            foreach (var (k, v) in environment) psi.Environment[k] = v;
        return psi;
    }

    public static async Task<RunResult> RunAsync(ProcessStartInfo psi, TimeSpan timeout)
    {
        using var process = Process.Start(psi) ?? throw new InvalidOperationException("process did not start");

        // Both pipes are drained at the same time, so neither can fill up and block the child.
        var stdout = process.StandardOutput.ReadToEndAsync();
        var stderr = process.StandardError.ReadToEndAsync();

        using var cts = new CancellationTokenSource(timeout);
        var timedOut = false;
        try
        {
            await process.WaitForExitAsync(cts.Token);
        }
        catch (OperationCanceledException)
        {
            timedOut = true;
            // true: also kill the grandchildren, otherwise they keep the pipes open.
            process.Kill(entireProcessTree: true);
            await process.WaitForExitAsync();
        }

        return new RunResult(process.ExitCode, await stdout, await stderr, timedOut);
    }

    // On Unix, .NET reports a process killed by signal N as exit code 128 + N.
    public static ExitKind Classify(int exitCode) => exitCode switch
    {
        0 => ExitKind.Success,
        > 128 => ExitKind.Signaled,
        _ => ExitKind.Failed,
    };

    public static int SignalNumber(int exitCode) =>
        Classify(exitCode) == ExitKind.Signaled ? exitCode - 128 : 0;
}
