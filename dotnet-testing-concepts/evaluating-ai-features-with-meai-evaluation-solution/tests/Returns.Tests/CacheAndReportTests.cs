using Microsoft.Extensions.AI.Evaluation.Reporting;
using Microsoft.Extensions.AI.Evaluation.Reporting.Formats.Html;
using Microsoft.Extensions.AI.Evaluation.Reporting.Storage;
using Returns.Tests.Support;

namespace Returns.Tests;

[TestClass]
public sealed class CacheAndReportTests
{
    private static async Task<ScenarioRunResult> LastResultOfAsync(string executionName, string scenarioName)
    {
        var store = new DiskBasedResultStore(EvalSupport.ResultsDir);
        await foreach (var result in store.ReadResultsAsync(executionName: executionName, scenarioName: scenarioName))
        {
            return result;
        }
        throw new AssertFailedException($"No stored result for {executionName}/{scenarioName}.");
    }

    [TestMethod]
    public async Task ReplayingTheCacheNeverReachesTheModel()
    {
        if (EvalSupport.Recording)
        {
            Assert.Inconclusive("EVAL_RECORD=1: this test is about replay.");
        }

        var (configuration, replay) = EvalSupport.CreateConfiguration(executionName: "replay");
        var evaluated = await EvalRunner.AskAndEvaluateAsync(configuration, Scenarios.Get("refund-window"));

        Assert.AreEqual(0, replay!.Calls);
        Assert.AreNotEqual("", evaluated.Answer);
    }

    [TestMethod]
    public async Task EveryModelTurnOfAReplayedRunIsACacheHit()
    {
        if (EvalSupport.Recording)
        {
            Assert.Inconclusive("EVAL_RECORD=1: this test is about replay.");
        }

        var (configuration, _) = EvalSupport.CreateConfiguration(executionName: "cache-hits");
        await EvalRunner.AskAndEvaluateAsync(configuration, Scenarios.Get("refund-window"));

        var stored = await LastResultOfAsync("cache-hits", "refund-window");
        var turns = stored.ChatDetails!.TurnDetails;

        // 1 answer + 3 judge calls (relevance, coherence, groundedness). The fact check makes no call at all.
        Assert.HasCount(4, turns);
        Assert.IsTrue(turns.All(t => t.CacheHit == true));
    }

    [TestMethod]
    public async Task TheRecordedTokensStayInTheReportEvenWhenReplayed()
    {
        var (configuration, _) = EvalSupport.CreateConfiguration(executionName: "tokens");
        await EvalRunner.AskAndEvaluateAsync(configuration, Scenarios.Get("refund-window"));

        var stored = await LastResultOfAsync("tokens", "refund-window");
        var tokens = stored.ChatDetails!.TurnDetails.Sum(t => t.Usage?.TotalTokenCount ?? 0);

        // The judge reads the policy and thinks aloud: far more tokens than the 20-word answer.
        Assert.IsGreaterThan(1000, tokens);
    }

    [TestMethod]
    public async Task TheRunIsWrittenAsAnHtmlReport()
    {
        var (configuration, _) = EvalSupport.CreateConfiguration(executionName: "report");
        foreach (var scenario in Scenarios.All.Take(3))
        {
            await EvalRunner.AskAndEvaluateAsync(configuration, scenario);
        }

        var results = new List<ScenarioRunResult>();
        await foreach (var result in configuration.ResultStore.ReadResultsAsync(executionName: "report"))
        {
            results.Add(result);
        }

        var path = Path.Combine(EvalSupport.ResultsDir, "report.html");
        await new HtmlReportWriter(path).WriteReportAsync(results);

        Assert.IsTrue(File.Exists(path));
        Assert.HasCount(3, results);
        StringAssert.Contains(await File.ReadAllTextAsync(path), "refund-window");
    }
}
