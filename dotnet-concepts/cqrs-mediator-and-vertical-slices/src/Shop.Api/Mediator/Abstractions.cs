namespace Shop.Api.Mediator;

public interface IRequest<TResponse>;

/// <summary>A request that changes state. Behaviors such as the transaction target only these.</summary>
public interface ICommand<TResponse> : IRequest<TResponse>;

/// <summary>A request that only reads.</summary>
public interface IQuery<TResponse> : IRequest<TResponse>;

public interface IRequestHandler<TRequest, TResponse> where TRequest : IRequest<TResponse>
{
    Task<TResponse> Handle(TRequest request, CancellationToken ct);
}

/// <summary>"The rest of the pipeline": the next behavior, or the handler itself.</summary>
public delegate Task<TResponse> RequestHandlerDelegate<TResponse>();

public interface IPipelineBehavior<TRequest, TResponse> where TRequest : IRequest<TResponse>
{
    Task<TResponse> Handle(TRequest request, RequestHandlerDelegate<TResponse> next, CancellationToken ct);
}

public interface IMediator
{
    Task<TResponse> Send<TResponse>(IRequest<TResponse> request, CancellationToken ct = default);
}
