using Microsoft.Extensions.DependencyInjection;

namespace DiLab;

public static class ServiceRegistration
{
    public static IServiceCollection AddGameStoreServices(this IServiceCollection services)
    {
        // TODO-00: RequestState and FakeDb must live exactly as long as one request (one scope).

        // TODO-01: ISystemClock is stateless and shared by everyone: one instance for the app.

        // TODO-02: ReceiptNumberGenerator must hand out a new instance every time it is requested.

        // TODO-03: Register CatalogReader as a singleton, then run the tests. Read the
        //   exception about the scoped service and fix CatalogReader (see Services.cs).

        // TODO-05 (optional): Register StripeGateway and PaypalGateway as keyed singletons
        //   ("stripe" and "paypal"), and CheckoutService as scoped.

        return services;
    }
}
