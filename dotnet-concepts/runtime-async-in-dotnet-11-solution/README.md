# Runtime Async in .NET 11 (Solution)

Solution for the lab `runtime-async-in-dotnet-11`. `src/Workload/Workload.cs` has all six TODOs implemented,
and the tests pass with the .NET 11 SDK (11.0.100 or later).

From this directory:

```bash
dotnet test
```

Both modes compile the same file; only `RuntimeAsyncLab.RuntimeAsync.csproj` adds
`<Features>runtime-async=on</Features>`. Runtime async is still documented as a preview feature for user code.
