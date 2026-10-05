using Microsoft.Extensions.DependencyInjection;

namespace DiLab;

public static class ServiceRegistration
{
    public static IServiceCollection AddGameStoreServices(this IServiceCollection services)
    {
        services.AddScoped<RequestState>();
        services.AddSingleton<ISystemClock, SystemClock>();
        services.AddTransient<ReceiptNumberGenerator>();
        services.AddScoped<FakeDb>();
        services.AddSingleton<CatalogReader>();

        services.AddKeyedSingleton<IPaymentGateway, StripeGateway>("stripe");
        services.AddKeyedSingleton<IPaymentGateway, PaypalGateway>("paypal");
        services.AddScoped<CheckoutService>();

        return services;
    }
}
