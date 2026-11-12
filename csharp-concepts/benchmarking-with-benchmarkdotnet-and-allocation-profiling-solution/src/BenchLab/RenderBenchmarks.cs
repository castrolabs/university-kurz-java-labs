using BenchmarkDotNet.Attributes;

namespace BenchLab;

[MemoryDiagnoser]
public class RenderBenchmarks
{
    [Params(10, 1000)]
    public int Count;

    private int[] _values = [];

    [GlobalSetup]
    public void Setup() => _values = Enumerable.Range(0, Count).ToArray();

    [Benchmark(Baseline = true)]
    public string Slow() => ReportRenderer.RenderSlow(_values);

    [Benchmark]
    public string Fast() => ReportRenderer.RenderFast(_values);
}
