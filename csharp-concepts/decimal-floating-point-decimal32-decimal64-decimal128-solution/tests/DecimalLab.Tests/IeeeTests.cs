using System.Globalization;
using System.Numerics;
using DecimalLab;

namespace DecimalLab.Tests;

public class IeeeTests
{
    private static Decimal64 D(string text) => Decimal64.Parse(text, CultureInfo.InvariantCulture);

    private static string Show(Decimal64 value) => value.ToString(CultureInfo.InvariantCulture);

    [Theory]
    [InlineData("19.9", "19.90")]
    [InlineData("2.675", "2.68")]
    [InlineData("2.665", "2.66")]
    [InlineData("-2.675", "-2.68")]
    public void ShouldQuantizeToCentsWithTiesToEven(string input, string expected) =>
        Assert.Equal(expected, Show(Ieee.RoundToCents(D(input))));

    [Theory]
    [InlineData("2.665", "2.67")]
    [InlineData("-2.665", "-2.67")]
    [InlineData("1.004", "1")]
    public void ShouldRoundHalvesAwayFromZero(string input, string expected) =>
        Assert.Equal(D(expected), Ieee.RoundHalfAwayToCents(D(input)));

    [Fact]
    public void ShouldConvertDecimal128ThatFitsInDecimal() =>
        Assert.Equal(12.34m, Ieee.ToDecimalOrNull(Decimal128.Parse("12.34", CultureInfo.InvariantCulture)));

    [Fact]
    public void ShouldReturnNullWhenDecimal128IsTooBigForDecimal() =>
        Assert.Null(Ieee.ToDecimalOrNull(Decimal128.MaxValue));

    [Fact]
    public void ShouldReturnNullForInfinityAndNaN()
    {
        Assert.Null(Ieee.ToDecimalOrNull(Decimal128.PositiveInfinity));
        Assert.Null(Ieee.ToDecimalOrNull(Decimal128.NaN));
    }

    [Theory]
    [InlineData("1234567", true)]
    [InlineData("0.1", true)]
    [InlineData("12345678", false)]
    [InlineData("1.234567890123456", false)]
    [InlineData("1E200", false)]
    public void ShouldDetectValuesThatFitInSevenDigits(string input, bool expected) =>
        Assert.Equal(expected, Ieee.FitsInDecimal32(D(input)));

    [Fact]
    public void ShouldSumTenTenthsToExactlyOneInDecimal64() =>
        Assert.True(Ieee.SumTenths<Decimal64>(10) == Decimal64.One);

    [Fact]
    public void ShouldSumTenTenthsToExactlyOneInDecimal() =>
        Assert.Equal(1m, Ieee.SumTenths<decimal>(10));

    [Fact]
    public void ShouldNotSumTenTenthsToOneInDouble() =>
        Assert.NotEqual(1.0, Ieee.SumTenths<double>(10));

    [Theory]
    [InlineData("1.0", "1.00", true)]
    [InlineData("1.0", "1.0", false)]
    [InlineData("1.0", "2.0", false)]
    public void ShouldSpotEqualValuesWithDifferentQuantum(string a, string b, bool expected) =>
        Assert.Equal(expected, Ieee.SameValueDifferentQuantum(D(a), D(b)));

    [Fact]
    public void ShouldEncodeBidBits()
    {
        Assert.Equal("3180000000000096", Ieee.ToBid(D("1.50")));
        Assert.Equal("31A000000000000A", Ieee.ToBid(D("1.0")));
    }

    [Fact]
    public void ShouldDecodeBidBitsKeepingTheExponent()
    {
        Decimal64 value = Ieee.FromBid(0x31A000000000000AUL);
        Assert.Equal(Decimal64.One, value);
        Assert.Equal("1.0", Show(value));
    }

    [Fact]
    public void ShouldMultiplyFiniteValues() =>
        Assert.Equal(D("6"), Ieee.MultiplyChecked(D("2"), D("3")));

    [Fact]
    public void ShouldThrowWhenTheProductOverflows() =>
        Assert.Throws<OverflowException>(() => Ieee.MultiplyChecked(D("1E200"), D("1E200")));

    [Fact]
    public void ShouldKeepInfinityThatWasAlreadyThere() =>
        Assert.True(Decimal64.IsPositiveInfinity(Ieee.MultiplyChecked(Decimal64.PositiveInfinity, D("2"))));

    [Fact]
    public void ShouldWriteJsonNumberWithTrailingZeros() =>
        Assert.Equal("19.90", Ieee.ToJson(D("19.90")));

    [Fact]
    public void ShouldReadJsonNumberKeepingTheQuantum()
    {
        Decimal64 value = Ieee.FromJson("19.90");
        Assert.Equal(D("19.9"), value);
        Assert.Equal("19.90", Show(value));
    }
}
