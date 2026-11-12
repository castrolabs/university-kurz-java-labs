namespace Returns.Tests.Support;

/// <summary>
/// The "model" used when the cache is replayed. Any request that reaches it is a cache miss: the prompt, the
/// answer or an evaluator changed since the cache was recorded. It counts the misses so a test can say so.
/// </summary>
public sealed class ReplayOnlyChatClient : IChatClient
{
    public int Calls { get; private set; }

    public Task<ChatResponse> GetResponseAsync(IEnumerable<ChatMessage> messages, ChatOptions? options = null,
        CancellationToken cancellationToken = default)
    {
        Calls++;
        throw new InvalidOperationException(
            "Cache miss: this request is not in eval-cache/. Re-record it with EVAL_RECORD=1 (see the README).");
    }

    public IAsyncEnumerable<ChatResponseUpdate> GetStreamingResponseAsync(IEnumerable<ChatMessage> messages,
        ChatOptions? options = null, CancellationToken cancellationToken = default) =>
        throw new NotSupportedException();

    public object? GetService(Type serviceType, object? serviceKey = null) =>
        serviceType == typeof(ChatClientMetadata)
            ? new ChatClientMetadata("ollama", new Uri("http://localhost:11434"), "qwen3:4b-instruct")
            : null;

    public void Dispose() { }
}
