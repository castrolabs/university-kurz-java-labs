namespace Billing;

public static class MoneySplitter
{
    /// <summary>
    /// Splits an amount in cents into <paramref name="parts"/> shares, for an invoice or a refund.
    /// The shares add up to the amount and never differ by more than one cent; the larger shares come first.
    /// </summary>
    public static long[] Split(long cents, int parts)
    {
        ArgumentOutOfRangeException.ThrowIfLessThan(parts, 1);

        var share = cents / parts;
        var remainder = cents % parts;

        var result = new long[parts];
        for (var i = 0; i < parts; i++)
        {
            result[i] = share + (i < remainder ? 1 : 0);
        }
        return result;
    }
}
