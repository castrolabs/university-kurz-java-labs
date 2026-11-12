namespace Invoicing;

public sealed record OrderLine(string Product, int Quantity, decimal UnitPrice);

public sealed record Order(string Customer, IReadOnlyList<OrderLine> Lines, string? Note = null);

public sealed record InvoiceLine(string Product, int Quantity, decimal UnitPrice, decimal Total);

public sealed record Invoice(
    Guid Id,
    string Number,
    DateTimeOffset IssuedAt,
    string Customer,
    IReadOnlyList<InvoiceLine> Lines,
    decimal Subtotal,
    decimal Tax,
    decimal Total,
    string? Note);
