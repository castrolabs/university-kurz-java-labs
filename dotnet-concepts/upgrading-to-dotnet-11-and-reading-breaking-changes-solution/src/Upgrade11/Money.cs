using System.Globalization;

namespace Upgrade11;

public static class Money
{
    /// <summary>
    /// Converts a price that arrived as a <see cref="double"/> (a JSON number, a spreadsheet cell)
    /// to the decimal the person typed: 0.1 must become 0.1m.
    /// </summary>
    public static decimal FromDouble(double value) =>
        // "R" is the shortest text that round-trips the double, so 0.1 gives "0.1" on every runtime.
        decimal.Parse(value.ToString("R", CultureInfo.InvariantCulture), NumberStyles.Float, CultureInfo.InvariantCulture);

    /// <summary>Rounds a price to cents, with .5 going away from zero (2.675 becomes 2.68).</summary>
    public static decimal RoundToCents(double value) =>
        Math.Round(FromDouble(value), 2, MidpointRounding.AwayFromZero);

    /// <summary>The brightness channel of a pixel: 0 to 255, values outside the range clamp to the ends.</summary>
    public static byte ToChannel(double value) =>
        (byte)Math.Clamp(value, 0, 255);
}
