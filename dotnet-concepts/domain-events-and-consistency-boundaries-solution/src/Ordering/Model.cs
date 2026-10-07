namespace Ordering;

public sealed class Order : AggregateRoot
{
    public Guid Id { get; private set; }
    public Guid CustomerId { get; private set; }
    public decimal Total { get; private set; }

    private Order() { }

    public static Order Place(Guid customerId, decimal total, TimeProvider clock)
    {
        if (total <= 0) throw new ArgumentOutOfRangeException(nameof(total), "An order needs a positive total.");

        var order = new Order { Id = Guid.CreateVersion7(), CustomerId = customerId, Total = total };
        order.Raise(new OrderPlaced(order.Id, customerId, total, clock.GetUtcNow()));
        return order;
    }
}

public sealed class Customer : AggregateRoot
{
    public Guid Id { get; private set; }
    public int Points { get; private set; }

    private Customer() { }

    public Customer(Guid id) => Id = id;

    public void AwardPoints(decimal orderTotal) => Points += (int)orderTotal;
}
