using System.Text.Json;

namespace Messaging;

public static class OrderService
{
    // The business write and the outbox row share ONE SaveChanges, hence one transaction.
    public static async Task<Guid> PlaceOrderAsync(
        AppDbContext db, string customer, decimal total, TimeProvider clock,
        Guid? orderId = null, CancellationToken ct = default)
    {
        var order = new Order { Id = orderId ?? Guid.CreateVersion7(), Customer = customer, Total = total };
        var evt = new OrderPlaced(Guid.CreateVersion7(), order.Id, customer, total);

        db.Orders.Add(order);
        db.Outbox.Add(new OutboxMessage
        {
            MessageId = evt.MessageId,
            Type = nameof(OrderPlaced),
            Payload = JsonSerializer.Serialize(evt),
            NextAttemptAtMs = clock.GetUtcNow().ToUnixTimeMilliseconds(),
        });

        await db.SaveChangesAsync(ct);
        return order.Id;
    }
}
