using System.Globalization;

namespace Invoicing;

// Every invoice gets a new Guid and the current time, so two runs never produce the same object.
// The Number is built from both: "INV-20261112-1a2b3c4d".
public sealed class InvoiceService(TimeProvider? clock = null)
{
    public const decimal TaxRate = 0.19m;

    private readonly TimeProvider _clock = clock ?? TimeProvider.System;

    public Invoice Create(Order order)
    {
        if (order.Lines.Count == 0)
        {
            throw new ArgumentException("An invoice needs at least one line.", nameof(order));
        }

        var id = Guid.NewGuid();
        var issuedAt = _clock.GetUtcNow();
        var lines = order.Lines
            .Select(line => new InvoiceLine(line.Product, line.Quantity, line.UnitPrice, line.Quantity * line.UnitPrice))
            .ToList();
        var subtotal = lines.Sum(line => line.Total);
        var tax = Math.Round(subtotal * TaxRate, 2, MidpointRounding.AwayFromZero);

        return new Invoice(
            id,
            string.Create(CultureInfo.InvariantCulture, $"INV-{issuedAt:yyyyMMdd}-{id.ToString("N")[..8]}"),
            issuedAt,
            order.Customer,
            lines,
            subtotal,
            tax,
            subtotal + tax,
            order.Note);
    }
}
