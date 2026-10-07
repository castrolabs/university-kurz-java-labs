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

    // TODO-04: Write the two tests below. Use OrderPlacedHandler as the handler under test.
    [TestMethod]
    public async Task TheOutboxRowIsSavedInTheSameTransactionAsTheOrder()
    {
        // After one successful save, read back with another context: one order and one outbox row.
        await Task.CompletedTask;
        Assert.Fail("TODO-04: write this test.");
    }

    [TestMethod]
    public async Task NoOrderMeansNoOutboxRowWhenTheSaveFails()
    {
        // Register OrderPlacedHandler first and ExplodingHandler second, so the outbox row is added
        // before the failure. Then prove that neither the order nor the row reached the database.
        await Task.CompletedTask;
        Assert.Fail("TODO-04: write this test.");
    }
}
