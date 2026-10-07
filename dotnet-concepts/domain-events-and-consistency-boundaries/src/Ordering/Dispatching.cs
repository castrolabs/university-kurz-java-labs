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
        // TODO-01: Collect the events of every tracked AggregateRoot (db.ChangeTracker.Entries<AggregateRoot>()),
        // clear them, then dispatch each one with dispatcher.DispatchAsync(e, db, ct).
        // Hint: this method runs BEFORE the commit. Clear first, or a second SaveChanges dispatches again.
        await Task.CompletedTask;
        throw new NotImplementedException("Not implemented yet.");
    }
}
