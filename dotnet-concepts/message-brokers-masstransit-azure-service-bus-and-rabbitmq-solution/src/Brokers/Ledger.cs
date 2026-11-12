using System.Collections.Concurrent;

namespace Brokers;

// The "database" of the lab: what the consumers did. Singleton, thread safe, no I/O.
public sealed class Ledger
{
    public ConcurrentDictionary<Guid, byte> Charges { get; } = new();   // keyed by order: charging twice is a no-op
    public ConcurrentBag<Guid> Shipments { get; } = [];
    public ConcurrentBag<Guid> DeadLetters { get; } = [];
    public ConcurrentBag<Guid> Captured { get; } = [];
    private readonly ConcurrentDictionary<(Guid MessageId, string Consumer), byte> _inbox = new();

    // How many times the shipping consumer throws before it works (a flaky downstream).
    public int FailShippingTimes;
    public int ShippingAttempts;

    // Fails the billing consumer after it published, before it recorded the message as processed.
    public int FailBillingTimes;
    public int BillingAttempts;
    public int BillingDeliveries;   // every time Consume starts, duplicates included

    public bool IsProcessed(Guid messageId, string consumer) => _inbox.ContainsKey((messageId, consumer));
    public void MarkProcessed(Guid messageId, string consumer) => _inbox.TryAdd((messageId, consumer), 0);
}
