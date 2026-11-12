using Microsoft.Agents.AI.Workflows;

namespace WorkflowLab;

[SendsMessage(typeof(int))]
public sealed partial class Source() : Executor("Source"), IResettableExecutor
{
    [MessageHandler]
    private ValueTask<int> HandleAsync(int n, IWorkflowContext context) => ValueTask.FromResult(n);

    public ValueTask ResetAsync() => ValueTask.CompletedTask;
}

[SendsMessage(typeof(int))]
public sealed partial class Square() : Executor("Square"), IResettableExecutor
{
    [MessageHandler]
    private ValueTask<int> HandleAsync(int n, IWorkflowContext context) => ValueTask.FromResult(n * n);

    public ValueTask ResetAsync() => ValueTask.CompletedTask;
}

[SendsMessage(typeof(int))]
public sealed partial class Cube() : Executor("Cube"), IResettableExecutor
{
    [MessageHandler]
    private ValueTask<int> HandleAsync(int n, IWorkflowContext context) => ValueTask.FromResult(n * n * n);

    public ValueTask ResetAsync() => ValueTask.CompletedTask;
}

[YieldsOutput(typeof(int))]
public sealed partial class Total(int expectedParts) : Executor("Total"), IResettableExecutor
{
    private int _received;
    private int _sum;

    [MessageHandler]
    private async ValueTask HandleAsync(int part, IWorkflowContext context)
    {
        _sum += part;
        if (++_received == expectedParts)
        {
            await context.YieldOutputAsync(_sum);
        }
    }

    public ValueTask ResetAsync()
    {
        _received = 0;
        _sum = 0;
        return ValueTask.CompletedTask;
    }
}

public static class FanWorkflow
{
    public static Workflow Build(Total total)
    {
        var source = new Source();
        var square = new Square();
        var cube = new Cube();

        return new WorkflowBuilder(source)
            .AddFanOutEdge(source, [square, cube])
            .AddFanInBarrierEdge([square, cube], total)
            .WithOutputFrom(total)
            .Build();
    }

    public static async Task<IReadOnlyList<int>> RunAsync(Workflow workflow, int n)
    {
        await using Run run = await InProcessExecution.RunAsync(workflow, n);
        return run.NewEvents.OfType<WorkflowOutputEvent>().Select(e => (int)e.Data!).ToList();
    }
}
