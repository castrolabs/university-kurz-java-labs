namespace Shipping.Tests;

// The suite the lab starts from: it passes, it runs every line of ShippingCalculator, and it is weak.
// Stryker needs a green suite to start, so the TODOs below are [Ignore]d shells instead of failing tests.
// Remove the [Ignore] from each one as you write it.
[TestClass]
public sealed class ShippingCalculatorTests
{
    private readonly ShippingCalculator _calculator = new();

    [TestMethod]
    public void AParcelHasAPrice()
    {
        var price = _calculator.Quote(new Parcel(3m, Zone.Domestic, false, 20m));

        Assert.IsGreaterThan(0m, price);
    }

    [TestMethod]
    public void AEuropeanParcelCostsMoreThanADomesticOne()
    {
        var domestic = _calculator.Quote(new Parcel(3m, Zone.Domestic, false, 20m));
        var europe = _calculator.Quote(new Parcel(3m, Zone.Europe, false, 20m));

        Assert.IsGreaterThan(domestic, europe);
    }

    [TestMethod]
    public void AHeavyParcelCostsMoreThanALightOne()
    {
        var light = _calculator.Quote(new Parcel(1m, Zone.Domestic, false, 20m));
        var heavy = _calculator.Quote(new Parcel(20m, Zone.Domestic, false, 20m));

        Assert.IsGreaterThan(light, heavy);
    }

    [TestMethod]
    public void AnExpensiveOrderShipsForLess()
    {
        var normal = _calculator.Quote(new Parcel(3m, Zone.Domestic, false, 20m));
        var free = _calculator.Quote(new Parcel(3m, Zone.Domestic, false, 150m));

        Assert.IsLessThan(normal, free);
    }

    [TestMethod]
    public void ANegativeWeightIsRejected()
    {
        Assert.ThrowsExactly<ArgumentOutOfRangeException>(() =>
            _calculator.Quote(new Parcel(-1m, Zone.Domestic, false, 20m)));
    }

    [TestMethod]
    public void ALabelIsNotEmpty()
    {
        var label = _calculator.Label(new Parcel(3m, Zone.Domestic, false, 20m));

        Assert.IsFalse(string.IsNullOrEmpty(label));
    }

    // TODO-01: Replace the guesses of "costs more than" by the tariff. Write one data-driven test with
    // [DynamicData] (an attribute cannot hold a decimal, so a property returning tuples works well). Give it
    // the exact price for: every weight tier edge and one step past it (1, 1.01, 5, 5.01, 10, 10.01), a weight
    // above 10 kg that is not a whole number of kilos (11 and 11.01), 20 kg and 30 kg, each zone, and express.
    // Work the prices out from the comment on ShippingCalculator, not from running the code.
    // Hint: 4.90 x 1.5 x 1.5 is 11.025. Which way does the calculator round it?
    [TestMethod]
    [Ignore("TODO-01: write this test.")]
    public void TheQuoteFollowsTheTariff()
    {
    }

    // TODO-02: The weight limits. 0 and -1 are rejected, and so is 30.01; 30 is not (the tariff test covers it).
    // Check more than the exception type: the ParamName, the ActualValue and a piece of the message.
    [TestMethod]
    [Ignore("TODO-02: write this test.")]
    public void TheWeightLimitsAreEnforced()
    {
    }

    // TODO-03: Free shipping. 100 is free, 99.99 is not. Express does not change a free parcel, and Europe
    // and World orders are never free, whatever they are worth.
    [TestMethod]
    [Ignore("TODO-03: write this test.")]
    public void FreeShippingIsDomesticAndFromOneHundred()
    {
    }

    // TODO-04: The label: assert the whole text for a standard and for an express parcel, in more than one zone,
    // with a weight that has no decimals (3) and one that has two (1.25).
    [TestMethod]
    [Ignore("TODO-04: write this test.")]
    public void ALabelSaysTheServiceTheZoneAndTheWeight()
    {
    }
}
