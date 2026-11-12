using Microsoft.EntityFrameworkCore;

namespace UpgradeLab;

public static class Bootstrap
{
    /// <summary>Creates the schema when the application starts. This project has no migrations.</summary>
    public static void Prepare(ShopDbContext db)
    {
        // TODO-00: This returned quietly on EF Core 10. On EF Core 11 it throws, because the
        //          assembly has no migrations. Use the call that creates the schema from the model.
        db.Database.Migrate();
    }
}
