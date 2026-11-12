using System.Globalization;
using System.Numerics;
using System.Text.Json;

namespace DecimalLab;

public static class Ieee
{
    private static readonly Decimal64 OneCent = Decimal64.Parse("0.01", CultureInfo.InvariantCulture);

    public static Decimal64 RoundToCents(Decimal64 amount) => Decimal64.Quantize(amount, OneCent);

    public static Decimal64 RoundHalfAwayToCents(Decimal64 amount) =>
        Decimal64.Round(amount, 2, MidpointRounding.AwayFromZero);

    public static decimal? ToDecimalOrNull(Decimal128 value)
    {
        try
        {
            return (decimal)value;
        }
        catch (OverflowException)
        {
            return null;
        }
    }

    public static bool FitsInDecimal32(Decimal64 value) => (Decimal64)(Decimal32)value == value;

    public static T SumTenths<T>(int count) where T : INumber<T>
    {
        T tenth = T.One / T.CreateChecked(10);
        T sum = T.Zero;
        for (int i = 0; i < count; i++)
        {
            sum += tenth;
        }
        return sum;
    }

    public static bool SameValueDifferentQuantum(Decimal64 a, Decimal64 b) =>
        a == b && !Decimal64.HaveSameQuantum(a, b);

    public static string ToBid(Decimal64 value) => Decimal64.EncodeBinary(value).ToString("X16", CultureInfo.InvariantCulture);

    public static Decimal64 FromBid(ulong bits) => Decimal64.DecodeBinary(bits);

    public static Decimal64 MultiplyChecked(Decimal64 a, Decimal64 b)
    {
        Decimal64 result = a * b;
        if (Decimal64.IsInfinity(result) && Decimal64.IsFinite(a) && Decimal64.IsFinite(b))
        {
            throw new OverflowException("Decimal64 multiplication overflowed.");
        }
        return result;
    }

    public static string ToJson(Decimal64 value) => JsonSerializer.Serialize(value);

    public static Decimal64 FromJson(string json) => JsonSerializer.Deserialize<Decimal64>(json);
}
