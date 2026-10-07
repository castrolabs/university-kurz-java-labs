using Microsoft.EntityFrameworkCore;

namespace Ordering.Tests;

// The consumer runs after the commit, in its own transaction. The test calls it directly:
// no background worker, no polling, no Thread.Sleep.
[TestClass]
[TestCategory("Integration")]
public sealed class LoyaltyConsumerTests : IDisposable
{
    private static readonly Guid CustomerId = Guid.NewGuid();

    private readonly TestContext _testContext;
    private readonly TestDatabase _database = new();
    private readonly LoyaltyConsumer _consumer;

    // MSTest passes the TestContext of the running test to the constructor. Its CancellationToken is
    // cancelled when the test times out or the run is aborted.
    public LoyaltyConsumerTests(TestContext testContext)
    {
        _testContext = testContext;
        _consumer = new LoyaltyConsumer(_database.Plain);
    }

    public void Dispose() => _database.Dispose();

    private static OrderPlacedIntegrationEvent Message(decimal total) =>
        new(Guid.NewGuid(), Guid.NewGuid(), CustomerId, total);

    [TestMethod]
    public async Task TheConsumerAwardsPointsInItsOwnTransaction()
    {
        await _consumer.HandleAsync(Message(42m), _testContext.CancellationToken);

        await using var check = _database.Plain();
        Assert.AreEqual(42, (await check.Customers.SingleAsync(c => c.Id == CustomerId)).Points);
    }

    [TestMethod]
    public async Task ADuplicateDeliveryOfTheSameMessageAwardsPointsOnlyOnce()
    {
        var message = Message(42m);

        await _consumer.HandleAsync(message, _testContext.CancellationToken);
        await _consumer.HandleAsync(message, _testContext.CancellationToken);

        await using var check = _database.Plain();
        Assert.AreEqual(42, (await check.Customers.SingleAsync()).Points);
        Assert.AreEqual(1, await check.ProcessedMessages.CountAsync());
    }

    [TestMethod]
    public async Task DifferentMessagesForTheSameCustomerAccumulate()
    {
        await _consumer.HandleAsync(Message(10m), _testContext.CancellationToken);
        await _consumer.HandleAsync(Message(5m), _testContext.CancellationToken);

        await using var check = _database.Plain();
        Assert.AreEqual(15, (await check.Customers.SingleAsync()).Points);
    }
}
