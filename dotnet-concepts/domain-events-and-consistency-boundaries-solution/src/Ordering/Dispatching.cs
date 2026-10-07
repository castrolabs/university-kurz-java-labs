using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Diagnostics;

namespace Ordering;

public interface IDomainEventHandler<in TEvent> where TEvent : IDomainEvent
{
    // Handlers receive the context that is being saved: they only add to it,
    // the single outer SaveChanges commits everything.
    Task HandleAsync(TEvent e, OrdersDbContext db, CancellationToken ct);
}

public sealed class DomainEventDispatcher
{
    private readonly Dictionary<Type, List<Func<IDomainEvent, OrdersDbContext, CancellationToken, Task>>> _handlers = [];

    public DomainEventDispatcher Register<TEvent>(IDomainEventHandler<TEvent> handler) where TEvent : IDomainEvent
    {
        if (!_handlers.TryGetValue(typeof(TEvent), out var list)) _handlers[typeof(TEvent)] = list = [];
        list.Add((e, db, ct) => handler.HandleAsync((TEvent)e, db, ct));
        return this;
    }

    public async Task DispatchAsync(IDomainEvent e, OrdersDbContext db, CancellationToken ct)
    {
        if (!_handlers.TryGetValue(e.GetType(), out var list)) return;
        foreach (var handle in list) await handle(e, db, ct);
    }
}

public sealed class DomainEventsInterceptor(DomainEventDispatcher dispatcher) : SaveChangesInterceptor
{
    public override async ValueTask<InterceptionResult<int>> SavingChangesAsync(
        DbContextEventData data, InterceptionResult<int> result, CancellationToken ct = default)
    {
        if (data.Context is not OrdersDbContext db) return result;

        var aggregates = db.ChangeTracker.Entries<AggregateRoot>()
            .Select(e => e.Entity)
            .Where(a => a.DomainEvents.Count > 0)
            .ToList();

        var events = aggregates.SelectMany(a => a.DomainEvents).ToList();
        aggregates.ForEach(a => a.ClearDomainEvents());

        foreach (var e in events)
            await dispatcher.DispatchAsync(e, db, ct); // before the commit: same transaction

        return result;
    }
}
