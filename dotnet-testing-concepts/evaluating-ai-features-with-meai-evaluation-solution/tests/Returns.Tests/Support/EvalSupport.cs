using Microsoft.Extensions.AI.Evaluation.Quality;
using Microsoft.Extensions.AI.Evaluation.Reporting;
using Microsoft.Extensions.AI.Evaluation.Reporting.Storage;
using OllamaSharp;

namespace Returns.Tests.Support;

public static class EvalSupport
{
    /// <summary>EVAL_RECORD=1 talks to the local Ollama and writes eval-cache/. Otherwise only the cache is read.</summary>
    public static bool Recording => Environment.GetEnvironmentVariable("EVAL_RECORD") == "1";

    public static readonly string LabRoot = FindLabRoot();

    /// <summary>Committed: the recorded answers and judge verdicts.</summary>
    public static string CacheDir => Path.Combine(LabRoot, "eval-cache");

    /// <summary>Not committed: scores and reports of the last run.</summary>
    public static string ResultsDir => Path.Combine(LabRoot, "eval-results");

    public static IEvaluator[] ModelJudged() =>
        [new RelevanceEvaluator(), new CoherenceEvaluator(), new GroundednessEvaluator()];

    public static IEvaluator[] All() => [.. ModelJudged(), new ExpectedFactEvaluator()];

    public static (ReportingConfiguration Configuration, ReplayOnlyChatClient? Replay) CreateConfiguration(
        IEnumerable<IEvaluator>? evaluators = null, string executionName = "lab")
    {
        ReplayOnlyChatClient? replay = null;
        IChatClient model;
        if (Recording)
        {
            model = new OllamaApiClient(new Uri("http://localhost:11434"), "qwen3:4b-instruct");
        }
        else
        {
            replay = new ReplayOnlyChatClient();
            model = replay;
        }

        // 10 years: the default is 14 days, and a recorded cache that expires turns a green build red for nothing.
        var configuration = new ReportingConfiguration(
            evaluators ?? All(),
            new DiskBasedResultStore(ResultsDir),
            new ChatConfiguration(model),
            new DiskBasedResponseCacheProvider(CacheDir, TimeSpan.FromDays(3650)),
            executionName: executionName);
        return (configuration, replay);
    }

    /// <summary>The evaluator context a scenario needs: the policy as ground truth and the expected fact.</summary>
    public static EvaluationContext[] ContextFor(Scenario scenario) =>
    [
        new GroundednessEvaluatorContext(ReturnsAssistant.Policy),
        new ExpectedFactContext(scenario.FactPattern),
    ];

    private static string FindLabRoot()
    {
        var dir = new DirectoryInfo(AppContext.BaseDirectory);
        while (dir is not null && !dir.GetFiles("*.slnx").Any())
        {
            dir = dir.Parent;
        }
        return dir?.FullName ?? throw new InvalidOperationException("Could not find the lab root (the .slnx file).");
    }
}
