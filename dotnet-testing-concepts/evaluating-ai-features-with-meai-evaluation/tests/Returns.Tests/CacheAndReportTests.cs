using Microsoft.Extensions.AI.Evaluation.Reporting;
using Microsoft.Extensions.AI.Evaluation.Reporting.Formats.Html;
using Microsoft.Extensions.AI.Evaluation.Reporting.Storage;
using Returns.Tests.Support;

namespace Returns.Tests;

[TestClass]
public sealed class CacheAndReportTests
{
    // Provided: reads the stored result (scores, messages and chat details) of a run that just finished.
    private static async Task<ScenarioRunResult> LastResultOfAsync(string executionName, string scenarioName)
    {
        var store = new DiskBasedResultStore(EvalSupport.ResultsDir);
        await foreach (var result in store.ReadResultsAsync(executionName: executionName, scenarioName: scenarioName))
        {
            return result;
        }
        throw new AssertFailedException($"No stored result for {executionName}/{scenarioName}.");
    }

    // TODO-08: Replaying the cache must never reach the model. Skip the test with Assert.Inconclusive when
    // EvalSupport.Recording is true. Otherwise create a configuration with EvalSupport.CreateConfiguration
    // (it returns the ReplayOnlyChatClient as the second value), run EvalRunner.AskAndEvaluateAsync for
    // "refund-window", and assert replay.Calls is 0 and the answer is not empty.
    [TestMethod]
    public async Task ReplayingTheCacheNeverReachesTheModel()
    {
        await Task.CompletedTask;
        Assert.Fail("TODO-08: write this test.");
    }

    // TODO-09: Same setup, execution name "cache-hits", then read the stored result with LastResultOfAsync and
    // look at ChatDetails.TurnDetails. A scenario makes 4 model turns (1 answer and 3 judge calls: relevance,
    // coherence, groundedness; the fact check makes no call). Assert there are 4 and every CacheHit is true.
    // Skip when recording.
    [TestMethod]
    public async Task EveryModelTurnOfAReplayedRunIsACacheHit()
    {
        await Task.CompletedTask;
        Assert.Fail("TODO-09: write this test.");
    }

    // TODO-10: The cache keeps the token usage of the recording. Run "refund-window" under the execution name
    // "tokens", read the stored result, add up Usage?.TotalTokenCount of all the turns, and assert it is greater
    // than 1000: the judge reads the policy and thinks aloud, far more than the 20-word answer.
    [TestMethod]
    public async Task TheRecordedTokensStayInTheReportEvenWhenReplayed()
    {
        await Task.CompletedTask;
        Assert.Fail("TODO-10: write this test.");
    }

    // TODO-11: Run the first three scenarios under the execution name "report", read them back with
    // configuration.ResultStore.ReadResultsAsync(executionName: "report"), and write them with
    // new HtmlReportWriter(path).WriteReportAsync(results) to a "report.html" in EvalSupport.ResultsDir.
    // Assert the file exists, that three results were written, and that the HTML mentions "refund-window".
    [TestMethod]
    public async Task TheRunIsWrittenAsAnHtmlReport()
    {
        await Task.CompletedTask;
        Assert.Fail("TODO-11: write this test.");
    }
}
