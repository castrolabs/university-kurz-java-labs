using System.Globalization;

namespace Billing;

public enum InvoiceStatus { Open, Paid }

public sealed record Customer(string Name, string Email, string Country);

public sealed record InvoiceLine(string Sku, int Quantity, decimal UnitPrice)
{
    public decimal Amount => Quantity * UnitPrice;
}

public sealed class Invoice(string number, Customer customer, DateOnly issuedOn)
{
    private readonly List<InvoiceLine> _lines = [];

    public string Number { get; } = number;
    public Customer Customer { get; } = customer;
    public DateOnly IssuedOn { get; } = issuedOn;
    public DateOnly DueOn => IssuedOn.AddDays(30);
    public InvoiceStatus Status { get; private set; } = InvoiceStatus.Open;
    public IReadOnlyList<InvoiceLine> Lines => _lines;
    public decimal Total => _lines.Sum(l => l.Amount);

    public Invoice AddLine(string sku, int quantity, decimal unitPrice)
    {
        ArgumentOutOfRangeException.ThrowIfNegativeOrZero(quantity);
        _lines.Add(new InvoiceLine(sku, quantity, unitPrice));
        return this;
    }

    public void Pay()
    {
        if (Status == InvoiceStatus.Paid)
            throw new InvalidOperationException($"Invoice {Number} is already paid.");
        Status = InvoiceStatus.Paid;
    }

    public bool IsOverdue(DateOnly today) => Status == InvoiceStatus.Open && today > DueOn;

    public decimal TotalWithDiscount(decimal percent) =>
        Math.Round(Total * (100m - percent) / 100m, 2, MidpointRounding.ToEven);

    public string Summary() => $"{Number} | {Customer.Name} | {_lines.Count} lines | {Total.ToString("0.00", CultureInfo.InvariantCulture)} EUR";
}
