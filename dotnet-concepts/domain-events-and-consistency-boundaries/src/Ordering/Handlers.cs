using Microsoft.EntityFrameworkCore;

namespace Ordering;

// In-transaction: translates the domain event into an integration event in the outbox.
public sealed class OrderPlacedHandler : IDomainEventHandler<OrderPlaced>
{
    public Task HandleAsync(OrderPlaced e, OrdersDbContext db, CancellationToken ct)
    {
        // TODO-02: Translate OrderPlaced into an OrderPlacedIntegrationEvent (use one new Guid as both the
        // MessageId and the outbox row id) and add an OutboxMessage to db.OutboxMessages.
        // Hint: do NOT call SaveChanges here. The outer SaveChanges commits the order and the row together.
        throw new NotImplementedException("Not implemented yet.");
    }
}

// After the commit, in its own transaction, on its own context: another aggregate.
public sealed class LoyaltyConsumer(Func<OrdersDbContext> newContext)
{
    public const string Name = "loyalty";

    public async Task HandleAsync(OrderPlacedIntegrationEvent e, CancellationToken ct)
    {
        // TODO-03: Open a context with newContext() and award points to the customer (create the Customer if
        // it does not exist yet) with a single SaveChangesAsync.
        // Hint: the same message can be delivered twice. Record a ProcessedMessage(e.MessageId, Name) in the
        // same transaction and skip the message if it is already there.
        await Task.CompletedTask;
        throw new NotImplementedException("Not implemented yet.");
    }
}
