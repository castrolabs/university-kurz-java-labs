namespace Shop.Tests;

[TestClass]
public sealed class BogusTests
{
    [TestMethod]
    public void ShouldGenerateTheSameCustomersForTheSameSeed()
    {
        var first = Fakes.Customers(7).Generate(5);
        var second = Fakes.Customers(7).Generate(5);

        CollectionAssert.AreEqual(first, second);
    }

    [TestMethod]
    public void ShouldGenerateOtherCustomersForAnotherSeed()
    {
        var first = Fakes.Customers(7).Generate(5);
        var second = Fakes.Customers(8).Generate(5);

        CollectionAssert.AreNotEqual(first, second);
    }

    [TestMethod]
    public void ShouldOnlyEverGiveOneOfTheFourKnownRates()
    {
        var orders = Fakes.Orders(42).Generate(200);

        // The data must exercise both branches, otherwise "always one of four rates" proves little.
        Assert.IsTrue(orders.Any(o => o.Customer.IsPremium));
        Assert.IsTrue(orders.Any(o => !o.Customer.IsPremium));
        Assert.IsTrue(orders.Any(o => o.Total >= DiscountPolicy.BulkThreshold));
        Assert.IsTrue(orders.Any(o => o.Total < DiscountPolicy.BulkThreshold));

        decimal[] allowed = [0m, 0.05m, 0.10m, 0.15m];
        foreach (var order in orders)
        {
            CollectionAssert.Contains(allowed, DiscountPolicy.RateFor(order));
        }
    }
}
