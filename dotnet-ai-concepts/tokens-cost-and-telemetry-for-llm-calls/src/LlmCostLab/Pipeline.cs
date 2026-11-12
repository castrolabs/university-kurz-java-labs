using System.Runtime.CompilerServices;
using Microsoft.Extensions.AI;
using Microsoft.Extensions.Caching.Distributed;

namespace LlmCostLab;

/// <summary>Sums the usage of every call that reaches the client below it.</summary>
public sealed class UsageMeter(IChatClient inner) : DelegatingChatClient(inner)
{
    public UsageDetails Total { get; } = new();

    public int Calls { get; private set; }

    public override async Task<ChatResponse> GetResponseAsync(
        IEnumerable<ChatMessage> messages, ChatOptions? options = null, CancellationToken cancellationToken = default)
    {
        var response = await base.GetResponseAsync(messages, options, cancellationToken);
        // TODO-05: Count the call and add response.Usage to Total (Total.Add(...)).
        //          A provider may report no usage: the call still counts.
        return response;
    }

    public override async IAsyncEnumerable<ChatResponseUpdate> GetStreamingResponseAsync(
        IEnumerable<ChatMessage> messages, ChatOptions? options = null,
        [EnumeratorCancellation] CancellationToken cancellationToken = default)
    {
        await foreach (var update in base.GetStreamingResponseAsync(messages, options, cancellationToken))
        {
            // TODO-06: A stream carries its usage in UsageContent items (update.Contents.OfType<UsageContent>()).
            //          Add them up, and when the stream ends count the call once.
            yield return update;
        }
    }

    public decimal Cost(ModelPrice price)
    {
        // TODO-07: Estimate the cost of Total with CostCalculator.
        return 0m;
    }
}

public static class LlmPipeline
{
    public static IChatClient Build(UsageMeter meter, IDistributedCache cache, string telemetrySource)
    {
        // TODO-08: Wrap the meter with ChatClientBuilder: UseDistributedCache(cache) first (the first Use
        //          is the outermost layer), then UseOpenTelemetry(sourceName: telemetrySource).
        //          With this order a cache hit reaches neither the telemetry nor the meter.
        return meter;
    }
}
