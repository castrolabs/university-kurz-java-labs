using BenchLab;
using BenchmarkDotNet.Running;

// Run in Release, outside the debugger:
//   dotnet run -c Release --project src/BenchLab.Runner -- --filter "*" --job short
BenchmarkSwitcher.FromAssembly(typeof(RenderBenchmarks).Assembly).Run(args);
