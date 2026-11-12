using Microsoft.Extensions.AI.Evaluation.Quality;
using Returns.Tests.Support;

namespace Returns.Tests;

// Read src/Returns/ReturnsAssistant.cs and tests/Returns.Tests/Support first. The assistant answered every
// scenario once and the judge scored it; both are recorded in eval-cache/, so these tests take milliseconds and
// need no model. Nothing here calls Ollama unless you set EVAL_RECORD=1.
[TestClass]
public sealed class AnswerQualityTests
{
    // Results["refund-window"] is the answer and the evaluation of that scenario, already computed.
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

    // Handy: Results[name].Result.Get<NumericMetric>(RelevanceEvaluator.RelevanceMetricName).Value is a double?.
    // GroundednessEvaluator.GroundednessMetricName and CoherenceEvaluator.CoherenceMetricName work the same way;
    // ExpectedFactEvaluator.MetricName is a BooleanMetric.

    // TODO-00: The scores are on a 1 to 5 scale. Assert a threshold, never an exact value: the same answer may be
    // judged 4 today and 5 tomorrow. For "refund-window", relevance and groundedness must be at least 4.
    [TestMethod]
    public void AClearQuestionGetsARelevantAndGroundedAnswer()
    {
        Assert.Fail("TODO-00: write this test.");
    }

    // TODO-01: A judge that could not parse its own verdict leaves an Error diagnostic and no value. That is not
    // a pass. For every answerable scenario assert that EvaluationResult.ContainsDiagnostics(...) finds no
    // diagnostic with Severity == EvaluationDiagnosticSeverity.Error, and that the relevance Value is not null.
    [TestMethod]
    [DynamicData(nameof(AnswerableScenarios))]
    public void TheJudgeDidNotFailOnAnyScenario(Scenario scenario)
    {
        Assert.Fail("TODO-01: write this test.");
    }

    // TODO-02: The deterministic evaluator needs no model. For every answerable scenario the BooleanMetric called
    // ExpectedFactEvaluator.MetricName must be true. Put the scenario name and the answer in the failure message.
    [TestMethod]
    [DynamicData(nameof(AnswerableScenarios))]
    public void EveryAnswerContainsItsExpectedFact(Scenario scenario)
    {
        Assert.Fail("TODO-02: write this test.");
    }

    // TODO-03: "unknown" is a question the policy does not answer. The right answer is the exact sentence
    // ReturnsAssistant.Unknown, so assert on the text and on the fact check. Look at the relevance and
    // groundedness the judge gave it (print them) and do not gate on them: a correct refusal is scored 1.
    [TestMethod]
    public void AQuestionOutsideThePolicyIsRefusedWithTheExactSentence()
    {
        Assert.Fail("TODO-03: write this test.");
    }

    // TODO-04: One answer in the set was judged 2 for relevance, so "every score at least 4" is red and a gate that
    // tolerates nothing teaches the team to ignore it. Gate the answerable set instead: average relevance at
    // least 3.5, average groundedness at least 4.0, and at most one answer with relevance below 3.
    [TestMethod]
    public void TheSetPassesOnAverageAndNotOnEveryScore()
    {
        Assert.Fail("TODO-04: write this test.");
    }

    // TODO-05: Find the weak answer: it is "shipping-defective". Its text contains "correction" (the model
    // contradicted itself), the fact check is still green, and relevance is at most 2. Assert all three. This is
    // why a deterministic check and a judge are used together.
    [TestMethod]
    public void TheFactCheckAloneMissesAnAnswerThatContradictsItself()
    {
        Assert.Fail("TODO-05: write this test.");
    }
}
