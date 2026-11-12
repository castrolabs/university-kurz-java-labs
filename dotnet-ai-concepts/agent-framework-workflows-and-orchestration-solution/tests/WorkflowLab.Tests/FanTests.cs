using WorkflowLab;

namespace WorkflowLab.Tests;

public class FanTests
{
    [Fact]
    public async Task Barrier_waits_for_both_branches_and_total_sums_them()
    {
        var workflow = FanWorkflow.Build(new Total(expectedParts: 2));

        var outputs = await FanWorkflow.RunAsync(workflow, 3);

        Assert.Equal([36], outputs);
    }

    [Fact]
    public async Task A_shared_total_is_reset_between_runs()
    {
        var workflow = FanWorkflow.Build(new Total(expectedParts: 2));

        var first = await FanWorkflow.RunAsync(workflow, 3);
        var second = await FanWorkflow.RunAsync(workflow, 2);

        Assert.Equal([36], first);
        Assert.Equal([12], second);
    }
}
