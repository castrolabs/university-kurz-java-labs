using Microsoft.Extensions.AI;
using Microsoft.ML.Tokenizers;

namespace LlmCostLab;

/// <summary>Price per one million tokens. The numbers are configuration, never constants of the code.</summary>
public sealed record ModelPrice(decimal InputPerMillionTokens, decimal OutputPerMillionTokens);

public static class CostCalculator
{
    public static decimal Estimate(UsageDetails? usage, ModelPrice price)
    {
        if (usage is null)
        {
            return 0m;
        }

        decimal input = usage.InputTokenCount ?? 0;
        decimal output = usage.OutputTokenCount ?? 0;
        return (input * price.InputPerMillionTokens + output * price.OutputPerMillionTokens) / 1_000_000m;
    }
}

/// <summary>Counts tokens with a tiktoken vocabulary. It is an estimate for any other model.</summary>
public sealed class TokenCounter(Tokenizer tokenizer)
{
    public static TokenCounter O200k() => new(TiktokenTokenizer.CreateForModel("gpt-4o"));

    public int Count(string? text) => string.IsNullOrEmpty(text) ? 0 : tokenizer.CountTokens(text);
}

/// <summary>Keeps a prompt inside the context window, leaving room for the answer.</summary>
public sealed class ContextGuard(TokenCounter counter, int contextWindow, int reservedForOutput)
{
    /// <summary>Rough per-message cost of role markers and separators.</summary>
    public const int PerMessageOverhead = 4;

    public int PromptBudget => contextWindow - reservedForOutput;

    public int CountTokens(IEnumerable<ChatMessage> messages) =>
        messages.Sum(m => counter.Count(m.Text) + PerMessageOverhead);

    public bool Fits(IEnumerable<ChatMessage> messages) => CountTokens(messages) <= PromptBudget;

    /// <summary>Drops the oldest non-system messages until the prompt fits.</summary>
    public List<ChatMessage> Trim(IEnumerable<ChatMessage> messages)
    {
        var kept = messages.ToList();
        while (!Fits(kept))
        {
            int oldest = kept.FindIndex(m => m.Role != ChatRole.System);
            if (oldest < 0 || oldest == kept.Count - 1)
            {
                throw new InvalidOperationException(
                    $"The system prompt and the latest message need {CountTokens(kept)} tokens, the budget is {PromptBudget}.");
            }

            kept.RemoveAt(oldest);
        }

        return kept;
    }
}
