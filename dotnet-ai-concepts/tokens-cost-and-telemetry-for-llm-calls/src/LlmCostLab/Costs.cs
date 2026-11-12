using Microsoft.Extensions.AI;
using Microsoft.ML.Tokenizers;

namespace LlmCostLab;

/// <summary>Price per one million tokens. The numbers are configuration, never constants of the code.</summary>
public sealed record ModelPrice(decimal InputPerMillionTokens, decimal OutputPerMillionTokens);

public static class CostCalculator
{
    public static decimal Estimate(UsageDetails? usage, ModelPrice price)
    {
        // TODO-00: Price input and output tokens separately: tokens * price / 1,000,000.
        //          A null usage, or a null count, costs zero. Use decimal, never double, for money.
        return 0m;
    }
}

/// <summary>Counts tokens with a tiktoken vocabulary. It is an estimate for any other model.</summary>
public sealed class TokenCounter(Tokenizer tokenizer)
{
    public static TokenCounter O200k() => new(TiktokenTokenizer.CreateForModel("gpt-4o"));

    public int Count(string? text)
    {
        // TODO-01: Return 0 for null or empty text, otherwise tokenizer.CountTokens(text).
        return -1;
    }
}

/// <summary>Keeps a prompt inside the context window, leaving room for the answer.</summary>
public sealed class ContextGuard(TokenCounter counter, int contextWindow, int reservedForOutput)
{
    /// <summary>Rough per-message cost of role markers and separators.</summary>
    public const int PerMessageOverhead = 4;

    public int PromptBudget => contextWindow - reservedForOutput;

    public int CountTokens(IEnumerable<ChatMessage> messages)
    {
        // TODO-02: Sum counter.Count(message.Text) + PerMessageOverhead over the messages.
        return 0;
    }

    public bool Fits(IEnumerable<ChatMessage> messages)
    {
        // TODO-03: The prompt fits when its tokens are at most PromptBudget.
        return true;
    }

    /// <summary>Drops the oldest non-system messages until the prompt fits.</summary>
    public List<ChatMessage> Trim(IEnumerable<ChatMessage> messages)
    {
        // TODO-04: Remove the oldest message whose role is not System until Fits(...) is true.
        //          Never remove the last message. When only the system prompt and the last message
        //          are left and they still do not fit, throw InvalidOperationException.
        return messages.ToList();
    }
}
