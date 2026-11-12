using System.Runtime.CompilerServices;
using Microsoft.Extensions.AI;

namespace LlmCostLab.Tests;

/// <summary>
/// Deterministic IChatClient: fixed answer, fixed usage, counts calls. Swap it for
/// new OllamaApiClient(new Uri("http://localhost:11434"), "qwen3:4b-instruct") to run against Ollama.
/// </summary>
public sealed class ScriptedChatClient(UsageDetails? usage = null, bool reportUsage = true) : IChatClient
{
    private readonly UsageDetails? _usage = reportUsage
        ? usage ?? new UsageDetails { InputTokenCount = 100, OutputTokenCount = 20, TotalTokenCount = 120 }
        : null;

    public int Calls { get; private set; }

    public Task<ChatResponse> GetResponseAsync(
        IEnumerable<ChatMessage> messages, ChatOptions? options = null, CancellationToken cancellationToken = default)
    {
        Calls++;
        return Task.FromResult(new ChatResponse(new ChatMessage(ChatRole.Assistant, "Paris"))
        {
            ModelId = "fake-model",
            FinishReason = ChatFinishReason.Stop,
            Usage = _usage?.Clone(),
        });
    }

    public async IAsyncEnumerable<ChatResponseUpdate> GetStreamingResponseAsync(
        IEnumerable<ChatMessage> messages, ChatOptions? options = null,
        [EnumeratorCancellation] CancellationToken cancellationToken = default)
    {
        Calls++;
        yield return new ChatResponseUpdate(ChatRole.Assistant, "Par");
        yield return new ChatResponseUpdate(ChatRole.Assistant, "is");
        if (_usage is not null)
        {
            yield return new ChatResponseUpdate { Contents = [new UsageContent(_usage.Clone())] };
        }

        await Task.CompletedTask;
    }

    public object? GetService(Type serviceType, object? serviceKey = null) =>
        serviceType == typeof(ChatClientMetadata) && serviceKey is null
            ? new ChatClientMetadata("fake", new Uri("http://fake.test"), "fake-model")
            : null;

    public void Dispose()
    {
    }
}

internal static class UsageExtensions
{
    public static UsageDetails Clone(this UsageDetails u) => new()
    {
        InputTokenCount = u.InputTokenCount,
        OutputTokenCount = u.OutputTokenCount,
        TotalTokenCount = u.TotalTokenCount,
    };
}
