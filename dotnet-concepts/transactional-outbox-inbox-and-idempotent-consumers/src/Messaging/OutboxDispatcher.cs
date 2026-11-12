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

        // TODO-01: Load up to options.BatchSize rows that are pending (PublishedAtMs and DeadLetteredAtMs null) and
        // due (NextAttemptAtMs <= now), ordered by Seq. For each one, publish
        // new Envelope(m.MessageId, m.Type, m.Payload) and set PublishedAtMs = now. When the publish throws (anything
        // except OperationCanceledException) call RecordFailure(m, ex, now) instead. Call SaveChangesAsync after EACH
        // message, not once at the end, and return how many were published.
        // Hint: the order is publish first, mark later. Think about which crash that ordering allows.
        _ = db; _ = now; _ = broker;
        await Task.CompletedTask;
        throw new NotImplementedException("Not implemented yet.");
    }

    public void RecordFailure(OutboxMessage m, Exception ex, long now)
    {
        // TODO-02: Count the attempt (m.Attempts++) and keep ex.Message in LastError. When Attempts reaches
        // options.MaxAttempts, dead-letter the row (DeadLetteredAtMs = now). Otherwise push NextAttemptAtMs to
        // now + Backoff(m.Attempts) in milliseconds.
        throw new NotImplementedException("Not implemented yet.");
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
