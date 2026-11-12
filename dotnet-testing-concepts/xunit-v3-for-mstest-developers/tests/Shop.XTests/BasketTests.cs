using Shop;

namespace Shop.XTests;

// TODO-02: The MSTest suite builds the catalog once per class in [ClassInitialize]. In xUnit that is a fixture
// class: give this one a `PriceCatalog Catalog` that it builds, and ask for it with IClassFixture<CatalogFixture>.
public sealed class CatalogFixture
{
}

// TODO-01: Port tests/Shop.MsTests/BasketTests.cs. Keep the method names. For each test: what replaces the
// MSTest attribute, what replaces the MSTest assertion, and what changes in the constructor and in the exceptions.
// Every stub below is a plain [Fact] with a failing assertion: turn it into the real test.
public sealed class BasketTests
{
    // TODO-01: the MSTest class has [TestCategory("Fast")]. Find the xUnit equivalent and put it on the class,
    // then check it with: dotnet test --project tests/Shop.XTests --filter-trait "Category=Fast"

    // TODO-01: the MSTest class sets up in a field initializer and keeps a TestContext property.
    // In xUnit the constructor gets the fixture and an ITestOutputHelper, and builds the basket.

    [Fact]
    public void AddingTheSameProductTwiceAddsUpTheQuantity()
    {
        Assert.Fail("TODO-01: port this test.");
    }

    [Fact]
    public void TheTotalIsTheSumOfEveryLine()
    {
        Assert.Fail("TODO-01: port this test.");
    }

    [Fact]
    public void QuantitiesBelowOneAreRejected()
    {
        // [DataRow(0)] and [DataRow(-1)] become a [Theory] with two [InlineData] rows.
        Assert.Fail("TODO-01: port this test.");
    }

    [Fact]
    public void ThePriceComesFromTheCatalog()
    {
        // [DynamicData] over tuples becomes [MemberData] over a TheoryData<string, decimal>.
        Assert.Fail("TODO-01: port this test.");
    }

    [Fact]
    public void AnUnknownProductIsRejectedWithItsOwnType()
    {
        Assert.Fail("TODO-01: port this test.");
    }

    [Fact]
    public void AnUnknownProductIsAnArgumentException()
    {
        // MSTest's Assert.Throws<T> accepts a subclass. Run the "obvious" xUnit Assert.Throws<ArgumentException>
        // first and read the message, then pick the xUnit assertion that accepts a subclass.
        Assert.Fail("TODO-01: port this test.");
    }

    [Fact(Skip = "The discount rules are not in the shop yet.")]
    public void ADiscountLowersTheTotal()
    {
        Assert.Fail("not built yet");
    }

    [Fact]
    public void TheCatalogIsBuiltOnceForTheWholeClass()
    {
        // Passes only when the catalog comes from the class fixture and not from the constructor.
        Assert.Fail("TODO-02: port this test.");
    }

    [Fact]
    public void TheTestKnowsItsOwnNameAndItsCancellationToken()
    {
        // TestContext.WriteLine becomes ITestOutputHelper.WriteLine; TestContext.TestName and
        // TestContext.CancellationToken become members of xUnit's static TestContext.Current.
        Assert.Fail("TODO-04: port this test.");
    }
}
