using MassTransit;
using Microsoft.Extensions.DependencyInjection;

namespace Brokers;

public sealed class BillingConsumerDefinition : ConsumerDefinition<BillingConsumer>
{
    protected override void ConfigureConsumer(IReceiveEndpointConfigurator endpoint,
        IConsumerConfigurator<BillingConsumer> consumer, IRegistrationContext context)
    {
        endpoint.UseMessageRetry(r => r.Interval(3, TimeSpan.FromMilliseconds(10)));
        endpoint.UseInMemoryOutbox(context);
    }
}

public sealed class ShippingConsumerDefinition : ConsumerDefinition<ShippingConsumer>
{
    public ShippingConsumerDefinition() => EndpointName = Topology.ShippingQueue;

    protected override void ConfigureConsumer(IReceiveEndpointConfigurator endpoint,
        IConsumerConfigurator<ShippingConsumer> consumer, IRegistrationContext context)
        => endpoint.UseMessageRetry(r => r.Interval(2, TimeSpan.FromMilliseconds(10)));
}

public static class Setup
{
    // Shared by production (UsingInMemory below) and by the test harness, which swaps the transport itself.
    public static void AddOrderConsumers(this IBusRegistrationConfigurator x)
    {
        x.AddConsumer<BillingConsumer, BillingConsumerDefinition>();
        x.AddConsumer<ShippingConsumer, ShippingConsumerDefinition>();
        x.AddConsumer<AuditConsumer>();
        x.AddConsumer<BillingFaultConsumer>();
    }

    public static IServiceCollection AddBrokers(this IServiceCollection services)
    {
        services.AddSingleton<Ledger>();
        services.AddScoped<OrderApi>();
        services.AddMassTransit(x =>
        {
            x.AddOrderConsumers();
            x.UsingInMemory((ctx, cfg) => cfg.ConfigureEndpoints(ctx));
            // RabbitMQ or Azure Service Bus: replace the line above with UsingRabbitMq(...) or UsingAzureServiceBus(...).
        });
        return services;
    }
}
