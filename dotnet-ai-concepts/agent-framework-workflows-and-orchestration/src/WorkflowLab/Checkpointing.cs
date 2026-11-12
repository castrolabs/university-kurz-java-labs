using Microsoft.Agents.AI.Workflows;

namespace WorkflowLab;

[YieldsOutput(typeof(int))]
public sealed partial class Accumulator(int max, int failAtStep = -1, List<int>? log = null) : Executor("Accumulator")
{
    private const string StateKey = "total";
    private int _total;

    [MessageHandler(Send = [typeof(int)])]
    private async ValueTask HandleAsync(int step, IWorkflowContext context)
    {
        if (step == failAtStep)
        {
            throw new InvalidOperationException($"crash at step {step}");
        }

        _total += step;
        log?.Add(step);

        if (step < max)
        {
            await context.SendMessageAsync(step + 1);
        }
        else
        {
            await context.YieldOutputAsync(_total);
        }
    }

    // TODO-06a: Save _total under StateKey when a checkpoint is taken (QueueStateUpdateAsync).
    protected override ValueTask OnCheckpointingAsync(IWorkflowContext context, CancellationToken cancellationToken = default)
        => ValueTask.CompletedTask;

    // TODO-06b: Read _total back from StateKey when a checkpoint is restored (ReadStateAsync).
    protected override ValueTask OnCheckpointRestoredAsync(IWorkflowContext context, CancellationToken cancellationToken = default)
        => ValueTask.CompletedTask;
}

public sealed record RunResult(int? Output, string? Error, CheckpointInfo? LastCheckpoint, string SessionId);

public static class AccumulatorWorkflow
{
    public static Workflow Build(int max, int failAtStep = -1, List<int>? log = null)
    {
        var accumulator = new Accumulator(max, failAtStep, log);
        return new WorkflowBuilder(accumulator).AddEdge(accumulator, accumulator).WithOutputFrom(accumulator).Build();
    }

    public static async Task<RunResult> RunAsync(Workflow workflow, CheckpointManager checkpoints)
    {
        await using StreamingRun run = await InProcessExecution.RunStreamingAsync(workflow, 1, checkpoints);
        int? output = null;
        string? error = null;
        await foreach (WorkflowEvent e in run.WatchStreamAsync())
        {
            if (e is WorkflowOutputEvent o) output = (int)o.Data!;
            if (e is WorkflowErrorEvent err) error = err.Exception?.GetBaseException().Message;
        }
        return new RunResult(output, error, run.Checkpoints.LastOrDefault(), run.SessionId);
    }

    public static async Task<int?> ResumeAsync(Workflow workflow, CheckpointManager checkpoints, CheckpointInfo from)
    {
        await using StreamingRun run = await InProcessExecution.ResumeStreamingAsync(workflow, from, checkpoints);
        int? output = null;
        await foreach (WorkflowEvent e in run.WatchStreamAsync())
        {
            if (e is WorkflowOutputEvent o) output = (int)o.Data!;
        }
        return output;
    }
}
