using Shop;

namespace Shop.XTests;

// MSTest: [ClassInitialize] builds the catalog. xUnit: a fixture class that xUnit builds once per test class.
public sealed class CatalogFixture
{
    public PriceCatalog Catalog { get; } = new();
}

[Trait("Category", "Fast")]
public sealed class BasketTests : IClassFixture<CatalogFixture>, IDisposable
{
    private readonly PriceCatalog _catalog;
    private readonly Basket _basket;
    private readonly ITestOutputHelper _output;

    // MSTest: the TestContext property. xUnit: the constructor takes what it needs, and the same
    // constructor is the per-test setup because every test gets its own instance.
    public BasketTests(CatalogFixture fixture, ITestOutputHelper output)
    {
        _catalog = fixture.Catalog;
        _basket = new Basket(_catalog);
        _output = output;
    }

    public void Dispose() { }

    [Fact]
    public void AddingTheSameProductTwiceAddsUpTheQuantity()
    {
        _basket.Add("apple", 2);
        _basket.Add("apple", 3);

        Assert.Equal(5, _basket.Quantities["apple"]);
    }

    [Fact]
    public void TheTotalIsTheSumOfEveryLine()
    {
        _basket.Add("apple", 4);
        _basket.Add("pear", 2);

        Assert.Equal(3.60m, _basket.Total);
    }

    [Theory]
    [InlineData(0)]
    [InlineData(-1)]
    public void QuantitiesBelowOneAreRejected(int quantity)
    {
        Assert.Throws<ArgumentOutOfRangeException>(() => _basket.Add("apple", quantity));
    }

    public static TheoryData<string, decimal> Prices => new()
    {
        { "apple", 0.50m },
        { "pear", 0.80m },
    };

    [Theory]
    [MemberData(nameof(Prices))]
    public void ThePriceComesFromTheCatalog(string sku, decimal price)
    {
        Assert.Equal(price, _catalog.PriceOf(sku));
    }

    [Fact]
    public void AnUnknownProductIsRejectedWithItsOwnType()
    {
        var failure = Assert.Throws<UnknownProductException>(() => _basket.Add("plum", 1));

        Assert.Equal("sku", failure.ParamName);
    }

    [Fact]
    public void AnUnknownProductIsAnArgumentException()
    {
        // Throws<ArgumentException> would fail here: xUnit wants the exact type. ThrowsAny accepts a subclass.
        Assert.ThrowsAny<ArgumentException>(() => _basket.Add("plum", 1));
    }

    [Fact(Skip = "The discount rules are not in the shop yet.")]
    public void ADiscountLowersTheTotal()
    {
        Assert.Fail("not built yet");
    }

    [Fact]
    public void TheCatalogIsBuiltOnceForTheWholeClass()
    {
        Assert.Equal(1, PriceCatalog.Created);
    }

    [Fact]
    public void TheTestKnowsItsOwnNameAndItsCancellationToken()
    {
        _output.WriteLine($"checking {TestContext.Current.Test?.TestDisplayName}");

        Assert.Contains(nameof(TheTestKnowsItsOwnNameAndItsCancellationToken), TestContext.Current.Test?.TestDisplayName);
        Assert.False(TestContext.Current.CancellationToken.IsCancellationRequested);
    }
}
