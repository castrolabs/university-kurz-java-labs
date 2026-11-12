using System.Text.Json;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;

namespace Messaging;

public enum Outcome { Processed, Duplicate }

public sealed class ShippingConsumer(Func<AppDbContext> newContext, TimeProvider clock)
{
    public const string Name = "shipping";

    public async Task<Outcome> HandleAsync(Envelope envelope, CancellationToken ct)
    {
        // TODO-03: Deserialize the OrderPlaced payload, open a context with newContext(), add a Shipment
        // (Id = Guid.CreateVersion7(), OrderId from the event) AND an InboxMessage(envelope.MessageId, Name, now in
        // unix ms), and save both with ONE SaveChangesAsync. Return Outcome.Processed.
        // Hint: if the inbox row already exists this is a duplicate: return Outcome.Duplicate, never throw.
        // Check first (AnyAsync) for the common case, and also catch the DbUpdateException that the composite
        // primary key raises when two deliveries race (SqliteException with SqliteErrorCode 19).
        _ = clock; _ = newContext;
        await Task.CompletedTask;
        throw new NotImplementedException("Not implemented yet.");
    }
}

// Reads the queue and decides what a failure means: redeliver, or give up and dead-letter.
public sealed class ConsumerRunner(
    InMemoryBroker broker, ShippingConsumer consumer, ILogger<ConsumerRunner> log, int maxDeliveries = 3)
    : BackgroundService
{
    protected override async Task ExecuteAsync(CancellationToken stoppingToken)
    {
        try
        {
            await foreach (var envelope in broker.Reader.ReadAllAsync(stoppingToken))
                await ProcessAsync(envelope, stoppingToken);
        }
        catch (OperationCanceledException) { }
    }

    public async Task ProcessAsync(Envelope envelope, CancellationToken ct)
    {
        // TODO-04: Call consumer.HandleAsync(envelope, ct) (returning is the ack). When it throws (anything except
        // OperationCanceledException): if envelope.DeliveryCount >= maxDeliveries, log it and call
        // broker.DeadLetter(envelope). Otherwise call broker.RedeliverAsync(envelope, ct).
        _ = log; _ = maxDeliveries; _ = broker; _ = consumer;
        await Task.CompletedTask;
        throw new NotImplementedException("Not implemented yet.");
    }
}
