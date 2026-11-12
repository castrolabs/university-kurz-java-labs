using Shop;

namespace Shop.MsTests;

[TestClass]
[TestCategory("Fast")]
public sealed class BasketTests : IDisposable
{
    private static PriceCatalog s_catalog = null!;

    private readonly Basket _basket = new(s_catalog);

    public TestContext TestContext { get; set; } = null!;

    [ClassInitialize]
    public static void BuildCatalog(TestContext _) => s_catalog = new PriceCatalog();

    public void Dispose() { }

    [TestMethod]
    public void AddingTheSameProductTwiceAddsUpTheQuantity()
    {
        _basket.Add("apple", 2);
        _basket.Add("apple", 3);

        Assert.AreEqual(5, _basket.Quantities["apple"]);
    }

    [TestMethod]
    public void TheTotalIsTheSumOfEveryLine()
    {
        _basket.Add("apple", 4);
        _basket.Add("pear", 2);

        Assert.AreEqual(3.60m, _basket.Total);
    }

    [TestMethod]
    [DataRow(0)]
    [DataRow(-1)]
    public void QuantitiesBelowOneAreRejected(int quantity)
    {
        Assert.ThrowsExactly<ArgumentOutOfRangeException>(() => _basket.Add("apple", quantity));
    }

    public static IEnumerable<(string Sku, decimal Price)> Prices => [("apple", 0.50m), ("pear", 0.80m)];

    [TestMethod]
    [DynamicData(nameof(Prices))]
    public void ThePriceComesFromTheCatalog(string sku, decimal price)
    {
        Assert.AreEqual(price, s_catalog.PriceOf(sku));
    }

    [TestMethod]
    public void AnUnknownProductIsRejectedWithItsOwnType()
    {
        var failure = Assert.ThrowsExactly<UnknownProductException>(() => _basket.Add("plum", 1));

        Assert.AreEqual("sku", failure.ParamName);
    }

    [TestMethod]
    public void AnUnknownProductIsAnArgumentException()
    {
        // Throws<T> accepts a subclass in MSTest.
        Assert.Throws<ArgumentException>(() => _basket.Add("plum", 1));
    }

    [TestMethod]
    [Ignore("The discount rules are not in the shop yet.")]
    public void ADiscountLowersTheTotal()
    {
        Assert.Fail("not built yet");
    }

    [TestMethod]
    public void TheCatalogIsBuiltOnceForTheWholeClass()
    {
        Assert.AreEqual(1, PriceCatalog.Created);
    }

    [TestMethod]
    public void TheTestKnowsItsOwnNameAndItsCancellationToken()
    {
        TestContext.WriteLine("checking {0}", TestContext.TestName);

        Assert.AreEqual(nameof(TheTestKnowsItsOwnNameAndItsCancellationToken), TestContext.TestName);
        Assert.IsFalse(TestContext.CancellationToken.IsCancellationRequested);
    }
}
