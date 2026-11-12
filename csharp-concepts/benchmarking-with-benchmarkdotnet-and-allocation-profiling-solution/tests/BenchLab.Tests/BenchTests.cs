using System.Reflection;
using BenchmarkDotNet.Attributes;

[assembly: CollectionBehavior(DisableTestParallelization = true)]

namespace BenchLab.Tests;

// The tests check allocations (bytes), never time: bytes are stable across machines, time is not.
// They use ranges, because the runtime is free to change small details between versions.
public class BenchTests
{
    private static int[] Numbers(int count) => Enumerable.Range(0, count).ToArray();

    // TODO-00
    [Fact]
    public void ShouldMeasureTheBytesOfOneArray()
    {
        // An array of 1000 bytes is 1000 bytes of data plus a header of 24 bytes on 64-bit.
        var bytes = Measure.BytesPerCall(() => new byte[1000]);
        Assert.InRange(bytes, 1000, 1100);
    }

    [Fact]
    public void ShouldMeasureZeroForWorkThatDoesNotAllocate()
    {
        var text = "constant";
        Assert.Equal(0, Measure.BytesPerCall(() => text));
    }

    [Fact]
    public void ShouldRejectNonPositiveIterations()
    {
        Assert.Throws<ArgumentOutOfRangeException>(() => Measure.BytesPerCall(() => "x", 0));
    }

    // TODO-01
    [Fact]
    public void ShouldReportTheAllocationRatioOfACandidate()
    {
        var ratio = Measure.AllocRatio(() => new byte[2000], () => new byte[1000]);
        Assert.InRange(ratio, 0.4, 0.6);
    }

    [Fact]
    public void ShouldRefuseARatioAgainstABaselineThatAllocatesNothing()
    {
        var text = "constant";
        Assert.Throws<InvalidOperationException>(() => Measure.AllocRatio(() => text, () => new byte[10]));
    }

    // TODO-02
    [Fact]
    public void ShouldRenderTheSameTextAsTheSlowVersion()
    {
        var values = Numbers(50);
        Assert.Equal(ReportRenderer.RenderSlow(values), ReportRenderer.RenderFast(values));
        Assert.Equal("", ReportRenderer.RenderFast([]));
    }

    [Fact]
    public void ShouldAllocateAFewTimesTheResultNotTheSquareOfIt()
    {
        var values = Numbers(1000);
        var resultBytes = ReportRenderer.RenderSlow(values).Length * 2L;

        var slow = Measure.BytesPerCall(() => ReportRenderer.RenderSlow(values), 20);
        var fast = Measure.BytesPerCall(() => ReportRenderer.RenderFast(values), 20);

        Assert.True(slow > 50 * resultBytes, $"slow allocated {slow} bytes for a {resultBytes} byte result");
        Assert.InRange(fast, resultBytes, 4 * resultBytes);
    }

    [Fact]
    public void ShouldCutTheAllocationRatioByMoreThanNinetyNinePercent()
    {
        var values = Numbers(1000);
        var ratio = Measure.AllocRatio(
            () => ReportRenderer.RenderSlow(values),
            () => ReportRenderer.RenderFast(values));
        Assert.True(ratio < 0.01, $"ratio was {ratio}");
    }

    // TODO-03
    [Theory]
    [InlineData(1_000)]
    [InlineData(100_000)]
    public void ShouldChecksumAFrameWithoutCopyingIt(int length)
    {
        var frame = new byte[length];
        Array.Fill(frame, (byte)1);

        // Leave a dirty, larger buffer in the pool: a method that sums the whole rented buffer is wrong.
        var dirty = System.Buffers.ArrayPool<byte>.Shared.Rent(length * 2);
        Array.Fill(dirty, (byte)1);
        System.Buffers.ArrayPool<byte>.Shared.Return(dirty);

        Assert.Equal(length, Frames.ChecksumPooled(frame));
        Assert.Equal(Frames.ChecksumCopy(frame), Frames.ChecksumPooled(frame));
    }

    [Fact]
    public void ShouldStopAllocatingTheFrameOnEveryCall()
    {
        var frame = new byte[100_000]; // above the 85,000 byte large object heap threshold

        var copy = Measure.BytesPerCall(() => Frames.ChecksumCopy(frame), 200);
        var pooled = Measure.BytesPerCall(() => Frames.ChecksumPooled(frame), 200);

        Assert.True(copy >= 100_000, $"copy allocated {copy} bytes");
        Assert.True(pooled < 1_000, $"pooled allocated {pooled} bytes");
    }

    // TODO-04
    [Fact]
    public void ShouldDeclareTheMemoryDiagnoser()
    {
        Assert.NotNull(typeof(RenderBenchmarks).GetCustomAttribute<MemoryDiagnoserAttribute>());
    }

    [Fact]
    public void ShouldHaveExactlyOneBaselineAndAtLeastTwoBenchmarks()
    {
        var benchmarks = BenchmarkMethods();
        Assert.True(benchmarks.Count >= 2, "write at least two [Benchmark] methods");
        Assert.Single(benchmarks, m => m.GetCustomAttribute<BenchmarkAttribute>()!.Baseline);
    }

    [Fact]
    public void ShouldReturnTheResultOfEveryBenchmarkSoItIsNotDeadCode()
    {
        Assert.NotEmpty(BenchmarkMethods());
        foreach (var method in BenchmarkMethods())
            Assert.NotEqual(typeof(void), method.ReturnType);
    }

    [Fact]
    public void ShouldParameterizeTheInputSize()
    {
        var field = typeof(RenderBenchmarks).GetField(nameof(RenderBenchmarks.Count))!;
        var values = field.GetCustomAttribute<ParamsAttribute>()?.Values;
        Assert.NotNull(values);
        Assert.True(values!.Length >= 2, "[Params] needs at least two sizes");
    }

    [Fact]
    public void ShouldBuildTheInputInAGlobalSetupNotInsideTheBenchmark()
    {
        var setup = typeof(RenderBenchmarks).GetMethod(nameof(RenderBenchmarks.Setup))!;
        Assert.NotNull(setup.GetCustomAttribute<GlobalSetupAttribute>());
    }

    [Fact]
    public void ShouldMakeTheBenchmarksProduceTheSameResult()
    {
        var bench = new RenderBenchmarks { Count = 30 };
        bench.Setup();
        Assert.Equal(bench.Slow(), bench.Fast());
        Assert.NotEmpty(bench.Fast());
    }

    private static List<MethodInfo> BenchmarkMethods() =>
        typeof(RenderBenchmarks).GetMethods()
            .Where(m => m.GetCustomAttribute<BenchmarkAttribute>() is not null)
            .ToList();

    // TODO-05 (optional)
    [Fact]
    public void ShouldRenderWithAPooledBuilderAndAllocateOnlyTheResult()
    {
        var values = Numbers(100); // the default pool policy drops builders larger than 4096 characters
        var result = ReportRenderer.RenderPooled(values);
        Assert.Equal(ReportRenderer.RenderSlow(values), result);

        var resultBytes = result.Length * 2L;
        var fast = Measure.BytesPerCall(() => ReportRenderer.RenderFast(values));
        var pooled = Measure.BytesPerCall(() => ReportRenderer.RenderPooled(values));

        Assert.InRange(pooled, resultBytes, resultBytes + 100);
        Assert.True(pooled < fast, $"pooled {pooled} bytes, fast {fast} bytes");
    }
}
