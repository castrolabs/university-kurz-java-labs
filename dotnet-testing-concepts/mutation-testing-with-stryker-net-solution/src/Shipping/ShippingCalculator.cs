using System.Globalization;

namespace Shipping;

public enum Zone { Domestic, Europe, World }

public sealed record Parcel(decimal WeightKg, Zone Zone, bool Express, decimal OrderValue);

// Prices in euro. The base price depends on the weight (up to 1 kg, up to 5 kg, up to 10 kg, then 0.80 per
// started kilogram above 10), then the zone and the express service multiply it. Domestic orders worth 100 or
// more ship for free. A parcel weighs more than 0 and at most 30 kg.
public sealed class ShippingCalculator
{
    public const decimal MaxWeightKg = 30m;
    public const decimal FreeShippingFrom = 100m;

    public decimal Quote(Parcel parcel)
    {
        if (parcel.WeightKg <= 0 || parcel.WeightKg > MaxWeightKg)
        {
            throw new ArgumentOutOfRangeException(nameof(parcel), parcel.WeightKg, "The weight must be above 0 and at most 30 kg.");
        }

        if (parcel.Zone == Zone.Domestic && parcel.OrderValue >= FreeShippingFrom)
        {
            return 0m;
        }

        var price = BasePrice(parcel.WeightKg);
        price *= parcel.Zone switch
        {
            Zone.Domestic => 1m,
            Zone.Europe => 1.5m,
            _ => 3m,
        };

        if (parcel.Express)
        {
            price *= 1.5m;
        }

        return Math.Round(price, 2, MidpointRounding.AwayFromZero);
    }

    public string Label(Parcel parcel) =>
        string.Create(CultureInfo.InvariantCulture,
            $"{(parcel.Express ? "EXP" : "STD")}-{parcel.Zone.ToString().ToUpperInvariant()}-{parcel.WeightKg:0.##}");

    private static decimal BasePrice(decimal weightKg) =>
        weightKg <= 1 ? 4.90m
        : weightKg <= 5 ? 7.90m
        : weightKg <= 10 ? 12.90m
        : 12.90m + Math.Ceiling(weightKg - 10) * 0.80m;
}
