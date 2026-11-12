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
    // Provided. Same flow as EvalRunner, but the answer is the text you pass in. The judge's verdicts for the two
    // answers below are recorded under the scenario names "bad-wrong-number" and "bad-off-topic".
    private static async Task<EvaluationResult> EvaluateAsync(string scenarioName, string answer)
    {
        var scenario = Scenarios.Get("refund-window");
        var (configuration, _) = EvalSupport.CreateConfiguration(executionName: "known-bad");

        await using var run = await configuration.CreateScenarioRunAsync(scenarioName);
        var question = new ChatMessage(ChatRole.User, scenario.Question);
        var response = new ChatResponse(new ChatMessage(ChatRole.Assistant, answer));

        return await run.EvaluateAsync(question, response, EvalSupport.ContextFor(scenario));
    }

    // TODO-06: Evaluate "You have 60 days to return a jacket, and return shipping is always free." under the
    // scenario name "bad-wrong-number". The fact check ("30 days") must be false and groundedness must be below 4
    // (the 4B judge gave it a 3, and relevance a 4: it noticed, but less loudly than a person would).
    [TestMethod]
    public async Task AWrongNumberIsCaughtByTheFactCheckAndPulledBelowTheGroundednessGate()
    {
        await Task.CompletedTask;
        Assert.Fail("TODO-06: write this test.");
    }

    // TODO-07: Evaluate "Our tents are made of ripstop nylon and weigh 2 kg." under the scenario name
    // "bad-off-topic". The fact check must be false and relevance at most 2.
    [TestMethod]
    public async Task AnOffTopicAnswerIsCaughtByRelevance()
    {
        await Task.CompletedTask;
        Assert.Fail("TODO-07: write this test.");
    }
}
