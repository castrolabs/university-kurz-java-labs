using Microsoft.Extensions.DependencyInjection;

namespace Tenancy.Application;

public static class DependencyInjection
{
    public static IServiceCollection AddApplication(this IServiceCollection services)
    {
        services.AddScoped<CreateTenantHandler>();
        services.AddScoped<ResolveTenantHandler>();
        services.AddScoped<TenantContext>();
        services.AddScoped<ITenantContext>(sp => sp.GetRequiredService<TenantContext>());

        return services;
    }
}
