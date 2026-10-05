namespace Orders.Core;

// TODO-03: Make Order, OrderLine and OrderStore internal. Only OrdersModule stays public.

public sealed record OrderLine(int GameId, int Quantity, decimal UnitPrice);

public sealed record Order(Guid Id, IReadOnlyList<OrderLine> Lines);

public sealed class OrderStore
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
