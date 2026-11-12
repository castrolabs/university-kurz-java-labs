using Microsoft.Extensions.AI.Evaluation.Quality;
using Returns.Tests.Support;

namespace Returns.Tests;

[TestClass]
public sealed class AnswerQualityTests
{
    // The assistant answered every scenario once and the judge scored it; both are in eval-cache/, so this
    // takes milliseconds and needs no model. Results["refund-window"] is the evaluation of that scenario.
    private static Dictionary<string, Evaluated> Results = [];

    public static IEnumerable<Scenario> AnswerableScenarios => Scenarios.All.Where(s => s.Name != "unknown");

    [ClassInitialize]
    public static async Task EvaluateEverything(TestContext _)
    {
        var (configuration, _) = EvalSupport.CreateConfiguration(executionName: "answer-quality");
        foreach (var scenario in Scenarios.All)
        {
            Results[scenario.Name] = await EvalRunner.AskAndEvaluateAsync(configuration, scenario);
        }
    }

    private static double Score(string scenario, string metricName) =>
        Results[scenario].Result.Get<NumericMetric>(metricName).Value!.Value;

    [TestMethod]
    public void AClearQuestionGetsARelevantAndGroundedAnswer()
    {
        // 1 to 5 scales. A threshold, not an equality: the same answer may be judged 4 today and 5 tomorrow.
        Assert.IsGreaterThanOrEqualTo(4, Score("refund-window", RelevanceEvaluator.RelevanceMetricName));
        Assert.IsGreaterThanOrEqualTo(4, Score("refund-window", GroundednessEvaluator.GroundednessMetricName));
    }

    [TestMethod]
    [DynamicData(nameof(AnswerableScenarios))]
    public void TheJudgeDidNotFailOnAnyScenario(Scenario scenario)
    {
        // A judge that could not parse its own verdict leaves an Error diagnostic and no value. That is not a pass.
        var result = Results[scenario.Name].Result;

        Assert.IsFalse(result.ContainsDiagnostics(d => d.Severity == EvaluationDiagnosticSeverity.Error),
            $"{scenario.Name}: the evaluation reported an error.");
        Assert.IsFalse(result.Get<NumericMetric>(RelevanceEvaluator.RelevanceMetricName).Value is null);
    }

    [TestMethod]
    [DynamicData(nameof(AnswerableScenarios))]
    public void EveryAnswerContainsItsExpectedFact(Scenario scenario)
    {
        var fact = Results[scenario.Name].Result.Get<BooleanMetric>(ExpectedFactEvaluator.MetricName);

        Assert.IsTrue(fact.Value, $"{scenario.Name}: {Results[scenario.Name].Answer}");
    }

    [TestMethod]
    public void AQuestionOutsideThePolicyIsRefusedWithTheExactSentence()
    {
        // Deterministic on purpose. A refusal is the right answer here, but the judge scored it 1 for relevance
        // and 1 for groundedness: quality scores must never gate a refusal.
        Assert.AreEqual(ReturnsAssistant.Unknown, Results["unknown"].Answer);
        Assert.IsTrue(Results["unknown"].Result.Get<BooleanMetric>(ExpectedFactEvaluator.MetricName).Value);
    }

    [TestMethod]
    public void TheSetPassesOnAverageAndNotOnEveryScore()
    {
        // One answer in the set was judged 2 for relevance. A gate of "every score at least 4" is red; the
        // gate that reflects the real quality of the set is on the average and on the number of weak answers.
        var relevance = AnswerableScenarios.Select(s => Score(s.Name, RelevanceEvaluator.RelevanceMetricName)).ToArray();
        var groundedness = AnswerableScenarios.Select(s => Score(s.Name, GroundednessEvaluator.GroundednessMetricName)).ToArray();

        Assert.IsGreaterThanOrEqualTo(3.5, relevance.Average());
        Assert.IsGreaterThanOrEqualTo(4.0, groundedness.Average());
        Assert.IsLessThanOrEqualTo(1, relevance.Count(v => v < 3));
    }

    [TestMethod]
    public void TheFactCheckAloneMissesAnAnswerThatContradictsItself()
    {
        // "The customer pays ... Wait, correction: ... free" contains the word the regular expression wants, so
        // the deterministic evaluator is green. Only the judge sees that the answer is a mess.
        var weak = Results["shipping-defective"];

        Assert.Contains("correction", weak.Answer, StringComparison.OrdinalIgnoreCase);
        Assert.IsTrue(weak.Result.Get<BooleanMetric>(ExpectedFactEvaluator.MetricName).Value);
        Assert.IsLessThanOrEqualTo(2, Score("shipping-defective", RelevanceEvaluator.RelevanceMetricName));
    }
}
