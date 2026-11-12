using Microsoft.EntityFrameworkCore;

namespace Messaging;

public sealed class AppDbContext(DbContextOptions<AppDbContext> options) : DbContext(options)
{
    public DbSet<Order> Orders => Set<Order>();
    public DbSet<Shipment> Shipments => Set<Shipment>();
    public DbSet<OutboxMessage> Outbox => Set<OutboxMessage>();
    public DbSet<InboxMessage> Inbox => Set<InboxMessage>();

    protected override void OnModelCreating(ModelBuilder b)
    {
        b.Entity<OutboxMessage>(e =>
        {
            e.HasKey(m => m.Seq);
            e.HasIndex(m => m.MessageId).IsUnique();
            // The relay only scans what is still pending: a small partial index instead of the whole history.
            e.HasIndex(m => m.NextAttemptAtMs).HasFilter("PublishedAtMs IS NULL AND DeadLetteredAtMs IS NULL");
        });

        // The composite key IS the deduplication: a second insert of the same pair fails.
        b.Entity<InboxMessage>().HasKey(i => new { i.MessageId, i.Consumer });
    }
}
