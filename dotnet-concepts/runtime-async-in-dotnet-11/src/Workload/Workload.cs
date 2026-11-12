using System.Diagnostics;
using System.Runtime.CompilerServices;

namespace RuntimeAsyncLab;

// This one file is compiled twice: RuntimeAsyncLab.Classic (compiler-generated state machines)
// and RuntimeAsyncLab.RuntimeAsync (<Features>runtime-async=on</Features>). The tests load both
// and compare what you can observe. Every method below that you write must be a real
// "async" method, because the point is how the compiler and the runtime treat it.
public static class Workload
{
    public static Task<int> FramesAtDepth(int depth)
    {
        // TODO-00: Write an async method that calls itself depth times (use "await Task.CompletedTask"
        //   first so the compiler has an await to work with). When depth is 0, return
        //   new StackTrace().FrameCount, the number of frames on the live stack at that moment.
        //   Each level must await the next one, not return its task.
        throw new NotImplementedException("Not implemented yet.");
    }

    // Given. Always suspends once, so every call has to leave the stack and come back.
    [MethodImpl(MethodImplOptions.NoInlining)]
    private static async Task<int> SuspendOnce(int i)
    {
        await Task.Yield();
        return i;
    }

    public static Task<int> SuspendingLoop(int n)
    {
        // TODO-01: Write an async method that awaits SuspendOnce(i) for i from 0 to n - 1 and
        //   returns the sum of the results.
        throw new NotImplementedException("Not implemented yet.");
    }

    public static Task<long> BytesPerCall(int calls)
    {
        // TODO-02: Write an async method that measures the bytes allocated per call of SuspendOnce:
        //   run SuspendingLoop(2_000) once as warm-up, read GC.GetTotalAllocatedBytes(precise: true),
        //   run SuspendingLoop(calls), read it again, and return the difference divided by calls.
        //   GetTotalAllocatedBytes counts all threads, which matters because the loop continues
        //   on thread pool threads after the first Task.Yield.
        throw new NotImplementedException("Not implemented yet.");
    }

    public static Task<int> PostsObserved(CountingContext context, bool captureContext)
    {
        // TODO-03: Write an async method that remembers context.Posts, awaits Task.Delay(5)
        //   (with ConfigureAwait(false) when captureContext is false), and returns how many
        //   continuations were posted to the context in between: context.Posts minus the
        //   remembered value. The test installs the context on the calling thread.
        throw new NotImplementedException("Not implemented yet.");
    }

    public static Task<string?> AsyncLocalAfterYield(AsyncLocal<string?> local)
    {
        // TODO-04: Write an async method that sets local.Value to "inner", awaits Task.Yield(),
        //   and returns local.Value as seen after the await.
        throw new NotImplementedException("Not implemented yet.");
    }

    public static Task ThrowAfterYield(int depth)
    {
        // TODO-05: Write an async method that awaits Task.Yield(), then throws
        //   InvalidOperationException("boom") when depth is 0 and otherwise awaits
        //   ThrowAfterYield(depth - 1). The test counts the lines of the exception's StackTrace.
        throw new NotImplementedException("Not implemented yet.");
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
