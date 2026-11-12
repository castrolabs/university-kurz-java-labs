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
        // TODO-03: The barrier delivers each branch result as a separate message. Accumulate them in
        //   _sum, count them in _received, and yield the sum once expectedParts messages arrived.
        await ValueTask.CompletedTask;
    }

    public ValueTask ResetAsync()
    {
        // TODO-04: Reset the instance state so the same workflow can run again.
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

        // TODO-05: Start at source, fan out to square and cube (AddFanOutEdge), join both into total
        //   with a barrier (AddFanInBarrierEdge) so total runs only after BOTH branches finished,
        //   and make total the output source.
        throw new NotImplementedException("Not implemented yet.");
    }

    public static async Task<IReadOnlyList<int>> RunAsync(Workflow workflow, int n)
    {
        await using Run run = await InProcessExecution.RunAsync(workflow, n);
        return run.NewEvents.OfType<WorkflowOutputEvent>().Select(e => (int)e.Data!).ToList();
    }
}
