using Microsoft.EntityFrameworkCore;

namespace Ordering;

public sealed class OrdersDbContext(DbContextOptions<OrdersDbContext> options) : DbContext(options)
{
    public DbSet<Order> Orders => Set<Order>();
    public DbSet<Customer> Customers => Set<Customer>();
    public DbSet<OutboxMessage> OutboxMessages => Set<OutboxMessage>();
    public DbSet<ProcessedMessage> ProcessedMessages => Set<ProcessedMessage>();

    protected override void OnModelCreating(ModelBuilder b)
    {
        b.Entity<Order>().Ignore(o => o.DomainEvents);
        b.Entity<Customer>().Ignore(c => c.DomainEvents);
        b.Entity<ProcessedMessage>().HasKey(p => new { p.MessageId, p.Consumer });
    }
}
