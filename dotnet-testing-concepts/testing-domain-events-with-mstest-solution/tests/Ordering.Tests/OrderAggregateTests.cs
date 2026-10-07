using Microsoft.Extensions.Time.Testing;

namespace Ordering.Tests;

// The aggregate only records events: no database, no dispatcher, no setup.
[TestClass]
public sealed class OrderAggregateTests
{
    private static readonly Guid CustomerId = Guid.NewGuid();

    [TestMethod]
    public void PlacingAnOrderRaisesOneOrderPlacedEvent()
    {
        var order = Order.Place(CustomerId, 50m, TimeProvider.System);

        var raised = Assert.ContainsSingle(order.DomainEvents);
        var placed = Assert.IsInstanceOfType<OrderPlaced>(raised);
        Assert.AreEqual(order.Id, placed.OrderId);
        Assert.AreEqual(CustomerId, placed.CustomerId);
        Assert.AreEqual(50m, placed.Total);
    }

    [TestMethod]
    public void TheEventCarriesTheTimeOfTheClockItWasGiven()
    {
        var clock = new FakeTimeProvider(new DateTimeOffset(2026, 10, 7, 9, 30, 0, TimeSpan.Zero));

        var order = Order.Place(CustomerId, 10m, clock);

        var placed = Assert.IsInstanceOfType<OrderPlaced>(Assert.ContainsSingle(order.DomainEvents));
        Assert.AreEqual(clock.GetUtcNow(), placed.At);
    }

    [TestMethod]
    [DataRow(0)]
    [DataRow(-5)]
    public void AnOrderWithoutAPositiveTotalIsRejected(int total)
    {
        // ThrowsExactly wants the exact type. Place throws ArgumentOutOfRangeException, so asking
        // for ArgumentException here would fail; Assert.Throws<ArgumentException> would accept it.
        Assert.ThrowsExactly<ArgumentOutOfRangeException>(() => Order.Place(CustomerId, total, TimeProvider.System));
    }
}
