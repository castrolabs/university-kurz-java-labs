using Microsoft.Extensions.AI;
using OllamaSharp;
using WorkflowLab;

namespace WorkflowLab.Tests;

public class BonusTests
{
    private sealed class EchoClient : IChatClient
    {
        public int Calls;

        public Task<ChatResponse> GetResponseAsync(IEnumerable<ChatMessage> messages, ChatOptions? options = null,
            CancellationToken cancellationToken = default)
        {
            Interlocked.Increment(ref Calls);
            string last = messages.Last().Text;
            return Task.FromResult(new ChatResponse(new ChatMessage(ChatRole.Assistant, $"echo({last})")));
        }

        public async IAsyncEnumerable<ChatResponseUpdate> GetStreamingResponseAsync(IEnumerable<ChatMessage> messages,
            ChatOptions? options = null, [System.Runtime.CompilerServices.EnumeratorCancellation] CancellationToken cancellationToken = default)
        {
            var response = await GetResponseAsync(messages, options, cancellationToken);
            yield return new ChatResponseUpdate(ChatRole.Assistant, response.Text);
        }

        public object? GetService(Type serviceType, object? serviceKey = null) => null;

        public void Dispose() { }
    }

    [Fact]
    public async Task Writer_then_editor_runs_one_model_call_each_in_order()
    {
        var client = new EchoClient();

        var messages = await AgentPipelines.RunAsync(AgentPipelines.BuildWriterEditor(client), "Porto");

        Assert.Equal(2, client.Calls);
        Assert.Equal("echo(echo(Porto))", messages[^1].Text);
    }

    [FactIfOllama]
    public async Task Writer_then_editor_with_a_real_local_model()
    {
        IChatClient client = new OllamaApiClient(new Uri("http://localhost:11434"), "qwen3:4b-instruct");

        var messages = await AgentPipelines.RunAsync(AgentPipelines.BuildWriterEditor(client), "the city of Porto");

        Assert.Equal(ChatRole.Assistant, messages[^1].Role);
        Assert.False(string.IsNullOrWhiteSpace(messages[^1].Text));
    }
}

public sealed class FactIfOllamaAttribute : FactAttribute
{
    public FactIfOllamaAttribute()
    {
        if (Environment.GetEnvironmentVariable("RUN_OLLAMA") != "1")
        {
            Skip = "Set RUN_OLLAMA=1 and run `ollama pull qwen3:4b-instruct` to enable.";
        }
    }
}
