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
        var evt = JsonSerializer.Deserialize<OrderPlaced>(envelope.Payload)
            ?? throw new InvalidOperationException("empty payload");

        await using var db = newContext();

        // Fast path for the common duplicate.
        if (await db.Inbox.AnyAsync(i => i.MessageId == envelope.MessageId && i.Consumer == Name, ct))
            return Outcome.Duplicate;

        // The effect and the inbox row commit together or not at all.
        db.Shipments.Add(new Shipment { Id = Guid.CreateVersion7(), OrderId = evt.OrderId });
        db.Inbox.Add(new InboxMessage
        {
            MessageId = envelope.MessageId,
            Consumer = Name,
            ProcessedAtMs = clock.GetUtcNow().ToUnixTimeMilliseconds(),
        });

        try
        {
            await db.SaveChangesAsync(ct);
            return Outcome.Processed;
        }
        catch (DbUpdateException ex) when (ex.InnerException is Microsoft.Data.Sqlite.SqliteException { SqliteErrorCode: 19 })
        {
            // Two deliveries raced past the check: the primary key let only one of them in.
            return Outcome.Duplicate;
        }
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
        try
        {
            await consumer.HandleAsync(envelope, ct); // returning is the ack
        }
        catch (Exception ex) when (ex is not OperationCanceledException)
        {
            if (envelope.DeliveryCount >= maxDeliveries)
            {
                log.LogError(ex, "Message {Id} dead-lettered after {N} deliveries", envelope.MessageId, envelope.DeliveryCount);
                broker.DeadLetter(envelope);
            }
            else
            {
                await broker.RedeliverAsync(envelope, ct);
            }
        }
    }
}
