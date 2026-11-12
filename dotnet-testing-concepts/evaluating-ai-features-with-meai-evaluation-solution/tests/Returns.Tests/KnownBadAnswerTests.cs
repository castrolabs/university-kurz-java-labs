using Microsoft.Extensions.AI.Evaluation.Quality;
using Microsoft.Extensions.AI.Evaluation.Reporting;
using Returns.Tests.Support;

namespace Returns.Tests;

/// <summary>
/// A gate that no answer can fail is worth nothing. These tests feed the evaluators answers written by hand
/// (no assistant involved) and check that the scores go down.
/// </summary>
[TestClass]
public sealed class KnownBadAnswerTests
{
    private static async Task<EvaluationResult> EvaluateAsync(string scenarioName, string answer)
    {
        var scenario = Scenarios.Get("refund-window");
        var (configuration, _) = EvalSupport.CreateConfiguration(executionName: "known-bad");

        await using var run = await configuration.CreateScenarioRunAsync(scenarioName);
        var question = new ChatMessage(ChatRole.User, scenario.Question);
        var response = new ChatResponse(new ChatMessage(ChatRole.Assistant, answer));

        return await run.EvaluateAsync(question, response, EvalSupport.ContextFor(scenario));
    }

    [TestMethod]
    public async Task AWrongNumberIsCaughtByTheFactCheckAndPulledBelowTheGroundednessGate()
    {
        var result = await EvaluateAsync("bad-wrong-number",
            "You have 60 days to return a jacket, and return shipping is always free.");

        Assert.IsFalse(result.Get<BooleanMetric>(ExpectedFactEvaluator.MetricName).Value);
        // The 4B judge gave this a 3 (and a 4 for relevance): it noticed, but not as loudly as a person would.
        // That is why the gate is "below 4", and why the fact check exists.
        Assert.IsLessThan(4, result.Get<NumericMetric>(GroundednessEvaluator.GroundednessMetricName).Value!.Value);
    }

    [TestMethod]
    public async Task AnOffTopicAnswerIsCaughtByRelevance()
    {
        var result = await EvaluateAsync("bad-off-topic", "Our tents are made of ripstop nylon and weigh 2 kg.");

        Assert.IsFalse(result.Get<BooleanMetric>(ExpectedFactEvaluator.MetricName).Value);
        Assert.IsLessThanOrEqualTo(2, result.Get<NumericMetric>(RelevanceEvaluator.RelevanceMetricName).Value!.Value);
    }
}
