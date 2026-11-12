using Microsoft.EntityFrameworkCore;

namespace UpgradeLab;

public static class Bootstrap
{
    /// <summary>Creates the schema when the application starts. This project has no migrations.</summary>
    public static void Prepare(ShopDbContext db)
    {
        // EF Core 11 throws on Migrate() when the assembly has no migrations (MigrationsNotFound).
        // Without migrations the schema comes from the model.
        db.Database.EnsureCreated();
    }
}
