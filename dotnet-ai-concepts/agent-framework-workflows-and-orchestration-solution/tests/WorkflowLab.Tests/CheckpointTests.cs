using Microsoft.Agents.AI.Workflows;
using Microsoft.Agents.AI.Workflows.Checkpointing;
using WorkflowLab;

namespace WorkflowLab.Tests;

public class CheckpointTests
{
    [Fact]
    public async Task Without_a_crash_the_loop_sums_every_step()
    {
        var result = await AccumulatorWorkflow.RunAsync(
            AccumulatorWorkflow.Build(max: 8), CheckpointManager.CreateInMemory());

        Assert.Equal(36, result.Output);
        Assert.Null(result.Error);
    }

    [Fact]
    public async Task A_crash_leaves_the_checkpoint_of_the_last_finished_superstep()
    {
        var result = await AccumulatorWorkflow.RunAsync(
            AccumulatorWorkflow.Build(max: 8, failAtStep: 5), CheckpointManager.CreateInMemory());

        Assert.Null(result.Output);
        Assert.Equal("crash at step 5", result.Error);
        Assert.NotNull(result.LastCheckpoint);
    }

    [Fact]
    public async Task A_new_workflow_resumes_from_the_checkpoint_and_restores_the_total()
    {
        var checkpoints = CheckpointManager.CreateInMemory();
        var crashed = await AccumulatorWorkflow.RunAsync(
            AccumulatorWorkflow.Build(max: 8, failAtStep: 5), checkpoints);

        var log = new List<int>();
        var output = await AccumulatorWorkflow.ResumeAsync(
            AccumulatorWorkflow.Build(max: 8, log: log), checkpoints, crashed.LastCheckpoint!);

        Assert.Equal(36, output);
        Assert.Equal([5, 6, 7, 8], log);
    }

    [Fact]
    public async Task Resume_works_from_a_json_store_on_disk_like_another_process()
    {
        string dir = Path.Combine(Path.GetTempPath(), "workflow-lab-" + Guid.NewGuid().ToString("N"));
        Directory.CreateDirectory(dir);
        try
        {
            RunResult crashed;
            using (var firstStore = new FileSystemJsonCheckpointStore(new DirectoryInfo(dir)))
            {
                crashed = await AccumulatorWorkflow.RunAsync(
                    AccumulatorWorkflow.Build(max: 8, failAtStep: 5), CheckpointManager.CreateJson(firstStore));
            }

            using var secondStore = new FileSystemJsonCheckpointStore(new DirectoryInfo(dir));
            var secondProcess = CheckpointManager.CreateJson(secondStore);
            var latest = await secondProcess.GetLatestCheckpointAsync(crashed.SessionId);
            var output = await AccumulatorWorkflow.ResumeAsync(
                AccumulatorWorkflow.Build(max: 8), secondProcess, latest!);

            Assert.Equal(36, output);
        }
        finally
        {
            Directory.Delete(dir, recursive: true);
        }
    }
}
