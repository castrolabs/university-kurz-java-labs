using Microsoft.EntityFrameworkCore;

namespace Ordering.Tests;

[TestClass]
[TestCategory("Integration")]
public sealed class OutboxTests : IDisposable
{
    private static readonly Guid CustomerId = Guid.NewGuid();

    // MSTest creates a new instance of the class for every test, so this field is private to one test.
    // A constructor (here, the field initializer) can set up a readonly field; Dispose runs after the test.
    private readonly TestDatabase _database = new();

    public void Dispose() => _database.Dispose();

    [TestMethod]
    public async Task TheOutboxRowIsSavedInTheSameTransactionAsTheOrder()
    {
        await using (var db = _database.NewContext(new DomainEventDispatcher().Register(new OrderPlacedHandler())))
        {
            db.Orders.Add(Order.Place(CustomerId, 10m, TimeProvider.System));
            await db.SaveChangesAsync();
        }

        await using var check = _database.Plain();
        var message = await check.OutboxMessages.SingleAsync();
        Assert.AreEqual(nameof(OrderPlacedIntegrationEvent), message.Type);
        Assert.AreEqual(1, await check.Orders.CountAsync());
    }

    [TestMethod]
    public async Task NoOrderMeansNoOutboxRowWhenTheSaveFails()
    {
        // The outbox handler runs first and adds its row; the exploding one then aborts the save.
        var dispatcher = new DomainEventDispatcher().Register(new OrderPlacedHandler()).Register(new ExplodingHandler());
        await using (var db = _database.NewContext(dispatcher))
        {
            db.Orders.Add(Order.Place(CustomerId, 10m, TimeProvider.System));
            await Assert.ThrowsExactlyAsync<InvalidOperationException>(() => db.SaveChangesAsync());
        }

        await using var check = _database.Plain();
        Assert.AreEqual(0, await check.OutboxMessages.CountAsync());
        Assert.AreEqual(0, await check.Orders.CountAsync());
    }
}
