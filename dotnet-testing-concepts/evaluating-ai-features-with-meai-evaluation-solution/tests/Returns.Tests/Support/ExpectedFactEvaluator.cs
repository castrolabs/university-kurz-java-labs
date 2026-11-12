using System.Text.RegularExpressions;

namespace Returns.Tests.Support;

/// <summary>What the answer must contain, written as a regular expression.</summary>
public sealed class ExpectedFactContext(string pattern) : EvaluationContext("Expected fact", pattern)
{
    public string Pattern { get; } = pattern;
}

/// <summary>
/// A deterministic evaluator: no model, no cache, no cost. It checks that the answer contains the expected
/// fact and reports it as a boolean metric, so it shows up in the same report as the model-judged metrics.
/// </summary>
public sealed class ExpectedFactEvaluator : IEvaluator
{
    public const string MetricName = "Expected fact";

    public IReadOnlyCollection<string> EvaluationMetricNames => [MetricName];

    public ValueTask<EvaluationResult> EvaluateAsync(IEnumerable<ChatMessage> messages, ChatResponse modelResponse,
        ChatConfiguration? chatConfiguration = null, IEnumerable<EvaluationContext>? additionalContext = null,
        CancellationToken cancellationToken = default)
    {
        var metric = new BooleanMetric(MetricName);
        var expected = additionalContext?.OfType<ExpectedFactContext>().FirstOrDefault();

        if (expected is null)
        {
            metric.AddDiagnostics(EvaluationDiagnostic.Error("Pass an ExpectedFactContext."));
            return ValueTask.FromResult(new EvaluationResult(metric));
        }

        var found = Regex.IsMatch(modelResponse.Text, expected.Pattern, RegexOptions.IgnoreCase);
        metric.Value = found;
        metric.Reason = found ? $"The answer matches /{expected.Pattern}/." : $"The answer does not match /{expected.Pattern}/.";
        metric.Interpretation = new EvaluationMetricInterpretation(
            found ? EvaluationRating.Good : EvaluationRating.Unacceptable, failed: !found);
        return ValueTask.FromResult(new EvaluationResult(metric));
    }
}
