using Billing;

namespace Billing.Tests;

// The invoice every test in this lab looks at: two lines, 25.48 EUR, open, issued on 2026-03-01.
internal static class Samples
{
    public static Customer Ada => new("Ada Lovelace", "ada@example.com", "UK");

    public static Invoice Invoice() =>
        new Invoice("INV-001", Ada, new DateOnly(2026, 3, 1))
            .AddLine("A-100", 2, 9.99m)
            .AddLine("B-200", 1, 5.50m);
}
