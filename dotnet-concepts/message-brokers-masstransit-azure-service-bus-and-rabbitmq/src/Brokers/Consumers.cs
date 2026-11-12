using MassTransit;

namespace Brokers;

public sealed class BillingConsumer(Ledger ledger) : IConsumer<OrderSubmitted>
{
    public const string Name = "billing";

    public Task Consume(ConsumeContext<OrderSubmitted> context)
    {
        // TODO-03: inbox. Read context.MessageId (a Guid?, set by the publisher). When ledger.IsProcessed(id, Name) is
        // true this is a redelivery of something already done: return without side effects. Call
        // ledger.MarkProcessed(id, Name) as the LAST step, after the effect succeeded.
        // TODO-00: when context.Message.Total is below zero throw InvalidOperationException("negative total"). Otherwise
        // record the charge (ledger.Charges.TryAdd(orderId, 0)) and context.Publish a PaymentCaptured.
        // Keep the two lines the tests rely on: Interlocked.Increment(ref ledger.BillingDeliveries) first, and after the
        // publish: if (Interlocked.Increment(ref ledger.BillingAttempts) <= ledger.FailBillingTimes)
        //     throw new InvalidOperationException("commit failed after publish");
        throw new NotImplementedException("Not implemented yet.");
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
        // TODO-05: record context.Message.Message.OrderId in ledger.DeadLetters. MassTransit publishes Fault<T> once the
        // retries are exhausted, so this is the place where "give up" becomes visible to the application.
        throw new NotImplementedException("Not implemented yet.");
    }
}
