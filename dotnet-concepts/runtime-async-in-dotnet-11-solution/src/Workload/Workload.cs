using System.Diagnostics;
using System.Runtime.CompilerServices;

namespace RuntimeAsyncLab;

// This one file is compiled twice: RuntimeAsyncLab.Classic (compiler-generated state machines)
// and RuntimeAsyncLab.RuntimeAsync (<Features>runtime-async=on</Features>). The tests load both
// and compare what you can observe.
public static class Workload
{
    public static async Task<int> FramesAtDepth(int depth)
    {
        await Task.CompletedTask;
        return depth == 0 ? new StackTrace().FrameCount : await FramesAtDepth(depth - 1);
    }

    // Given. Always suspends once, so every call has to leave the stack and come back.
    [MethodImpl(MethodImplOptions.NoInlining)]
    private static async Task<int> SuspendOnce(int i)
    {
        await Task.Yield();
        return i;
    }

    public static async Task<int> SuspendingLoop(int n)
    {
        var sum = 0;
        for (var i = 0; i < n; i++)
            sum += await SuspendOnce(i);
        return sum;
    }

    public static async Task<long> BytesPerCall(int calls)
    {
        await SuspendingLoop(2_000);
        var before = GC.GetTotalAllocatedBytes(precise: true);
        await SuspendingLoop(calls);
        var after = GC.GetTotalAllocatedBytes(precise: true);
        return (after - before) / calls;
    }

    public static async Task<int> PostsObserved(CountingContext context, bool captureContext)
    {
        var before = context.Posts;
        if (captureContext)
            await Task.Delay(5);
        else
            await Task.Delay(5).ConfigureAwait(false);
        return context.Posts - before;
    }

    public static async Task<string?> AsyncLocalAfterYield(AsyncLocal<string?> local)
    {
        local.Value = "inner";
        await Task.Yield();
        return local.Value;
    }

    public static async Task ThrowAfterYield(int depth)
    {
        await Task.Yield();
        if (depth == 0)
            throw new InvalidOperationException("boom");
        await ThrowAfterYield(depth - 1);
    }
}

// Given. A SynchronizationContext that counts how many continuations were posted to it.
public sealed class CountingContext : SynchronizationContext
{
    private int _posts;
    public int Posts => Volatile.Read(ref _posts);

    public override void Post(SendOrPostCallback d, object? state)
    {
        Interlocked.Increment(ref _posts);
        ThreadPool.QueueUserWorkItem(_ => d(state));
    }
}
