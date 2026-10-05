using Microsoft.Extensions.DependencyInjection;

namespace DiLab;

public static class StartupTasks
{
    public static FakeDb SeedDatabase(IServiceProvider root)
    {
        using var scope = root.CreateScope();
        var db = scope.ServiceProvider.GetRequiredService<FakeDb>();
        db.Seed();
        return db;
    }
}
