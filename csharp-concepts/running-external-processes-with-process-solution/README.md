# Running external processes with Process

## Goal

Run a child process the way production code needs to: arguments passed without a shell, both output streams read without deadlock, a timeout that kills the whole process tree, and an exit code that tells failure from a signal.

## Prerequisites

- `async` and `await`, `Task`
- `CancellationToken`
- .NET 10 SDK
- macOS or Linux (the tests start `sh`, `head`, `tr`, `sleep` and `printf`)

## Task

`ShellRunner` is stubbed. `Build` creates the `ProcessStartInfo`, `RunAsync` runs it and captures stdout, stderr and the exit code, and `Classify` interprets the exit code. The tests use small shell scripts as the "external program" and never need the network.

## Instructions

Complete the following TODOs:

- TODO-00: `Build`: fill `ArgumentList`, redirect both streams, `UseShellExecute = false`, working directory and environment.
- TODO-01: `RunAsync`: start the process and read stdout and stderr at the same time, so a full pipe cannot block the child. A missing program must throw `Win32Exception`.
- TODO-02: `RunAsync`: honor the timeout and kill the whole process tree with `Kill(entireProcessTree: true)`.
- TODO-03: `Classify` and `SignalNumber`: exit code `128 + N` means "killed by signal N".

Run the tests until they all pass.

## Running the Lab

From the lab directory:

```bash
dotnet test
```

## Bonus (Optional)

- TODO-04 (optional): Remove the concurrent reads and read stdout to the end first, then stderr. Run `ShouldNotDeadlockWhenStderrFillsItsPipeFirst` and watch it fail on the 10 second guard. Find the amount of stderr where it starts to hang.
- TODO-05 (optional): In a separate `net11.0` console project (needs the .NET 11 SDK), run the same scripts with `Process.RunAndCaptureText` and `Process.Run`, and compare what happens to a grandchild on timeout with your `Kill(entireProcessTree: true)` version.
