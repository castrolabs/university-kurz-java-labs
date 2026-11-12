namespace Shop.Tests;

// TODO-02 (part 2): use the fakers from Fakes.cs.
[TestClass]
public sealed class BogusTests
{
    [TestMethod]
    public void ShouldGenerateTheSameCustomersForTheSameSeed()
    {
        // Two fakers, same seed, Generate(5) each: the lists are equal (records compare by value).
        // CollectionAssert.AreEqual.
        Assert.Fail("TODO-02: write this test.");
    }

    [TestMethod]
    public void ShouldGenerateOtherCustomersForAnotherSeed()
    {
        Assert.Fail("TODO-02: write this test.");
    }

    [TestMethod]
    public void ShouldOnlyEverGiveOneOfTheFourKnownRates()
    {
        // 200 orders from Fakes.Orders(42). First assert the data hits both branches of each rule (some premium,
        // some not, some at or above the threshold, some below), otherwise the main assertion proves little.
        // Then every RateFor is one of 0, 0.05, 0.10, 0.15. Do not recompute the rule in the test.
        Assert.Fail("TODO-02: write this test.");
    }
}
