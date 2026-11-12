using Microsoft.Extensions.AI.Evaluation.Reporting;

namespace Returns.Tests.Support;

public sealed record Evaluated(Scenario Scenario, string Answer, EvaluationResult Result);

public static class EvalRunner
{
    /// <summary>
    /// The usual flow: ask the assistant through the caching client of the scenario run, then evaluate the answer.
    /// Both the answer and the judge's verdicts are cached, so a replay never needs a model.
    /// </summary>
    public static async Task<Evaluated> AskAndEvaluateAsync(ReportingConfiguration configuration, Scenario scenario)
    {
        await using var run = await configuration.CreateScenarioRunAsync(scenario.Name);

        var assistant = new ReturnsAssistant(run.ChatConfiguration!.ChatClient);
        var question = new ChatMessage(ChatRole.User, scenario.Question);
        var response = await assistant.AskAsync(scenario.Question);

        var result = await run.EvaluateAsync(question, response, EvalSupport.ContextFor(scenario));
        return new Evaluated(scenario, response.Text, result);
    }
}
