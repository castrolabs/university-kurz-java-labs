using Shop.Api.Mediator;

namespace Shop.Api.Features.Orders;

public sealed record GetOrder(Guid Id) : IQuery<OrderDto?>;

public sealed record OrderDto(Guid Id, string Sku, int Quantity);

public sealed class GetOrderHandler(OrderStore store) : IRequestHandler<GetOrder, OrderDto?>
{
    public Task<OrderDto?> Handle(GetOrder q, CancellationToken ct)
    {
        var o = store.Find(q.Id);
        return Task.FromResult(o is null ? null : new OrderDto(o.Id, o.Sku, o.Quantity));
    }
}
