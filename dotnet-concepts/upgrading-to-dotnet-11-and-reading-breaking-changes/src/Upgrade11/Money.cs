namespace Upgrade11;

public static class Money
{
    /// <summary>
    /// Converts a price that arrived as a <see cref="double"/> (a JSON number, a spreadsheet cell)
    /// to the decimal the person typed: 0.1 must become 0.1m.
    /// </summary>
    public static decimal FromDouble(double value)
    {
        // TODO-00: On .NET 10 this cast kept about 15 significant digits, so 0.1 became 0.1m.
        //          On .NET 11 it keeps the exact binary value (0.1000000000000000055511151231).
        //          Build the decimal from the shortest text that round-trips the double instead.
        return (decimal)value;
    }

    /// <summary>Rounds a price to cents, with .5 going away from zero (2.675 becomes 2.68).</summary>
    public static decimal RoundToCents(double value)
    {
        // TODO-01: Math.Round(double, digits) used to multiply by 100, which made 2.675 look like
        //          267.5. On .NET 11 it uses the exact double (2.67499999...) and returns 2.67.
        //          Round the decimal you get from FromDouble, not the double.
        return (decimal)Math.Round(value, 2, MidpointRounding.AwayFromZero);
    }

    /// <summary>The brightness channel of a pixel: 0 to 255, values outside the range clamp to the ends.</summary>
    public static byte ToChannel(double value)
    {
        // TODO-07 (optional): Casting a double to byte went through int and wrapped around on .NET 10
        //          (300.0 became 44). On .NET 11 the cast saturates (300.0 becomes 255), but code
        //          should not depend on either: state the clamp, so every runtime gives the same answer.
        return unchecked((byte)(int)value);
    }
}
