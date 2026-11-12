using System.Threading.Channels;

namespace Messaging;

public sealed record Envelope(Guid MessageId, string Type, string Payload, int DeliveryCount = 1);

public sealed class BrokerUnavailableException() : Exception("broker unavailable");

public interface IBroker
{
    ValueTask PublishAsync(Envelope envelope, CancellationToken ct);
}

// A Channel<T> pretending to be RabbitMQ / Service Bus, with the faults the lab needs.
public sealed class InMemoryBroker : IBroker
{
    private readonly Channel<Envelope> _queue = Channel.CreateUnbounded<Envelope>();
    private readonly List<Envelope> _deadLetters = [];
    private readonly object _gate = new();
    private int _failures;

    public ChannelReader<Envelope> Reader => _queue.Reader;

    public IReadOnlyList<Envelope> DeadLetters { get { lock (_gate) return [.. _deadLetters]; } }

    // The next n publishes throw, like a broker that is down or a network that dropped.
    public void FailNextPublishes(int n) { lock (_gate) _failures = n; }

    public ValueTask PublishAsync(Envelope envelope, CancellationToken ct)
    {
        lock (_gate)
        {
            if (_failures > 0) { _failures--; throw new BrokerUnavailableException(); }
        }
        return _queue.Writer.WriteAsync(envelope, ct);
    }

    // What the broker does when a consumer fails: put the message back with a higher delivery count.
    public ValueTask RedeliverAsync(Envelope envelope, CancellationToken ct) =>
        _queue.Writer.WriteAsync(envelope with { DeliveryCount = envelope.DeliveryCount + 1 }, ct);

    public void DeadLetter(Envelope envelope) { lock (_gate) _deadLetters.Add(envelope); }
}
