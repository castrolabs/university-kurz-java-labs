using System.Runtime.CompilerServices;
using Microsoft.Extensions.AI;
using Microsoft.Extensions.Caching.Distributed;

namespace LlmCostLab;

/// <summary>Sums the usage of every call that reaches the client below it.</summary>
public sealed class UsageMeter(IChatClient inner) : DelegatingChatClient(inner)
{
    private readonly object _gate = new();

    public UsageDetails Total { get; } = new();

    public int Calls { get; private set; }

    public override async Task<ChatResponse> GetResponseAsync(
        IEnumerable<ChatMessage> messages, ChatOptions? options = null, CancellationToken cancellationToken = default)
    {
        var response = await base.GetResponseAsync(messages, options, cancellationToken);
        Record(response.Usage);
        return response;
    }

    public override async IAsyncEnumerable<ChatResponseUpdate> GetStreamingResponseAsync(
        IEnumerable<ChatMessage> messages, ChatOptions? options = null,
        [EnumeratorCancellation] CancellationToken cancellationToken = default)
    {
        UsageDetails streamed = new();
        bool any = false;
        await foreach (var update in base.GetStreamingResponseAsync(messages, options, cancellationToken))
        {
            foreach (var usage in update.Contents.OfType<UsageContent>())
            {
                streamed.Add(usage.Details);
                any = true;
            }

            yield return update;
        }

        Record(any ? streamed : null);
    }

    public decimal Cost(ModelPrice price) => CostCalculator.Estimate(Total, price);

    private void Record(UsageDetails? usage)
    {
        lock (_gate)
        {
            Calls++;
            if (usage is not null)
            {
                Total.Add(usage);
            }
        }
    }
}

public static class LlmPipeline
{
    /// <summary>
    /// cache -> OpenTelemetry -> meter -> model. A cache hit never reaches the telemetry or the meter,
    /// so neither counts tokens nobody paid for.
    /// </summary>
    public static IChatClient Build(UsageMeter meter, IDistributedCache cache, string telemetrySource) =>
        new ChatClientBuilder(meter)
            .UseDistributedCache(cache)                        // first Use = outermost layer
            .UseOpenTelemetry(sourceName: telemetrySource)
            .Build();
}
