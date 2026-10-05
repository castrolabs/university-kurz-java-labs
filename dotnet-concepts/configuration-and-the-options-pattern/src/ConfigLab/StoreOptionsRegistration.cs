using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;

namespace ConfigLab;

public static class StoreOptionsRegistration
{
    public static IServiceCollection AddStoreOptions(this IServiceCollection services, IConfiguration configuration)
    {
        // TODO-02: Register StoreOptions bound to the "Store" section, check its data
        //   annotations, and make the check run when the host starts (not on first use).
        return services;
    }
}
