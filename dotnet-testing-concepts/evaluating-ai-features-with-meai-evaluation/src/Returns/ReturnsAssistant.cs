using Microsoft.Extensions.AI;

namespace Returns;

/// <summary>
/// Answers customer questions about the returns policy. The policy text is the only source it may use.
/// </summary>
public sealed class ReturnsAssistant(IChatClient chat)
{
    // Line endings are fixed on purpose: the text is part of the cache key, and a checkout with CRLF
    // would otherwise turn every recorded request into a cache miss.
    public static readonly string Policy = """
        Returns policy of Brightside Outdoor.
        You can return an unused item within 30 days of delivery for a full refund.
        Refunds go back to the original payment method and take 5 business days to appear.
        Return shipping is free for defective items. For all other returns the customer pays 6 EUR.
        Sale items can only be exchanged, never refunded.
        Tents that have been pitched are considered used and cannot be returned.
        Gift cards are not refundable.
        """.ReplaceLineEndings("\n");

    public const string Unknown = "I don't know, please contact support.";

    public static readonly string SystemPrompt =
        "You are the support assistant of Brightside Outdoor. Answer in at most three sentences, using only the " +
        "returns policy below. If the policy does not say, answer exactly: " + Unknown + "\n\n" + Policy;

    public ChatOptions Options { get; } = new() { Temperature = 0f, Seed = 42 };

    public IReadOnlyList<ChatMessage> BuildMessages(string question) =>
    [
        new(ChatRole.System, SystemPrompt),
        new(ChatRole.User, question),
    ];

    public Task<ChatResponse> AskAsync(string question, CancellationToken ct = default) =>
        chat.GetResponseAsync(BuildMessages(question), Options, ct);
}
