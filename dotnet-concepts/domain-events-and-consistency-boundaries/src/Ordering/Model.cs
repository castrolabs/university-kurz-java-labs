namespace Ordering;

public sealed class Order : AggregateRoot
{
    public Guid Id { get; private set; }
    public Guid CustomerId { get; private set; }
    public decimal Total { get; private set; }

    private Order() { }

    public static Order Place(Guid customerId, decimal total, TimeProvider clock)
    {
        // TODO-00: Reject a total <= 0 with ArgumentOutOfRangeException. Otherwise build the order
        // (Id = Guid.CreateVersion7()) and Raise an OrderPlaced event, using clock.GetUtcNow() for At.
        // Hint: the aggregate only records the event. It must not publish it.
        throw new NotImplementedException("Not implemented yet.");
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
