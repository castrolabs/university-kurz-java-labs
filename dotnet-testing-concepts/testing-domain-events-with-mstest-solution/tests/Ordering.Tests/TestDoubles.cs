namespace Ordering.Tests;

// Hand-written fakes: no mocking library, the behavior is two lines each and reads like the test.
internal sealed class RecordingHandler : IDomainEventHandler<OrderPlaced>
{
    public List<OrderPlaced> Seen { get; } = [];

    public Task HandleAsync(OrderPlaced e, OrdersDbContext db, CancellationToken ct)
    {
        Seen.Add(e);
        return Task.CompletedTask;
    }
}

internal sealed class ExplodingHandler : IDomainEventHandler<OrderPlaced>
{
    public Task HandleAsync(OrderPlaced e, OrdersDbContext db, CancellationToken ct) =>
        throw new InvalidOperationException("loyalty is down");
}
