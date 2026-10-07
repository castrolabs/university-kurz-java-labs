using Microsoft.Data.Sqlite;
using Microsoft.EntityFrameworkCore;

namespace Ordering.Tests;

// An in-memory SQLite database lives only as long as its connection, so one connection is opened
// per test and shared by every context the test creates. Several contexts over the same connection
// let a test look at what was really committed, not at what one context still has in memory.
internal sealed class TestDatabase : IDisposable
{
    private readonly SqliteConnection _connection = new("DataSource=:memory:");

    public TestDatabase()
    {
        _connection.Open();
        using var db = Plain();
        db.Database.EnsureCreated();
    }

    public OrdersDbContext NewContext(DomainEventDispatcher dispatcher) =>
        new(new DbContextOptionsBuilder<OrdersDbContext>()
            .UseSqlite(_connection)
            .AddInterceptors(new DomainEventsInterceptor(dispatcher))
            .Options);

    // A context with no handlers: use it to read back the committed state.
    public OrdersDbContext Plain() => NewContext(new DomainEventDispatcher());

    public void Dispose() => _connection.Dispose();
}
