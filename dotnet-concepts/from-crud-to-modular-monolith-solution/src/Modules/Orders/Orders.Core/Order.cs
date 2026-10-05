namespace Orders.Core;

internal sealed record OrderLine(int GameId, int Quantity, decimal UnitPrice);

internal sealed record Order(Guid Id, IReadOnlyList<OrderLine> Lines);

internal sealed class OrderStore
{
    private readonly Dictionary<Guid, Order> _orders = [];
    private readonly Lock _lock = new();

    public void Add(Order order)
    {
        lock (_lock) _orders[order.Id] = order;
    }

    public Order? Find(Guid id)
    {
        lock (_lock) return _orders.GetValueOrDefault(id);
    }
}
