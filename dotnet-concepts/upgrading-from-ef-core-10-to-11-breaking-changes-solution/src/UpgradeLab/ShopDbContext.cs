using Microsoft.EntityFrameworkCore;

namespace UpgradeLab;

public class ShopDbContext(DbContextOptions<ShopDbContext> options) : DbContext(options)
{
    public DbSet<Customer> Customers => Set<Customer>();

    protected override void OnModelCreating(ModelBuilder modelBuilder) =>
        modelBuilder.Entity<Customer>(customer =>
        {
            customer.ComplexProperty(c => c.Address, a => a.ToJson());

            // Primitive collections are explicit in EF Core 11: Property() no longer configures them.
            customer.PrimitiveCollection(c => c.Tags);

            customer.ComplexCollection(c => c.Notes, n => n.ToJson());

            // EF Core 11 creates a real expression index over the JSON path on SQLite.
            customer.HasIndex("Address.City");
        });
}
