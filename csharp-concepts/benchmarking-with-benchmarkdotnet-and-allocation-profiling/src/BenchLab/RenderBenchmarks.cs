using BenchmarkDotNet.Attributes;

namespace BenchLab;

// TODO-04: Turn this class into a BenchmarkDotNet benchmark that compares RenderSlow and RenderFast.
//   - Report allocations with [MemoryDiagnoser] on the class.
//   - Make `Count` a parameter with at least two sizes, e.g. [Params(10, 1000)].
//   - Build `_values` once in a [GlobalSetup] method, not inside the benchmarks.
//   - Write two [Benchmark] methods, `Slow` and `Fast`, that call ReportRenderer. Mark `Slow` as the
//     baseline ([Benchmark(Baseline = true)]) and return the result of both, so the work is not dead code.
//   When it is done, run it in Release with:
//     dotnet run -c Release --project src/BenchLab.Runner -- --filter "*" --job short
public class RenderBenchmarks
{
    public int Count;

    private int[] _values = [];

    public void Setup() => _values = Enumerable.Range(0, Count).ToArray();

    public string Slow() => throw new NotImplementedException("Not implemented yet.");

    public string Fast() => throw new NotImplementedException("Not implemented yet.");
}
