using Microsoft.EntityFrameworkCore;

namespace Ordering.Tests;

// The interceptor needs a real EF Core pipeline, so these tests use SQLite in memory.
[TestClass]
public sealed class DomainEventsInterceptorTests : IDisposable
{
    private static readonly Guid CustomerId = Guid.NewGuid();

    // MSTest creates a new instance of the class for every test, so this field is private to one test.
    // A constructor (here, the field initializer) can set up a readonly field; Dispose runs after the test.
    private readonly TestDatabase _database = new();

    public void Dispose() => _database.Dispose();

    [TestMethod]
    [TestCategory("Integration")]
    public async Task SavingDispatchesTheEventsOfTheTrackedAggregates()
    {
        var recorder = new RecordingHandler();
        await using var db = _database.NewContext(new DomainEventDispatcher().Register(recorder));

        db.Orders.Add(Order.Place(CustomerId, 10m, TimeProvider.System));
        await db.SaveChangesAsync();

        Assert.HasCount(1, recorder.Seen);
    }

    [TestMethod]
    [TestCategory("Integration")]
    public async Task EventsAreClearedSoASecondSaveDoesNotDispatchThemAgain()
    {
        var recorder = new RecordingHandler();
        await using var db = _database.NewContext(new DomainEventDispatcher().Register(recorder));
        var order = Order.Place(CustomerId, 10m, TimeProvider.System);
        db.Orders.Add(order);

        await db.SaveChangesAsync();
        await db.SaveChangesAsync();

        Assert.HasCount(1, recorder.Seen);
        Assert.IsEmpty(order.DomainEvents);
    }

    [TestMethod]
    [TestCategory("Integration")]
    public async Task AHandlerThatThrowsRollsBackTheOrder()
    {
        await using (var db = _database.NewContext(new DomainEventDispatcher().Register(new ExplodingHandler())))
        {
            db.Orders.Add(Order.Place(CustomerId, 10m, TimeProvider.System));
            await Assert.ThrowsExactlyAsync<InvalidOperationException>(() => db.SaveChangesAsync());
        }

        // A fresh context reads the database, not the failed context's tracker.
        await using var check = _database.Plain();
        Assert.AreEqual(0, await check.Orders.CountAsync());
    }
}
