using System.Reflection;

namespace Shop.Api.Mediator;

public static class MediatorRegistration
{
    /// <summary>Registers IMediator and every IRequestHandler found in the assembly. Behaviors are NOT scanned: their order matters, so Program.cs lists them.</summary>
    public static IServiceCollection AddMediator(this IServiceCollection services, Assembly assembly)
    {
        services.AddScoped<IMediator, Mediator>();

        foreach (var type in assembly.GetTypes().Where(t => t is { IsClass: true, IsAbstract: false }))
            foreach (var iface in type.GetInterfaces().Where(i =>
                         i.IsGenericType && i.GetGenericTypeDefinition() == typeof(IRequestHandler<,>)))
                services.AddScoped(iface, type);

        return services;
    }
}
