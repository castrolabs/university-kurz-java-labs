namespace BenchLab;

public static class Measure
{
    // Keeps results reachable so the JIT cannot treat the work as dead code.
    public static object? Sink;

    public static long BytesPerCall(Func<object?> work, int iterations = 1000)
    {
        ArgumentOutOfRangeException.ThrowIfNegativeOrZero(iterations);
        Sink = work(); // warm-up: JIT and first-call costs are not steady-state allocations
        long before = GC.GetAllocatedBytesForCurrentThread();
        for (int i = 0; i < iterations; i++) Sink = work();
        return (GC.GetAllocatedBytesForCurrentThread() - before) / iterations;
    }

    public static double AllocRatio(Func<object?> baseline, Func<object?> candidate)
    {
        long baselineBytes = BytesPerCall(baseline);
        if (baselineBytes == 0) throw new InvalidOperationException("The baseline allocates nothing, so a ratio is meaningless.");
        return (double)BytesPerCall(candidate) / baselineBytes;
    }
}
