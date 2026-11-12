using MassTransit;

namespace Brokers;

public sealed class BillingConsumer(Ledger ledger) : IConsumer<OrderSubmitted>
{
    public const string Name = "billing";

    public async Task Consume(ConsumeContext<OrderSubmitted> context)
    {
        Interlocked.Increment(ref ledger.BillingDeliveries);
        var id = context.MessageId!.Value;
        if (ledger.IsProcessed(id, Name)) return;                 // a redelivery of something already done

        if (context.Message.Total < 0) throw new InvalidOperationException("negative total");
        ledger.Charges.TryAdd(context.Message.OrderId, 0);
        await context.Publish(new PaymentCaptured(context.Message.OrderId));

        if (Interlocked.Increment(ref ledger.BillingAttempts) <= ledger.FailBillingTimes)
            throw new InvalidOperationException("commit failed after publish");
        ledger.MarkProcessed(id, Name);                           // last: the "inbox row" commits with the effect
    }
}

// Receives the command. Fails a configurable number of times first, to exercise retry.
public sealed class ShippingConsumer(Ledger ledger) : IConsumer<ShipOrder>
{
    public Task Consume(ConsumeContext<ShipOrder> context)
    {
        if (Interlocked.Increment(ref ledger.ShippingAttempts) <= ledger.FailShippingTimes)
            throw new InvalidOperationException("carrier unavailable");
        ledger.Shipments.Add(context.Message.OrderId);
        return Task.CompletedTask;
    }
}

// A second subscriber to PaymentCaptured: every subscriber gets its own copy (fan-out).
public sealed class AuditConsumer(Ledger ledger) : IConsumer<PaymentCaptured>
{
    public Task Consume(ConsumeContext<PaymentCaptured> context)
    {
        ledger.Captured.Add(context.Message.OrderId);
        return Task.CompletedTask;
    }
}

// Fault<T> is published by MassTransit after the retries are exhausted.
public sealed class BillingFaultConsumer(Ledger ledger) : IConsumer<Fault<OrderSubmitted>>
{
    public Task Consume(ConsumeContext<Fault<OrderSubmitted>> context)
    {
        ledger.DeadLetters.Add(context.Message.Message.OrderId);
        return Task.CompletedTask;
    }
}
