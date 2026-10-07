using Microsoft.Extensions.Time.Testing;

namespace Ordering.Tests;

// TODO-00: The aggregate only records events: no database, no dispatcher, no setup.
// Write the three tests below. Read Order.Place in src/Ordering/Model.cs first.
[TestClass]
public sealed class OrderAggregateTests
{
    private static readonly Guid CustomerId = Guid.NewGuid();

    [TestMethod]
    public void PlacingAnOrderRaisesOneOrderPlacedEvent()
    {
        // Assert.ContainsSingle returns the element, and Assert.IsInstanceOfType<T> returns it typed.
        // Check the order id, the customer id and the total carried by the event.
        Assert.Fail("TODO-00: write this test.");
    }

    [TestMethod]
    public void TheEventCarriesTheTimeOfTheClockItWasGiven()
    {
        // Use a FakeTimeProvider (already referenced) so the expected time is known, not "about now".
        Assert.Fail("TODO-00: write this test.");
    }

    [TestMethod]
    public void AnOrderWithoutAPositiveTotalIsRejected()
    {
        // Turn this into one test that runs for 0 and for -5: give it an int parameter and add [DataRow] rows.
        // Careful with the exception type: Assert.ThrowsExactly<T> wants exactly T, not a type derived from it.
        Assert.Fail("TODO-00: write this test.");
    }
}
