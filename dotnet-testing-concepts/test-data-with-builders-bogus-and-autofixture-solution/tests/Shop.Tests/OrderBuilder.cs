namespace Shop.Tests;

// Test Data Builder: a valid order by default, and a method per thing a test wants to say out loud.
// The defaults are fixed values (no Guid.NewGuid, no clock), so a failure always reproduces.
public sealed class OrderBuilder
{
    private bool _premium;
    private string _email = "ana@example.com";
    private List<OrderLine> _lines = [new("SKU-1", 1, 10m)];

    public OrderBuilder Premium() { _premium = true; return this; }

    public OrderBuilder WithEmail(string email) { _email = email; return this; }

    public OrderBuilder WithLines(params OrderLine[] lines) { _lines = [.. lines]; return this; }

    // One line worth exactly `total`: the number that matters for the discount rule.
    public OrderBuilder Worth(decimal total) => WithLines(new OrderLine("SKU-1", 1, total));

    public Order Build() => new(
        new Guid("00000000-0000-0000-0000-000000000001"),
        new Customer(new Guid("00000000-0000-0000-0000-0000000000c1"), "Ana", _email, _premium),
        _lines);
}
