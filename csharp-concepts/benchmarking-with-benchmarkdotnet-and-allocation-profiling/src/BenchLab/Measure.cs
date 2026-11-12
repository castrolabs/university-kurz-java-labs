namespace BenchLab;

public static class Measure
{
    // Keeps results reachable so the JIT cannot treat the work as dead code.
    public static object? Sink;

    public static long BytesPerCall(Func<object?> work, int iterations = 1000)
    {
        // TODO-00: Return the average number of bytes allocated per call of `work`.
        //   - Throw ArgumentOutOfRangeException when `iterations` is zero or negative.
        //   - Call `work` once first (warm-up), so JIT and first-call costs are not counted.
        //   - Read GC.GetAllocatedBytesForCurrentThread() before and after a loop of `iterations`
        //     calls, store every result in `Sink`, and divide the difference by `iterations`.
        throw new NotImplementedException("Not implemented yet.");
    }

    public static double AllocRatio(Func<object?> baseline, Func<object?> candidate)
    {
        // TODO-01: Return BytesPerCall(candidate) / BytesPerCall(baseline), the same idea as the
        //   "Alloc Ratio" column of BenchmarkDotNet. Throw InvalidOperationException when the
        //   baseline allocates 0 bytes, because a ratio against zero means nothing.
        throw new NotImplementedException("Not implemented yet.");
    }
}
