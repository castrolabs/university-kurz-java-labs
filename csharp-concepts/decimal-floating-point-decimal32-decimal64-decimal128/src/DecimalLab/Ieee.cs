using System.Numerics;

namespace DecimalLab;

public static class Ieee
{
    public static Decimal64 RoundToCents(Decimal64 amount)
    {
        // TODO-00: Return the amount with exactly two decimal places, ties to even
        //   (19.9 -> 19.90, 2.675 -> 2.68, 2.665 -> 2.66). Round(x, 2) keeps 19.9 as 19.9;
        //   look at Decimal64.Quantize with a 0.01 quantum. Parse text with CultureInfo.InvariantCulture.
        throw new NotImplementedException("Not implemented yet.");
    }

    public static Decimal64 RoundHalfAwayToCents(Decimal64 amount)
    {
        // TODO-01: Round to 2 decimal places with halves away from zero (2.665 -> 2.67,
        //   -2.665 -> -2.67). Choose the MidpointRounding mode explicitly.
        throw new NotImplementedException("Not implemented yet.");
    }

    public static decimal? ToDecimalOrNull(Decimal128 value)
    {
        // TODO-02: Convert to System.Decimal, or return null when the value does not fit
        //   (Decimal128.MaxValue, infinities, NaN). The explicit conversion throws OverflowException.
        throw new NotImplementedException("Not implemented yet.");
    }

    public static bool FitsInDecimal32(Decimal64 value)
    {
        // TODO-03: True when the value survives a trip through Decimal32 unchanged
        //   (1234567 yes, 12345678 no, 1E200 no because Decimal32 overflows to Infinity).
        throw new NotImplementedException("Not implemented yet.");
    }

    public static T SumTenths<T>(int count) where T : INumber<T>
    {
        // TODO-04: Add one tenth (T.One / 10) count times, with generic math so the same code
        //   runs for double, decimal and Decimal64. Ten tenths must be exactly 1 for the
        //   decimal types and not 1 for double.
        throw new NotImplementedException("Not implemented yet.");
    }

    public static bool SameValueDifferentQuantum(Decimal64 a, Decimal64 b)
    {
        // TODO-05: True when a == b but the two are different representations (1.0 and 1.00).
        //   Decimal64.HaveSameQuantum compares the exponents.
        throw new NotImplementedException("Not implemented yet.");
    }

    public static string ToBid(Decimal64 value)
    {
        // TODO-06: The 64 bits of the value in BID encoding as 16 upper-case hex digits
        //   (1.50 -> "3180000000000096"). EncodeBinary returns a ulong.
        throw new NotImplementedException("Not implemented yet.");
    }

    public static Decimal64 FromBid(ulong bits)
    {
        // TODO-07: The inverse of ToBid: build the value from BID bits, keeping the exponent.
        throw new NotImplementedException("Not implemented yet.");
    }

    public static Decimal64 MultiplyChecked(Decimal64 a, Decimal64 b)
    {
        // TODO-08: Multiply, but throw OverflowException when finite operands give an infinite
        //   result (1E200 * 1E200). `checked` does not do this for IEEE decimals, and an
        //   operand that was already infinite must not throw.
        throw new NotImplementedException("Not implemented yet.");
    }

    public static string ToJson(Decimal64 value)
    {
        // TODO-09: Serialize as a JSON number that keeps trailing zeros (19.90 -> 19.90).
        throw new NotImplementedException("Not implemented yet.");
    }

    public static Decimal64 FromJson(string json)
    {
        // TODO-10: Read a JSON number back, keeping the quantum (19.90 prints as 19.90).
        throw new NotImplementedException("Not implemented yet.");
    }
}
