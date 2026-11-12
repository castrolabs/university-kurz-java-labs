# Runtime Async in .NET 11

## Goal

Compile the same async code twice, once with compiler-generated state machines and once with
`<Features>runtime-async=on</Features>`, and measure what changes: the live stack, the bytes
allocated per suspending call, and what does not change (results, `ConfigureAwait`,
`AsyncLocal<T>`, the stack trace of an exception).

## Prerequisites

- `async` and `await`, `Task`, `SynchronizationContext` and `ConfigureAwait`
- .NET 11 SDK (11.0.100 or later) on your `PATH`. The lab targets `net11.0`, so the .NET 10 SDK cannot build it.

## Task

`src/Workload/Workload.cs` is compiled into two assemblies by two projects that differ in one line:
`RuntimeAsyncLab.Classic` (default) and `RuntimeAsyncLab.RuntimeAsync` (`runtime-async=on`).
The tests reference both with extern aliases and check properties you can observe:
frames per call level, an allocation range per call, and equal behavior. They never assert an exact
number, because those depend on the machine. Allocation is measured with `GC.GetTotalAllocatedBytes`
in a loop, so run the tests in Release if you want numbers close to the lesson (`dotnet test -c Release`).
Runtime async is still documented as a preview feature for your own code, even though the
runtime libraries already use it.

## Instructions

Complete the following TODOs in `src/Workload/Workload.cs` (write real `async` methods; do not return tasks by hand):

- TODO-00: `FramesAtDepth`, a recursive async method that returns `new StackTrace().FrameCount` at the bottom.
- TODO-01: `SuspendingLoop`, which awaits `SuspendOnce(i)` n times and sums the results.
- TODO-02: `BytesPerCall`, the allocation probe built on `GC.GetTotalAllocatedBytes(precise: true)`.
- TODO-03: `PostsObserved`, which counts continuations posted to a `SynchronizationContext`, with and without `ConfigureAwait(false)`.
- TODO-04: `AsyncLocalAfterYield`, which reads an `AsyncLocal<T>` after an await.
- TODO-05: `ThrowAfterYield`, which throws from the bottom of a chain of awaits.

Run the tests until they all pass.

## Running the Lab

From the lab directory:

```bash
dotnet test
```

## Bonus (Optional)

- TODO-06 (optional): Print `FramesAtDepth(0)` through `FramesAtDepth(10)` for both assemblies and plot frames against depth. What is the slope of each?
- TODO-07 (optional): Add `<UseRuntimeAsync>false</UseRuntimeAsync>` to the RuntimeAsync project (the documented opt-out) and check which tests change.
- TODO-08 (optional): Replace the allocation probe with a BenchmarkDotNet `[MemoryDiagnoser]` benchmark and compare its numbers with the test's range.
