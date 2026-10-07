using Microsoft.EntityFrameworkCore;

namespace Ordering.Tests;

// The interceptor needs a real EF Core pipeline, so these tests use SQLite in memory (see TestDatabase).
[TestClass]
public sealed class DomainEventsInterceptorTests
{
    private static readonly Guid CustomerId = Guid.NewGuid();

    // TODO-02: Keep a TestDatabase in a readonly field and dispose it after each test (make the class
    // IDisposable). MSTest creates a new instance of the class for every test, so the field is per test.
    // Think about why that matters when tests run in parallel. OutboxTests already does it: read it if
    // you are stuck. ([TestInitialize] and [TestCleanup] would work too, but cannot assign a readonly field.)

    // TODO-03: Write the three tests below. Tag them with [TestCategory("Integration")].
    [TestMethod]
    public async Task SavingDispatchesTheEventsOfTheTrackedAggregates()
    {
        // Register a RecordingHandler on a DomainEventDispatcher, save an order, check what the handler saw.
        await Task.CompletedTask;
        Assert.Fail("TODO-03: write this test.");
    }

    [TestMethod]
    public async Task EventsAreClearedSoASecondSaveDoesNotDispatchThemAgain()
    {
        // Save twice. The handler must see the event once, and the aggregate must have no events left.
        await Task.CompletedTask;
        Assert.Fail("TODO-03: write this test.");
    }

    [TestMethod]
    public async Task AHandlerThatThrowsRollsBackTheOrder()
    {
        // The failed context still tracks the order. To see what was really committed, read it back
        // with a different context (TestDatabase.Plain).
        await Task.CompletedTask;
        Assert.Fail("TODO-03: write this test.");
    }
}
