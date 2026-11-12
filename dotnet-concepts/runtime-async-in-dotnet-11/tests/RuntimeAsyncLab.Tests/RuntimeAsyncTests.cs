extern alias Classic;
extern alias RuntimeAsync;

using System.Reflection;
using System.Runtime.CompilerServices;
using ClassicContext = Classic::RuntimeAsyncLab.CountingContext;
using ClassicWork = Classic::RuntimeAsyncLab.Workload;
using RaContext = RuntimeAsync::RuntimeAsyncLab.CountingContext;
using RaWork = RuntimeAsync::RuntimeAsyncLab.Workload;

namespace RuntimeAsyncLab.Tests;

// The same Workload.cs is compiled into two assemblies. The tests check properties you can
// observe, with ranges and comparisons, never exact numbers: those depend on machine and build.
public class RuntimeAsyncTests
{
    private static bool HasStateMachineAttribute(Type workload) =>
        workload.GetMethod("FramesAtDepth")!.GetCustomAttribute<AsyncStateMachineAttribute>() is not null;

    [Fact]
    public void Only_the_classic_build_has_compiler_state_machines()
    {
        Assert.True(HasStateMachineAttribute(typeof(ClassicWork)));
        Assert.False(HasStateMachineAttribute(typeof(RaWork)));
    }

    [Fact]
    public async Task Runtime_async_keeps_the_live_stack_shorter_and_flat()
    {
        var classicShallow = await ClassicWork.FramesAtDepth(0);
        var classicDeep = await ClassicWork.FramesAtDepth(10);
        var raShallow = await RaWork.FramesAtDepth(0);
        var raDeep = await RaWork.FramesAtDepth(10);

        Assert.True(raDeep < classicDeep, $"runtime-async {raDeep} vs classic {classicDeep}");
        var classicPerLevel = (classicDeep - classicShallow) / 10.0;
        var raPerLevel = (raDeep - raShallow) / 10.0;
        Assert.InRange(raPerLevel, 0.5, 1.5);                 // about one real frame per call
        Assert.True(classicPerLevel >= 2 * raPerLevel, $"per level: classic {classicPerLevel}, runtime-async {raPerLevel}");
    }

    [Fact]
    public async Task Allocation_per_suspending_call_is_lower_with_runtime_async()
    {
        var classic = await ClassicWork.BytesPerCall(20_000);
        var ra = await RaWork.BytesPerCall(20_000);

        Assert.InRange(classic, 1, 1024);   // a boxed state machine and a task still cost something
        Assert.InRange(ra, 1, 1024);        // runtime-async still allocates when it suspends
        Assert.True(ra < classic, $"runtime-async {ra} B vs classic {classic} B per call");
    }

    [Fact]
    public async Task Both_modes_compute_the_same_result()
    {
        Assert.Equal(499_500, await ClassicWork.SuspendingLoop(1_000));
        Assert.Equal(499_500, await RaWork.SuspendingLoop(1_000));
    }

    [Theory]
    [InlineData(true, 1)]
    [InlineData(false, 0)]
    public async Task ConfigureAwait_decides_whether_the_context_is_used_in_classic(bool capture, int expectedPosts)
    {
        var original = SynchronizationContext.Current;
        var context = new ClassicContext();
        SynchronizationContext.SetSynchronizationContext(context);
        try { Assert.Equal(expectedPosts, await ClassicWork.PostsObserved(context, capture)); }
        finally { SynchronizationContext.SetSynchronizationContext(original); }
    }

    [Theory]
    [InlineData(true, 1)]
    [InlineData(false, 0)]
    public async Task ConfigureAwait_decides_whether_the_context_is_used_in_runtime_async(bool capture, int expectedPosts)
    {
        var original = SynchronizationContext.Current;
        var context = new RaContext();
        SynchronizationContext.SetSynchronizationContext(context);
        try { Assert.Equal(expectedPosts, await RaWork.PostsObserved(context, capture)); }
        finally { SynchronizationContext.SetSynchronizationContext(original); }
    }

    [Fact]
    public async Task AsyncLocal_flows_the_same_in_both_modes()
    {
        var a = new AsyncLocal<string?> { Value = "outer" };
        Assert.Equal("inner", await ClassicWork.AsyncLocalAfterYield(a));
        Assert.Equal("outer", a.Value);

        var b = new AsyncLocal<string?> { Value = "outer" };
        Assert.Equal("inner", await RaWork.AsyncLocalAfterYield(b));
        Assert.Equal("outer", b.Value);
    }

    [Fact]
    public async Task Exception_stack_trace_has_the_same_shape_in_both_modes()
    {
        var classic = await Assert.ThrowsAsync<InvalidOperationException>(() => ClassicWork.ThrowAfterYield(3));
        var ra = await Assert.ThrowsAsync<InvalidOperationException>(() => RaWork.ThrowAfterYield(3));

        static int Lines(Exception e) => e.StackTrace!.Split('\n', StringSplitOptions.RemoveEmptyEntries).Length;
        Assert.InRange(Lines(classic), 4, 8);
        Assert.InRange(Lines(ra), 4, 8);
        Assert.Equal(Lines(classic), Lines(ra));
    }
}
