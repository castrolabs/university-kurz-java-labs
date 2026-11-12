using Shop.Api.Mediator;

namespace Shop.Api.Features.Orders;

// One file per use case: request, validator and handler. The route lives in OrdersEndpoints.
public sealed record PlaceOrder(string Sku, int Quantity) : ICommand<Guid>;

public sealed class PlaceOrderValidator : IValidator<PlaceOrder>
{
    public IEnumerable<string> Validate(PlaceOrder c)
    {
        if (string.IsNullOrWhiteSpace(c.Sku)) yield return "Sku is required.";
        if (c.Quantity is < 1 or > 100) yield return "Quantity must be between 1 and 100.";
    }
}

public sealed class PlaceOrderHandler(OrderStore store) : IRequestHandler<PlaceOrder, Guid>
{
    public Task<Guid> Handle(PlaceOrder c, CancellationToken ct)
    {
        var order = new Order(Guid.CreateVersion7(), c.Sku.Trim(), c.Quantity, false);
        store.Save(order);
        return Task.FromResult(order.Id);
    }
}
