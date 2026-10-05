using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.DependencyInjection;
using Tenancy.Application;
using Tenancy.Infrastructure.Persistence;

namespace Tenancy.Infrastructure;

public static class DependencyInjection
{
    public static IServiceCollection AddInfrastructure(this IServiceCollection services, string connectionString)
    {
        services.AddDbContext<TenancyDbContext>(options => options.UseSqlite(connectionString));
        services.AddScoped<ITenantRepository, TenantRepository>();

        return services;
    }

    public static void InitializeDatabase(this IServiceProvider services)
    {
        using var scope = services.CreateScope();
        scope.ServiceProvider.GetRequiredService<TenancyDbContext>().Database.EnsureCreated();
    }
}
