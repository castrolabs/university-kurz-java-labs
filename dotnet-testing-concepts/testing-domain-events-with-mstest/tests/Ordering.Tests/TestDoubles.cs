namespace Ordering.Tests;

// Hand-written fakes: no mocking library. Each one is a few lines and reads like the test that uses it.

// TODO-01: Make this handler remember every OrderPlaced it receives, in a list the tests can read.
internal sealed class RecordingHandler : IDomainEventHandler<OrderPlaced>
{
    public Task HandleAsync(OrderPlaced e, OrdersDbContext db, CancellationToken ct) =>
        throw new NotImplementedException("TODO-01: record the event.");
}

// TODO-01: Make this handler throw an InvalidOperationException, like a loyalty service that is down.
internal sealed class ExplodingHandler : IDomainEventHandler<OrderPlaced>
{
    public Task HandleAsync(OrderPlaced e, OrdersDbContext db, CancellationToken ct) =>
        throw new NotImplementedException("TODO-01: throw the exception the tests expect.");
}
