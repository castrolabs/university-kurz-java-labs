using System.Text.Json;

namespace Messaging;

public static class OrderService
{
    public static async Task<Guid> PlaceOrderAsync(
        AppDbContext db, string customer, decimal total, TimeProvider clock,
        Guid? orderId = null, CancellationToken ct = default)
    {
        // TODO-00: Create the Order (Guid.CreateVersion7() when orderId is null) and an OrderPlaced event with a new
        // MessageId (Guid.CreateVersion7()). Add the order AND an OutboxMessage (Type = nameof(OrderPlaced),
        // Payload = JsonSerializer.Serialize(evt), NextAttemptAtMs = now in unix ms) to the same context, then call
        // SaveChangesAsync ONCE. Return the order id.
        // Hint: one SaveChanges is one transaction. Two SaveChanges would reopen the crash window.
        await Task.CompletedTask;
        throw new NotImplementedException("Not implemented yet.");
    }
}
