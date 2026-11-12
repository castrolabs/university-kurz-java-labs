namespace Shipping.Tests;

[TestClass]
public sealed class ShippingCalculatorTests
{
    private readonly ShippingCalculator _calculator = new();

    // Every tier edge and one step past it, so a "<=" that becomes "<" or ">" moves a row.
    public static IEnumerable<(decimal Weight, Zone Zone, bool Express, decimal Expected)> Prices =>
    [
        (0.5m, Zone.Domestic, false, 4.90m),
        (1m, Zone.Domestic, false, 4.90m),
        (1.01m, Zone.Domestic, false, 7.90m),
        (5m, Zone.Domestic, false, 7.90m),
        (5.01m, Zone.Domestic, false, 12.90m),
        (10m, Zone.Domestic, false, 12.90m),
        (10.01m, Zone.Domestic, false, 13.70m),
        (11m, Zone.Domestic, false, 13.70m),
        (11.01m, Zone.Domestic, false, 14.50m),
        (20m, Zone.Domestic, false, 20.90m),
        (30m, Zone.Domestic, false, 28.90m),
        (1m, Zone.Europe, false, 7.35m),
        (1m, Zone.World, false, 14.70m),
        (1m, Zone.Domestic, true, 7.35m),
        (1m, Zone.Europe, true, 11.03m), // 4.90 x 1.5 x 1.5 = 11.025, rounded away from zero
        (1m, Zone.World, true, 22.05m),
    ];

    [TestMethod]
    [DynamicData(nameof(Prices))]
    public void TheQuoteFollowsTheTariff(decimal weight, Zone zone, bool express, decimal expected)
    {
        var price = _calculator.Quote(new Parcel(weight, zone, express, 20m));

        Assert.AreEqual(expected, price);
    }

    [TestMethod]
    [DataRow(0)]
    [DataRow(-1)]
    public void AWeightOfZeroOrLessIsRejected(int weight)
    {
        var error = Assert.ThrowsExactly<ArgumentOutOfRangeException>(() =>
            _calculator.Quote(new Parcel(weight, Zone.Domestic, false, 20m)));

        Assert.AreEqual("parcel", error.ParamName);
        Assert.AreEqual((decimal)weight, error.ActualValue);
        StringAssert.Contains(error.Message, "at most 30 kg");
    }

    [TestMethod]
    public void AWeightAboveThirtyKilosIsRejected()
    {
        var error = Assert.ThrowsExactly<ArgumentOutOfRangeException>(() =>
            _calculator.Quote(new Parcel(30.01m, Zone.Domestic, false, 20m)));

        Assert.AreEqual(30.01m, error.ActualValue);
    }

    [TestMethod]
    public void ADomesticOrderOfOneHundredShipsForFree()
    {
        Assert.AreEqual(0m, _calculator.Quote(new Parcel(3m, Zone.Domestic, false, 100m)));
        Assert.AreEqual(0m, _calculator.Quote(new Parcel(3m, Zone.Domestic, true, 150m)));
    }

    [TestMethod]
    public void ADomesticOrderJustBelowOneHundredPays()
    {
        Assert.AreEqual(7.90m, _calculator.Quote(new Parcel(3m, Zone.Domestic, false, 99.99m)));
    }

    [TestMethod]
    public void FreeShippingIsOnlyForDomesticOrders()
    {
        Assert.AreEqual(11.85m, _calculator.Quote(new Parcel(3m, Zone.Europe, false, 150m)));
    }

    [TestMethod]
    [DataRow(3, Zone.Domestic, false, "STD-DOMESTIC-3")]
    [DataRow(12.5, Zone.Europe, true, "EXP-EUROPE-12.5")]
    [DataRow(1.25, Zone.World, false, "STD-WORLD-1.25")]
    public void ALabelSaysTheServiceTheZoneAndTheWeight(double weight, Zone zone, bool express, string expected)
    {
        var label = _calculator.Label(new Parcel((decimal)weight, zone, express, 20m));

        Assert.AreEqual(expected, label);
    }
}
