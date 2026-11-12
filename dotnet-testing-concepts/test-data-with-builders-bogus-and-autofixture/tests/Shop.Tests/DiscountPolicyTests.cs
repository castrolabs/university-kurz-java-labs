namespace Shop.Tests;

// TODO-01: With your OrderBuilder. Each test says only what its rule depends on. Read DiscountPolicy first.
[TestClass]
public sealed class DiscountPolicyTests
{
    [TestMethod]
    public void ShouldGiveNoDiscountToARegularCustomerBelowTheThreshold()
    {
        // Just below the bulk threshold: which amount is "just below"?
        Assert.Fail("TODO-01: write this test.");
    }

    [TestMethod]
    public void ShouldGivePremiumCustomersTenPercent()
    {
        Assert.Fail("TODO-01: write this test.");
    }

    [TestMethod]
    public void ShouldGiveFivePercentAtExactlyTheBulkThreshold()
    {
        // A boundary test: exactly at the threshold.
        Assert.Fail("TODO-01: write this test.");
    }

    [TestMethod]
    public void ShouldAddTheTwoDiscountsForAPremiumBulkOrder()
    {
        Assert.Fail("TODO-01: write this test.");
    }

    [TestMethod]
    public void ShouldSumQuantityTimesPriceAcrossLines()
    {
        // Two lines with quantity above 1 (WithLines). Assert the Total and the resulting rate.
        Assert.Fail("TODO-01: write this test.");
    }
}
