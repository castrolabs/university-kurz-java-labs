using Brokers;
using MassTransit;
using MassTransit.Testing;
using Microsoft.Extensions.DependencyInjection;

namespace Brokers.Tests;

public sealed class BrokersTests : IAsyncDisposable
{
    private readonly ServiceProvider _sp;
    private readonly ITestHarness _harness;
    private readonly Ledger _ledger = new();

    public BrokersTests()
    {
        _sp = new ServiceCollection()
            .AddSingleton(_ledger)
            .AddScoped<OrderApi>()
            .AddMassTransitTestHarness(x => x.AddOrderConsumers())
            .BuildServiceProvider(true);
        _harness = _sp.GetRequiredService<ITestHarness>();
    }

    private async Task<OrderApi> StartAsync()
    {
        await _harness.Start();
        return _sp.CreateScope().ServiceProvider.GetRequiredService<OrderApi>();
    }

    // The harness lists collapse messages with the same MessageId, so duplicates are counted in the ledger.
    private static async Task WaitUntil(Func<bool> condition)
    {
        for (var i = 0; i < 100 && !condition(); i++) await Task.Delay(50);
        Assert.True(condition(), "condition not reached in 5 s");
    }

    public async ValueTask DisposeAsync() => await _sp.DisposeAsync();

    [Fact]
    public async Task Publish_reaches_every_subscriber_of_the_event()
    {
        var api = await StartAsync();
        var order = Guid.NewGuid();

        await api.SubmitAsync(order, 10m);

        Assert.True(await _harness.GetConsumerHarness<AuditConsumer>().Consumed.Any<PaymentCaptured>());
        Assert.True(_ledger.Charges.ContainsKey(order));
        Assert.Equal([order], _ledger.Captured);
        Assert.True(await _harness.Published.Any<OrderSubmitted>());
    }

    [Fact]
    public async Task Send_delivers_a_command_to_the_one_queue()
    {
        var api = await StartAsync();
        var order = Guid.NewGuid();

        await api.RequestShipmentAsync(order);

        Assert.True(await _harness.GetConsumerHarness<ShippingConsumer>().Consumed.Any<ShipOrder>());
        Assert.True(await _harness.Sent.Any<ShipOrder>());
        Assert.Equal([order], _ledger.Shipments);
    }

    [Fact]
    public async Task Publish_does_not_use_the_send_path()
    {
        var api = await StartAsync();

        await api.SubmitAsync(Guid.NewGuid(), 10m);
        await _harness.GetConsumerHarness<AuditConsumer>().Consumed.Any<PaymentCaptured>();

        Assert.False(await _harness.Sent.Any<OrderSubmitted>());
        Assert.Empty(_ledger.Shipments);
    }

    [Fact]
    public async Task Retry_turns_a_transient_failure_into_success()
    {
        _ledger.FailShippingTimes = 1;
        var api = await StartAsync();
        var order = Guid.NewGuid();

        await api.RequestShipmentAsync(order);

        await WaitUntil(() => _ledger.Shipments.Count == 1);
        Assert.Equal([order], _ledger.Shipments);
        Assert.Equal(2, _ledger.ShippingAttempts);
        Assert.False(await _harness.Published.Any<Fault<ShipOrder>>());
    }

    [Fact]
    public async Task Retry_gives_up_and_publishes_a_fault()
    {
        _ledger.FailShippingTimes = 100;
        var api = await StartAsync();

        await api.RequestShipmentAsync(Guid.NewGuid());

        Assert.True(await _harness.Published.Any<Fault<ShipOrder>>());
        Assert.Equal(3, _ledger.ShippingAttempts); // 1 try + 2 retries
        Assert.Empty(_ledger.Shipments);
    }

    [Fact]
    public async Task A_poison_event_ends_in_the_fault_consumer_and_charges_nothing()
    {
        var api = await StartAsync();
        var order = Guid.NewGuid();

        await api.SubmitAsync(order, -1m);

        Assert.True(await _harness.GetConsumerHarness<BillingFaultConsumer>().Consumed.Any<Fault<OrderSubmitted>>());
        Assert.Equal([order], _ledger.DeadLetters);
        Assert.Equal(4, _ledger.BillingDeliveries); // 1 try + 3 retries
        Assert.Empty(_ledger.Charges);
        Assert.False(await _harness.Published.Any<PaymentCaptured>());
    }

    [Fact]
    public async Task The_same_message_id_twice_has_the_effect_once()
    {
        var api = await StartAsync();
        var order = Guid.NewGuid();
        var messageId = Guid.NewGuid();

        await api.SubmitAsync(order, 10m, messageId);
        await WaitUntil(() => _ledger.IsProcessed(messageId, BillingConsumer.Name));
        await api.SubmitAsync(order, 10m, messageId);
        await WaitUntil(() => _ledger.BillingDeliveries == 2);

        Assert.Equal(2, _ledger.BillingDeliveries);                         // the broker delivered twice
        Assert.Single(_ledger.Charges);
        Assert.Single(_ledger.Captured);                                    // and PaymentCaptured went out once
    }

    [Fact]
    public async Task The_outbox_holds_a_publish_until_the_consumer_succeeds()
    {
        _ledger.FailBillingTimes = 1;
        var api = await StartAsync();
        var order = Guid.NewGuid();

        await api.SubmitAsync(order, 10m);
        await WaitUntil(() => _ledger.Captured.Count >= 1);
        await Task.Delay(200); // room for a wrongly early publish to show up
        Assert.Equal(2, _ledger.BillingAttempts);                    // first try failed after publishing
        Assert.Single(_harness.Published.Select<PaymentCaptured>()); // but only the successful try released it
        Assert.Equal([order], _ledger.Captured);
    }
}
