using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;

namespace ConfigLab;

public static class StoreOptionsRegistration
{
    public static IServiceCollection AddStoreOptions(this IServiceCollection services, IConfiguration configuration)
    {
        services.AddOptions<StoreOptions>()
            .Bind(configuration.GetSection(StoreOptions.SectionName))
            .ValidateDataAnnotations()
            .ValidateOnStart();

        return services;
    }
}
