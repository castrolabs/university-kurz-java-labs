using System.ComponentModel;
using System.Diagnostics;
using ProcessLab;

namespace ProcessLab.Tests;

// These tests start `sh`, so they need macOS or Linux.
public class ShellRunnerTests
{
    private static readonly TimeSpan Generous = TimeSpan.FromSeconds(30);

    private static ProcessStartInfo Sh(string script) => ShellRunner.Build("sh", ["-c", script]);

    // ---- TODO-00 ----

    [Fact]
    public void ShouldPassEachArgumentAsOneItemAndRedirectBothStreams()
    {
        var psi = ShellRunner.Build("printf", ["[%s]", "hello world", "a \"quoted\" $HOME"]);

        Assert.Equal(["[%s]", "hello world", "a \"quoted\" $HOME"], psi.ArgumentList);
        Assert.Equal("", psi.Arguments);
        Assert.True(psi.RedirectStandardOutput);
        Assert.True(psi.RedirectStandardError);
        Assert.False(psi.UseShellExecute);
    }

    [Fact]
    public async Task ShouldNotLetTheShellInterpretArguments()
    {
        var psi = ShellRunner.Build("printf", ["[%s]", "hello world", "$HOME; echo injected"]);

        var result = await ShellRunner.RunAsync(psi, Generous);

        Assert.Equal("[hello world][$HOME; echo injected]", result.StandardOutput);
    }

    [Fact]
    public async Task ShouldApplyWorkingDirectoryAndEnvironment()
    {
        var psi = ShellRunner.Build("sh", ["-c", "echo $LAB_VALUE; pwd -P"],
            workingDirectory: "/tmp", environment: new Dictionary<string, string> { ["LAB_VALUE"] = "bar" });

        var result = await ShellRunner.RunAsync(psi, Generous);

        var lines = result.StandardOutput.Split('\n', StringSplitOptions.RemoveEmptyEntries);
        Assert.Equal("bar", lines[0]);
        Assert.EndsWith("tmp", lines[1]);
    }

    // ---- TODO-01 ----

    [Fact]
    public async Task ShouldCaptureStdoutStderrAndExitCodeSeparately()
    {
        var result = await ShellRunner.RunAsync(Sh("echo out; echo err >&2; exit 3"), Generous);

        Assert.Equal("out", result.StandardOutput.Trim());
        Assert.Equal("err", result.StandardError.Trim());
        Assert.Equal(3, result.ExitCode);
        Assert.False(result.TimedOut);
    }

    [Fact]
    public async Task ShouldNotDeadlockWhenStderrFillsItsPipeFirst()
    {
        // 300 KB on stderr is far more than a pipe buffer. Reading stdout to the end first would hang.
        var script = "head -c 300000 /dev/zero | tr '\\0' 'e' >&2; echo done";

        var result = await ShellRunner.RunAsync(Sh(script), Generous).WaitAsync(TimeSpan.FromSeconds(10));

        Assert.Equal("done", result.StandardOutput.Trim());
        Assert.Equal(300000, result.StandardError.Length);
    }

    [Fact]
    public async Task ShouldNotDeadlockWithTwoMegabytesOnBothStreams()
    {
        var script = "head -c 2000000 /dev/zero | tr '\\0' 'o'; head -c 2000000 /dev/zero | tr '\\0' 'e' >&2";

        var result = await ShellRunner.RunAsync(Sh(script), Generous).WaitAsync(TimeSpan.FromSeconds(20));

        Assert.Equal(2_000_000, result.StandardOutput.Length);
        Assert.Equal(2_000_000, result.StandardError.Length);
    }

    [Fact]
    public async Task ShouldThrowWin32ExceptionForAMissingExecutable()
    {
        var psi = ShellRunner.Build("definitely-not-a-program-" + Guid.NewGuid().ToString("N"), []);

        await Assert.ThrowsAsync<Win32Exception>(() => ShellRunner.RunAsync(psi, Generous));
    }

    // ---- TODO-02 ----

    [Fact]
    public async Task ShouldKillAChildThatOutlivesTheTimeout()
    {
        var sw = Stopwatch.StartNew();

        var result = await ShellRunner.RunAsync(Sh("sleep 30"), TimeSpan.FromMilliseconds(500)).WaitAsync(TimeSpan.FromSeconds(10));

        Assert.True(result.TimedOut);
        Assert.True(sw.Elapsed < TimeSpan.FromSeconds(10));
        Assert.Equal(137, result.ExitCode); // killed by SIGKILL: 128 + 9
    }

    [Fact]
    public async Task ShouldKillTheWholeProcessTreeOnTimeout()
    {
        var pidFile = Path.Combine(Path.GetTempPath(), Guid.NewGuid().ToString("N") + ".pid");
        try
        {
            // The shell starts a grandchild that holds the same pipes, then waits for it.
            var result = await ShellRunner.RunAsync(Sh($"sleep 30 & echo $! > {pidFile}; wait"), TimeSpan.FromMilliseconds(800))
                .WaitAsync(TimeSpan.FromSeconds(10));

            Assert.True(result.TimedOut);
            var grandchild = int.Parse(File.ReadAllText(pidFile).Trim());
            await Task.Delay(300);
            Assert.False(IsAlive(grandchild), "the grandchild survived: Kill(entireProcessTree: true) is missing");
        }
        finally
        {
            if (File.Exists(pidFile)) File.Delete(pidFile);
        }
    }

    [Fact]
    public async Task ShouldNotMarkAFastProcessAsTimedOut()
    {
        var result = await ShellRunner.RunAsync(Sh("echo quick"), Generous);

        Assert.False(result.TimedOut);
        Assert.Equal(0, result.ExitCode);
    }

    private static bool IsAlive(int pid)
    {
        try { Process.GetProcessById(pid); return true; }
        catch (ArgumentException) { return false; }
    }

    // ---- TODO-03 ----

    [Theory]
    [InlineData(0, ExitKind.Success, 0)]
    [InlineData(1, ExitKind.Failed, 0)]
    [InlineData(3, ExitKind.Failed, 0)]
    [InlineData(128, ExitKind.Failed, 0)]
    [InlineData(130, ExitKind.Signaled, 2)]
    [InlineData(137, ExitKind.Signaled, 9)]
    [InlineData(143, ExitKind.Signaled, 15)]
    public void ShouldTellFailureFromSignal(int code, ExitKind kind, int signal)
    {
        Assert.Equal(kind, ShellRunner.Classify(code));
        Assert.Equal(signal, ShellRunner.SignalNumber(code));
    }

    [Fact]
    public async Task ShouldClassifyARealSelfTerminatedChild()
    {
        var result = await ShellRunner.RunAsync(Sh("kill -TERM $$"), Generous);

        Assert.Equal(ExitKind.Signaled, ShellRunner.Classify(result.ExitCode));
        Assert.Equal(15, ShellRunner.SignalNumber(result.ExitCode));
    }
}
