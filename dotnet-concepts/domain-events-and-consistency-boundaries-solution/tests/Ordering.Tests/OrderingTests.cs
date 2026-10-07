using Microsoft.Data.Sqlite;
using Microsoft.EntityFrameworkCore;

namespace Ordering.Tests;

public sealed class OrderingTests : IDisposable
{
    private readonly SqliteConnection _connection = new("DataSource=:memory:");
    private readonly TimeProvider _clock = TimeProvider.System;
    private readonly Guid _customerId = Guid.NewGuid();

    public OrderingTests()
    {
        _connection.Open();
        using var db = NewContext(new DomainEventDispatcher());
        db.Database.EnsureCreated();
    }

    public void Dispose() => _connection.Dispose();

    private OrdersDbContext NewContext(DomainEventDispatcher dispatcher) =>
        new(new DbContextOptionsBuilder<OrdersDbContext>()
            .UseSqlite(_connection)
            .AddInterceptors(new DomainEventsInterceptor(dispatcher))
            .Options);

    private OrdersDbContext Plain() => NewContext(new DomainEventDispatcher());

    private sealed class Recorder : IDomainEventHandler<OrderPlaced>
    {
        public List<OrderPlaced> Seen { get; } = [];
        public Task HandleAsync(OrderPlaced e, OrdersDbContext db, CancellationToken ct)
        {
            Seen.Add(e);
            return Task.CompletedTask;
        }
    }

    private sealed class Exploding : IDomainEventHandler<OrderPlaced>
    {
        public Task HandleAsync(OrderPlaced e, OrdersDbContext db, CancellationToken ct) =>
            throw new InvalidOperationException("loyalty is down");
    }

    // TODO-00
    [Fact]
    public void PlacingAnOrderRaisesOneOrderPlacedEvent()
    {
        var order = Order.Place(_customerId, 50m, _clock);

        var e = Assert.IsType<OrderPlaced>(Assert.Single(order.DomainEvents));
        Assert.Equal(order.Id, e.OrderId);
        Assert.Equal(_customerId, e.CustomerId);
        Assert.Equal(50m, e.Total);
    }

    [Theory]
    [InlineData(0)]
    [InlineData(-5)]
    public void AnOrderWithoutAPositiveTotalIsRejectedAndRaisesNothing(int total) =>
        Assert.Throws<ArgumentOutOfRangeException>(() => Order.Place(_customerId, total, _clock));

    // TODO-01
    [Fact]
    public async Task SavingDispatchesTheEventsOfTheTrackedAggregates()
    {
        var recorder = new Recorder();
        await using var db = NewContext(new DomainEventDispatcher().Register(recorder));

        db.Orders.Add(Order.Place(_customerId, 10m, _clock));
        await db.SaveChangesAsync();

        Assert.Single(recorder.Seen);
    }

    [Fact]
    public async Task EventsAreClearedSoASecondSaveDoesNotDispatchThemAgain()
    {
        var recorder = new Recorder();
        await using var db = NewContext(new DomainEventDispatcher().Register(recorder));
        var order = Order.Place(_customerId, 10m, _clock);
        db.Orders.Add(order);

        await db.SaveChangesAsync();
        await db.SaveChangesAsync();

        Assert.Single(recorder.Seen);
        Assert.Empty(order.DomainEvents);
    }

    [Fact]
    public async Task AHandlerThatThrowsRollsBackTheOrder()
    {
        await using (var db = NewContext(new DomainEventDispatcher().Register(new Exploding())))
        {
            db.Orders.Add(Order.Place(_customerId, 10m, _clock));
            await Assert.ThrowsAsync<InvalidOperationException>(() => db.SaveChangesAsync());
        }

        await using var check = Plain();
        Assert.Equal(0, await check.Orders.CountAsync());
    }

    // TODO-02
    [Fact]
    public async Task TheOutboxRowIsSavedInTheSameTransactionAsTheOrder()
    {
        await using (var db = NewContext(new DomainEventDispatcher().Register(new OrderPlacedHandler())))
        {
            db.Orders.Add(Order.Place(_customerId, 10m, _clock));
            await db.SaveChangesAsync();
        }

        await using var check = Plain();
        var message = await check.OutboxMessages.SingleAsync();
        Assert.Equal(nameof(OrderPlacedIntegrationEvent), message.Type);
        Assert.Equal(1, await check.Orders.CountAsync());
    }

    [Fact]
    public async Task NoOrderMeansNoOutboxRowWhenTheSaveFails()
    {
        var dispatcher = new DomainEventDispatcher().Register(new OrderPlacedHandler()).Register(new Exploding());
        await using (var db = NewContext(dispatcher))
        {
            db.Orders.Add(Order.Place(_customerId, 10m, _clock));
            await Assert.ThrowsAsync<InvalidOperationException>(() => db.SaveChangesAsync());
        }

        await using var check = Plain();
        Assert.Equal(0, await check.OutboxMessages.CountAsync());
    }

    // TODO-03
    [Fact]
    public async Task TheConsumerAwardsPointsInItsOwnTransaction()
    {
        var consumer = new LoyaltyConsumer(Plain);
        var e = new OrderPlacedIntegrationEvent(Guid.NewGuid(), Guid.NewGuid(), _customerId, 42m);

        await consumer.HandleAsync(e, default);

        await using var check = Plain();
        Assert.Equal(42, (await check.Customers.SingleAsync(c => c.Id == _customerId)).Points);
    }

    [Fact]
    public async Task ADuplicateDeliveryOfTheSameMessageAwardsPointsOnlyOnce()
    {
        var consumer = new LoyaltyConsumer(Plain);
        var e = new OrderPlacedIntegrationEvent(Guid.NewGuid(), Guid.NewGuid(), _customerId, 42m);

        await consumer.HandleAsync(e, default);
        await consumer.HandleAsync(e, default);

        await using var check = Plain();
        Assert.Equal(42, (await check.Customers.SingleAsync()).Points);
        Assert.Equal(1, await check.ProcessedMessages.CountAsync());
    }

    [Fact]
    public async Task DifferentMessagesForTheSameCustomerAccumulate()
    {
        var consumer = new LoyaltyConsumer(Plain);

        await consumer.HandleAsync(new(Guid.NewGuid(), Guid.NewGuid(), _customerId, 10m), default);
        await consumer.HandleAsync(new(Guid.NewGuid(), Guid.NewGuid(), _customerId, 5m), default);

        await using var check = Plain();
        Assert.Equal(15, (await check.Customers.SingleAsync()).Points);
    }
}
