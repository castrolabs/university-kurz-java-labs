using Microsoft.EntityFrameworkCore;

namespace UpgradeLab;

public class ShopDbContext(DbContextOptions<ShopDbContext> options) : DbContext(options)
{
    public DbSet<Customer> Customers => Set<Customer>();

    protected override void OnModelCreating(ModelBuilder modelBuilder) =>
        modelBuilder.Entity<Customer>(customer =>
        {
            customer.ComplexProperty(c => c.Address, a => a.ToJson());

            // TODO-01: On EF Core 10, Property() on a List<string> was enough to get a primitive
            //          collection. On EF Core 11 the Contains query stops translating. Fix the mapping.
            customer.Property(c => c.Tags);

            // TODO-02: OwnsMany(...).ToJson() without a key is obsolete in EF Core 11 and warns.
            //          Map Notes as a complex collection in a JSON column instead.
            customer.OwnsMany(c => c.Notes, n => n.ToJson());

            // TODO-03: Index the city inside the Address JSON document (an expression index on SQLite).
        });
}
