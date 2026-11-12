namespace Shop.Api.Features.Orders;

public sealed record Order(Guid Id, string Sku, int Quantity, bool Cancelled);

public sealed class OrderStore
{
    private readonly Dictionary<Guid, Order> _orders = [];
    public void Save(Order order) => _orders[order.Id] = order;
    public Order? Find(Guid id) => _orders.GetValueOrDefault(id);
}
