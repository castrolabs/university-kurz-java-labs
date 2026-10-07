using Microsoft.EntityFrameworkCore;

namespace Ordering;

// In-transaction: translates the domain event into an integration event in the outbox.
public sealed class OrderPlacedHandler : IDomainEventHandler<OrderPlaced>
{
    public Task HandleAsync(OrderPlaced e, OrdersDbContext db, CancellationToken ct)
    {
        var messageId = Guid.CreateVersion7();
        db.OutboxMessages.Add(OutboxMessage.From(
            messageId, new OrderPlacedIntegrationEvent(messageId, e.OrderId, e.CustomerId, e.Total)));
        return Task.CompletedTask; // no SaveChanges: the outer one commits order and outbox row together
    }
}

// After the commit, in its own transaction, on its own context: another aggregate.
public sealed class LoyaltyConsumer(Func<OrdersDbContext> newContext)
{
    public const string Name = "loyalty";

    public async Task HandleAsync(OrderPlacedIntegrationEvent e, CancellationToken ct)
    {
        await using var db = newContext();

        if (await db.ProcessedMessages.AnyAsync(p => p.MessageId == e.MessageId && p.Consumer == Name, ct))
            return; // at-least-once delivery: a repeat is not an error, it is a no-op

        var customer = await db.Customers.FindAsync([e.CustomerId], ct) ?? db.Customers.Add(new Customer(e.CustomerId)).Entity;
        customer.AwardPoints(e.Total);
        db.ProcessedMessages.Add(new ProcessedMessage(e.MessageId, Name));

        await db.SaveChangesAsync(ct); // points and inbox row in one transaction
    }
}
