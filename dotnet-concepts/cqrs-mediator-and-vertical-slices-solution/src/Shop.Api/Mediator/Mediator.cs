using System.Collections.Concurrent;

namespace Shop.Api.Mediator;

public sealed class Mediator(IServiceProvider services) : IMediator
{
    // One wrapper per concrete request type, created once. The cache is why the reflection below is cheap.
    private static readonly ConcurrentDictionary<Type, object> Wrappers = new();

    public Task<TResponse> Send<TResponse>(IRequest<TResponse> request, CancellationToken ct = default)
    {
        ArgumentNullException.ThrowIfNull(request);

        var wrapper = (RequestWrapper<TResponse>)Wrappers.GetOrAdd(request.GetType(), static requestType =>
            Activator.CreateInstance(typeof(RequestWrapper<,>).MakeGenericType(requestType, typeof(TResponse)))!);

        return wrapper.Handle(request, services, ct);
    }
}

internal abstract class RequestWrapper<TResponse>
{
    public abstract Task<TResponse> Handle(IRequest<TResponse> request, IServiceProvider services, CancellationToken ct);
}

internal sealed class RequestWrapper<TRequest, TResponse> : RequestWrapper<TResponse>
    where TRequest : IRequest<TResponse>
{
    public override Task<TResponse> Handle(IRequest<TResponse> request, IServiceProvider services, CancellationToken ct)
    {
        var handler = services.GetService<IRequestHandler<TRequest, TResponse>>()
            ?? throw new InvalidOperationException($"No handler registered for {typeof(TRequest).Name}.");
        var typed = (TRequest)request;

        RequestHandlerDelegate<TResponse> pipeline = () => handler.Handle(typed, ct);

        // TODO-00: wrap the handler with every registered IPipelineBehavior<TRequest, TResponse>.
        // The behavior registered FIRST must be the OUTERMOST one, so fold the list from the end.
        var behaviors = services.GetServices<IPipelineBehavior<TRequest, TResponse>>().ToArray();
        for (var i = behaviors.Length - 1; i >= 0; i--)
        {
            var behavior = behaviors[i];
            var next = pipeline;
            pipeline = () => behavior.Handle(typed, next, ct);
        }

        return pipeline();
    }
}
