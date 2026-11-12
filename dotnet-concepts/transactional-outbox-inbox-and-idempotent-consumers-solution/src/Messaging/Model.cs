namespace Messaging;

public sealed class Order
{
    public Guid Id { get; set; }
    public string Customer { get; set; } = "";
    public decimal Total { get; set; }
}

// The effect of the consumer: it must exist exactly once per OrderPlaced message.
public sealed class Shipment
{
    public Guid Id { get; set; }
    public Guid OrderId { get; set; }
}

// Integration event: public contract, primitives only. MessageId is the deduplication key.
public sealed record OrderPlaced(Guid MessageId, Guid OrderId, string Customer, decimal Total);

// Written in the same transaction as the business data. Seq gives a total order to the relay.
public sealed class OutboxMessage
{
    public long Seq { get; set; }                 // autoincrement: the relay order
    public Guid MessageId { get; set; }           // what travels to the broker and the inbox
    public string Type { get; set; } = "";
    public string Payload { get; set; } = "";
    public long? PublishedAtMs { get; set; }      // null = still pending
    public int Attempts { get; set; }
    public long NextAttemptAtMs { get; set; }     // backoff: not due before this instant
    public long? DeadLetteredAtMs { get; set; }   // give up: needs a human or a replay tool
    public string? LastError { get; set; }
}

// Inbox: which messages a consumer already handled. (MessageId, Consumer) is the primary key.
public sealed class InboxMessage
{
    public Guid MessageId { get; set; }
    public string Consumer { get; set; } = "";
    public long ProcessedAtMs { get; set; }
}
