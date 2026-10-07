using Microsoft.EntityFrameworkCore;

namespace Ordering.Tests;

// The consumer runs after the commit, in its own transaction. Call it directly:
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
    // cancelled when the test times out or the run is aborted: pass it to the code under test instead
    // of CancellationToken.None.
    public LoyaltyConsumerTests(TestContext testContext)
    {
        _testContext = testContext;
        _consumer = new LoyaltyConsumer(_database.Plain);
    }

    public void Dispose() => _database.Dispose();

    private static OrderPlacedIntegrationEvent Message(decimal total) =>
        new(Guid.NewGuid(), Guid.NewGuid(), CustomerId, total);

    // TODO-05: Write the three tests below. Assert on a fresh context, never on the consumer's own.
    [TestMethod]
    public async Task TheConsumerAwardsPointsInItsOwnTransaction()
    {
        await Task.CompletedTask;
        Assert.Fail("TODO-05: write this test.");
    }

    [TestMethod]
    public async Task ADuplicateDeliveryOfTheSameMessageAwardsPointsOnlyOnce()
    {
        // At-least-once delivery: the same message arrives twice. Points once, and one inbox row.
        await Task.CompletedTask;
        Assert.Fail("TODO-05: write this test.");
    }

    [TestMethod]
    public async Task DifferentMessagesForTheSameCustomerAccumulate()
    {
        await Task.CompletedTask;
        Assert.Fail("TODO-05: write this test.");
    }
}
