using System.Text.Json;

namespace Ordering;

public interface IDomainEvent;

public abstract class AggregateRoot
{
    private readonly List<IDomainEvent> _events = [];
    public IReadOnlyList<IDomainEvent> DomainEvents => _events;
    protected void Raise(IDomainEvent e) => _events.Add(e);
    public void ClearDomainEvents() => _events.Clear();
}

// Internal to the module: may carry rich types.
public sealed record OrderPlaced(Guid OrderId, Guid CustomerId, decimal Total, DateTimeOffset At) : IDomainEvent;

// Public contract between modules: primitives only.
public sealed record OrderPlacedIntegrationEvent(Guid MessageId, Guid OrderId, Guid CustomerId, decimal Total);

public sealed class OutboxMessage
{
    public Guid Id { get; private set; }
    public string Type { get; private set; } = "";
    public string Payload { get; private set; } = "";

    public static OutboxMessage From(Guid id, object integrationEvent) => new()
    {
        Id = id,
        Type = integrationEvent.GetType().Name,
        Payload = JsonSerializer.Serialize(integrationEvent, integrationEvent.GetType()),
    };
}

// Inbox: remembers which messages a consumer already handled.
public sealed record ProcessedMessage(Guid MessageId, string Consumer);
