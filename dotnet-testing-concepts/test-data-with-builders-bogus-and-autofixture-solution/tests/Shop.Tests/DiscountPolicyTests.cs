namespace Shop.Tests;

[TestClass]
public sealed class DiscountPolicyTests
{
    [TestMethod]
    public void ShouldGiveNoDiscountToARegularCustomerBelowTheThreshold()
    {
        var order = new OrderBuilder().Worth(499.99m).Build();

        Assert.AreEqual(0m, DiscountPolicy.RateFor(order));
    }

    [TestMethod]
    public void ShouldGivePremiumCustomersTenPercent()
    {
        var order = new OrderBuilder().Premium().Worth(50m).Build();

        Assert.AreEqual(0.10m, DiscountPolicy.RateFor(order));
    }

    [TestMethod]
    public void ShouldGiveFivePercentAtExactlyTheBulkThreshold()
    {
        var order = new OrderBuilder().Worth(500m).Build();

        Assert.AreEqual(0.05m, DiscountPolicy.RateFor(order));
    }

    [TestMethod]
    public void ShouldAddTheTwoDiscountsForAPremiumBulkOrder()
    {
        var order = new OrderBuilder().Premium().Worth(500m).Build();

        Assert.AreEqual(0.15m, DiscountPolicy.RateFor(order));
    }

    [TestMethod]
    public void ShouldSumQuantityTimesPriceAcrossLines()
    {
        var order = new OrderBuilder().WithLines(new OrderLine("A", 2, 100m), new OrderLine("B", 3, 100m)).Build();

        Assert.AreEqual(500m, order.Total);
        Assert.AreEqual(0.05m, DiscountPolicy.RateFor(order));
    }
}
