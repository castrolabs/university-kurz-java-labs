using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;

namespace Messaging;

public sealed record OutboxOptions(int MaxAttempts = 5, int BatchSize = 20, TimeSpan? BaseDelay = null)
{
    public TimeSpan Delay => BaseDelay ?? TimeSpan.FromSeconds(1);
}

public sealed class OutboxDispatcher(
    Func<AppDbContext> newContext, IBroker broker, TimeProvider clock, OutboxOptions options)
{
    // Exponential backoff: BaseDelay * 2^(attempts - 1).
    public TimeSpan Backoff(int attempts) => options.Delay * Math.Pow(2, attempts - 1);

    // One pass of the relay. Returns how many messages reached the broker.
    public async Task<int> DispatchBatchAsync(CancellationToken ct)
    {
        await using var db = newContext();
        var now = clock.GetUtcNow().ToUnixTimeMilliseconds();

        var batch = await db.Outbox
            .Where(m => m.PublishedAtMs == null && m.DeadLetteredAtMs == null && m.NextAttemptAtMs <= now)
            .OrderBy(m => m.Seq)
            .Take(options.BatchSize)
            .ToListAsync(ct);

        var published = 0;
        foreach (var m in batch)
        {
            try
            {
                await broker.PublishAsync(new Envelope(m.MessageId, m.Type, m.Payload), ct);
                m.PublishedAtMs = now;
                published++;
            }
            catch (Exception ex) when (ex is not OperationCanceledException)
            {
                RecordFailure(m, ex, now);
            }

            // Saved per message: a crash after the publish loses at most the mark of this one message,
            // which means one duplicate on restart, never a lost message.
            await db.SaveChangesAsync(ct);
        }
        return published;
    }

    public void RecordFailure(OutboxMessage m, Exception ex, long now)
    {
        m.Attempts++;
        m.LastError = ex.Message;
        if (m.Attempts >= options.MaxAttempts)
            m.DeadLetteredAtMs = now;
        else
            m.NextAttemptAtMs = now + (long)Backoff(m.Attempts).TotalMilliseconds;
    }
}

// The relay as a hosted service: poll, dispatch, survive failures, never exit on its own.
public sealed class OutboxDispatcherService(
    OutboxDispatcher dispatcher, TimeSpan interval, ILogger<OutboxDispatcherService> log) : BackgroundService
{
    protected override async Task ExecuteAsync(CancellationToken stoppingToken)
    {
        using var timer = new PeriodicTimer(interval);
        do
        {
            try
            {
                await dispatcher.DispatchBatchAsync(stoppingToken);
            }
            catch (Exception ex) when (ex is not OperationCanceledException)
            {
                // An unhandled exception would stop the host (BackgroundServiceExceptionBehavior.StopHost).
                log.LogError(ex, "Outbox dispatch failed, retrying on the next tick");
            }
        }
        while (await WaitAsync(timer, stoppingToken));
    }

    private static async Task<bool> WaitAsync(PeriodicTimer timer, CancellationToken ct)
    {
        try { return await timer.WaitForNextTickAsync(ct); }
        catch (OperationCanceledException) { return false; }
    }
}
