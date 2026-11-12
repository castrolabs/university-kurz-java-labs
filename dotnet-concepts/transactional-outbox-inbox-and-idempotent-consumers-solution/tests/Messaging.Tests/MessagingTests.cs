using System.Text.Json;
using Messaging;
using Microsoft.Data.Sqlite;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Diagnostics;
using Microsoft.Extensions.Logging.Abstractions;

namespace Messaging.Tests;

// Fault injection: throws on the next n saves whose change set matches. Stands for a crash or a dead disk.
public sealed class FaultyInterceptor(Func<DbContext, bool> when) : SaveChangesInterceptor
{
    public int Remaining;

    public override ValueTask<InterceptionResult<int>> SavingChangesAsync(
        DbContextEventData data, InterceptionResult<int> result, CancellationToken ct = default)
    {
        if (data.Context is { } ctx && Remaining > 0 && when(ctx))
        {
            Remaining--;
            throw new InvalidOperationException("injected failure");
        }
        return ValueTask.FromResult(result);
    }
}

public sealed class ManualClock : TimeProvider
{
    private DateTimeOffset _now = new(2026, 11, 12, 10, 0, 0, TimeSpan.Zero);
    public override DateTimeOffset GetUtcNow() => _now;
    public void Advance(TimeSpan by) => _now += by;
}

public sealed class MessagingTests : IDisposable
{
    private readonly string _file = Path.Combine(Path.GetTempPath(), $"outbox-lab-{Guid.NewGuid():N}.db");
    private readonly ManualClock _clock = new();
    private readonly InMemoryBroker _broker = new();

    // Fails the next save that modifies an outbox row (the "mark as published" write).
    private readonly FaultyInterceptor _failMarkPublished =
        new(ctx => ctx.ChangeTracker.Entries<OutboxMessage>().Any(e => e.State == EntityState.Modified));

    // Fails the next save that inserts a shipment (the consumer's effect).
    private readonly FaultyInterceptor _failShipment =
        new(ctx => ctx.ChangeTracker.Entries<Shipment>().Any(e => e.State == EntityState.Added));

    public MessagingTests()
    {
        using var db = NewContext(faulty: false);
        db.Database.EnsureCreated();
    }

    public void Dispose()
    {
        SqliteConnection.ClearAllPools();
        foreach (var f in Directory.GetFiles(Path.GetTempPath(), Path.GetFileName(_file) + "*")) File.Delete(f);
    }

    private AppDbContext NewContext(bool faulty = true)
    {
        var b = new DbContextOptionsBuilder<AppDbContext>().UseSqlite($"Data Source={_file}");
        if (faulty) b.AddInterceptors(_failMarkPublished, _failShipment);
        return new AppDbContext(b.Options);
    }

    private OutboxDispatcher NewDispatcher(int maxAttempts = 5) =>
        new(() => NewContext(), _broker, _clock, new OutboxOptions(maxAttempts));

    private ShippingConsumer NewConsumer() => new(() => NewContext(), _clock);

    private ConsumerRunner NewRunner(int maxDeliveries = 3) =>
        new(_broker, NewConsumer(), NullLogger<ConsumerRunner>.Instance, maxDeliveries);

    private async Task<Guid> Place(string customer = "ana", Guid? id = null)
    {
        await using var db = NewContext();
        return await OrderService.PlaceOrderAsync(db, customer, 10m, _clock, id);
    }

    private List<Envelope> Drain()
    {
        var list = new List<Envelope>();
        while (_broker.Reader.TryRead(out var e)) list.Add(e);
        return list;
    }

    // TODO-00
    [Fact]
    public async Task PlacingAnOrderWritesTheOrderAndTheOutboxRowTogether()
    {
        var orderId = await Place();

        await using var db = NewContext();
        Assert.Equal(1, await db.Orders.CountAsync());
        var row = await db.Outbox.SingleAsync();
        Assert.Null(row.PublishedAtMs);
        Assert.Equal(orderId, JsonSerializer.Deserialize<OrderPlaced>(row.Payload)!.OrderId);
    }

    // TODO-00
    [Fact]
    public async Task AFailedCommitLeavesNoOutboxRowBehind()
    {
        var id = await Place();

        // Same order id: the order insert violates the primary key, so the whole SaveChanges rolls back.
        await Assert.ThrowsAsync<DbUpdateException>(() => Place(id: id));

        await using var db = NewContext();
        Assert.Equal(1, await db.Orders.CountAsync());
        Assert.Equal(1, await db.Outbox.CountAsync()); // never an event for an order that does not exist
    }

    // TODO-01
    [Fact]
    public async Task CrashBetweenCommitAndPublishLosesNothing()
    {
        await Place();
        // The process died here: nothing was published. A brand-new dispatcher (the restart) finds the row.
        var restarted = NewDispatcher();

        Assert.Equal(1, await restarted.DispatchBatchAsync(default));

        Assert.Single(Drain());
        await using var db = NewContext();
        Assert.NotNull((await db.Outbox.SingleAsync()).PublishedAtMs);
    }

    // TODO-01
    [Fact]
    public async Task MessagesAreRelayedInCommitOrderAndOnlyOnce()
    {
        var ids = new List<Guid>();
        for (var i = 0; i < 3; i++) ids.Add(await Place($"c{i}"));
        var dispatcher = NewDispatcher();

        await dispatcher.DispatchBatchAsync(default);
        await dispatcher.DispatchBatchAsync(default); // nothing pending any more

        var seen = Drain().Select(e => JsonSerializer.Deserialize<OrderPlaced>(e.Payload)!.OrderId).ToList();
        Assert.Equal(ids, seen);
    }

    // TODO-02
    [Fact]
    public async Task ABrokerOutageSchedulesARetryWithBackoff()
    {
        await Place();
        _broker.FailNextPublishes(1);
        var dispatcher = NewDispatcher();

        Assert.Equal(0, await dispatcher.DispatchBatchAsync(default));
        await using (var db = NewContext())
        {
            var row = await db.Outbox.SingleAsync();
            Assert.Equal(1, row.Attempts);
            Assert.Equal("broker unavailable", row.LastError);
            Assert.Null(row.PublishedAtMs);
        }

        Assert.Equal(0, await dispatcher.DispatchBatchAsync(default)); // not due yet
        _clock.Advance(dispatcher.Backoff(1));
        Assert.Equal(1, await dispatcher.DispatchBatchAsync(default)); // due now, broker is back
        Assert.Single(Drain());
    }

    // TODO-02
    [Fact]
    public async Task ExhaustedAttemptsMoveTheMessageToTheDeadLetter()
    {
        await Place();
        _broker.FailNextPublishes(100);
        var dispatcher = NewDispatcher(maxAttempts: 3);

        for (var i = 0; i < 5; i++)
        {
            await dispatcher.DispatchBatchAsync(default);
            _clock.Advance(TimeSpan.FromMinutes(1));
        }

        await using var db = NewContext();
        var row = await db.Outbox.SingleAsync();
        Assert.Equal(3, row.Attempts);
        Assert.NotNull(row.DeadLetteredAtMs);

        _broker.FailNextPublishes(0); // the broker recovers, but a dead-lettered row is no longer relayed
        Assert.Equal(0, await dispatcher.DispatchBatchAsync(default));
        Assert.Empty(Drain());
    }

    // TODO-02
    [Fact]
    public async Task CrashBetweenPublishAndMarkPublishedDeliversTwiceAndTheInboxAbsorbsIt()
    {
        await Place();
        _failMarkPublished.Remaining = 1; // the publish works, the "mark as published" write dies
        var dispatcher = NewDispatcher();

        await Assert.ThrowsAsync<InvalidOperationException>(() => dispatcher.DispatchBatchAsync(default));
        await dispatcher.DispatchBatchAsync(default); // next tick: the row still looks pending

        var deliveries = Drain();
        Assert.Equal(2, deliveries.Count);
        Assert.Equal(deliveries[0].MessageId, deliveries[1].MessageId);

        var consumer = NewConsumer();
        Assert.Equal(Outcome.Processed, await consumer.HandleAsync(deliveries[0], default));
        Assert.Equal(Outcome.Duplicate, await consumer.HandleAsync(deliveries[1], default));
        await using var db = NewContext();
        Assert.Equal(1, await db.Shipments.CountAsync());
    }

    // TODO-03
    [Fact]
    public async Task TheSameMessageDeliveredTwiceHasTheEffectOnce()
    {
        await Place();
        await NewDispatcher().DispatchBatchAsync(default);
        var envelope = Drain().Single();
        var consumer = NewConsumer();

        Assert.Equal(Outcome.Processed, await consumer.HandleAsync(envelope, default));
        Assert.Equal(Outcome.Duplicate, await consumer.HandleAsync(envelope, default));

        await using var db = NewContext();
        Assert.Equal(1, await db.Shipments.CountAsync());
        Assert.Equal(1, await db.Inbox.CountAsync());
    }

    // TODO-03
    [Fact]
    public async Task TheEffectAndTheInboxRowAreAtomic()
    {
        await Place();
        await NewDispatcher().DispatchBatchAsync(default);
        var envelope = Drain().Single();
        var consumer = NewConsumer();
        _failShipment.Remaining = 1;

        await Assert.ThrowsAsync<InvalidOperationException>(() => consumer.HandleAsync(envelope, default));
        await using (var db = NewContext())
        {
            Assert.Equal(0, await db.Shipments.CountAsync());
            Assert.Equal(0, await db.Inbox.CountAsync()); // no "processed" mark without the effect
        }

        Assert.Equal(Outcome.Processed, await consumer.HandleAsync(envelope, default)); // the retry works
    }

    // TODO-03
    [Fact]
    public async Task ConcurrentDuplicatesProduceOneEffect()
    {
        await Place();
        await NewDispatcher().DispatchBatchAsync(default);
        var envelope = Drain().Single();
        var consumer = NewConsumer();

        var outcomes = await Task.WhenAll(Enumerable.Range(0, 4).Select(_ => Task.Run(() => consumer.HandleAsync(envelope, default))));

        Assert.Equal(1, outcomes.Count(o => o == Outcome.Processed));
        await using var db = NewContext();
        Assert.Equal(1, await db.Shipments.CountAsync());
    }

    // TODO-03
    [Fact]
    public async Task TwoRelaysOnTheSameDatabaseMayDuplicateButTheEffectStaysSingle()
    {
        await Place();
        var (a, b) = (NewDispatcher(), NewDispatcher());

        await Task.WhenAll(Task.Run(() => a.DispatchBatchAsync(default)), Task.Run(() => b.DispatchBatchAsync(default)));

        var deliveries = Drain();
        Assert.InRange(deliveries.Count, 1, 2); // 2 when both read the row before either marked it
        var consumer = NewConsumer();
        foreach (var d in deliveries) await consumer.HandleAsync(d, default);
        await using var db = NewContext();
        Assert.Equal(1, await db.Shipments.CountAsync());
    }

    // TODO-04
    [Fact]
    public async Task AConsumerFailureIsRedeliveredAndSucceedsOnTheNextAttempt()
    {
        await Place();
        await NewDispatcher().DispatchBatchAsync(default);
        var runner = NewRunner();
        _failShipment.Remaining = 1;

        await runner.ProcessAsync(Drain().Single(), default);

        var redelivered = Drain().Single();
        Assert.Equal(2, redelivered.DeliveryCount);
        await runner.ProcessAsync(redelivered, default);
        Assert.Empty(Drain());
        await using var db = NewContext();
        Assert.Equal(1, await db.Shipments.CountAsync());
    }

    // TODO-04
    [Fact]
    public async Task APoisonMessageEndsInTheDeadLetterAfterMaxDeliveries()
    {
        var poison = new Envelope(Guid.NewGuid(), nameof(OrderPlaced), "not json");
        var runner = NewRunner(maxDeliveries: 3);

        await runner.ProcessAsync(poison, default);
        await runner.ProcessAsync(Drain().Single(), default);
        await runner.ProcessAsync(Drain().Single(), default);

        Assert.Empty(Drain());
        var dead = Assert.Single(_broker.DeadLetters);
        Assert.Equal(3, dead.DeliveryCount);
    }

    // TODO-05
    [Fact]
    public async Task EndToEndWithBackgroundServicesSurvivesBrokerAndDatabaseFaults()
    {
        var dispatcher = new OutboxDispatcher(() => NewContext(), _broker, TimeProvider.System,
            new OutboxOptions(MaxAttempts: 8, BaseDelay: TimeSpan.FromMilliseconds(20)));
        using var relay = new OutboxDispatcherService(dispatcher, TimeSpan.FromMilliseconds(20), NullLogger<OutboxDispatcherService>.Instance);
        using var runner = NewRunner();
        _broker.FailNextPublishes(2);
        _failMarkPublished.Remaining = 1;
        _failShipment.Remaining = 1;

        await relay.StartAsync(default);
        await runner.StartAsync(default);
        for (var i = 0; i < 5; i++) await Place($"c{i}");

        await Eventually(async () =>
        {
            await using var db = NewContext(faulty: false);
            return await db.Shipments.CountAsync() == 5;
        });
        await relay.StopAsync(default);
        await runner.StopAsync(default);

        await using var check = NewContext(faulty: false);
        Assert.Equal(5, await check.Shipments.CountAsync());
        Assert.Equal(5, await check.Shipments.Select(s => s.OrderId).Distinct().CountAsync());
        Assert.Empty(_broker.DeadLetters);
    }

    private static async Task Eventually(Func<Task<bool>> condition)
    {
        var deadline = DateTime.UtcNow.AddSeconds(15);
        while (DateTime.UtcNow < deadline)
        {
            if (await condition()) return;
            await Task.Delay(25);
        }
        Assert.Fail("condition not reached in time");
    }
}
