namespace Shop.Api.Mediator;

public interface IValidator<in T>
{
    IEnumerable<string> Validate(T instance);
}

public sealed class ValidationException(IReadOnlyList<string> errors) : Exception("Validation failed.")
{
    public IReadOnlyList<string> Errors { get; } = errors;
}

public interface IUnitOfWork
{
    Task BeginAsync(CancellationToken ct);
    Task CommitAsync(CancellationToken ct);
    Task RollbackAsync(CancellationToken ct);
}

/// <summary>Stand-in for a DbContext transaction. It only records what happened.</summary>
public sealed class FakeUnitOfWork : IUnitOfWork
{
    public List<string> Calls { get; } = [];
    public Task BeginAsync(CancellationToken ct) { Calls.Add("begin"); return Task.CompletedTask; }
    public Task CommitAsync(CancellationToken ct) { Calls.Add("commit"); return Task.CompletedTask; }
    public Task RollbackAsync(CancellationToken ct) { Calls.Add("rollback"); return Task.CompletedTask; }
}

public sealed class LoggingBehavior<TRequest, TResponse>(ILogger<LoggingBehavior<TRequest, TResponse>> logger)
    : IPipelineBehavior<TRequest, TResponse> where TRequest : IRequest<TResponse>
{
    public async Task<TResponse> Handle(TRequest request, RequestHandlerDelegate<TResponse> next, CancellationToken ct)
    {
        logger.LogInformation("Handling {Request}", typeof(TRequest).Name);
        var response = await next();
        logger.LogInformation("Handled {Request}", typeof(TRequest).Name);
        return response;
    }
}

public sealed class ValidationBehavior<TRequest, TResponse>(IEnumerable<IValidator<TRequest>> validators)
    : IPipelineBehavior<TRequest, TResponse> where TRequest : IRequest<TResponse>
{
    public Task<TResponse> Handle(TRequest request, RequestHandlerDelegate<TResponse> next, CancellationToken ct)
    {
        // TODO-01: run every validator, collect ALL errors, and throw ValidationException if there is any.
        // Otherwise call next(). The handler must not run when validation fails.
        var errors = validators.SelectMany(v => v.Validate(request)).ToList();
        if (errors.Count > 0) throw new ValidationException(errors);
        return next();
    }
}

/// <summary>The constraint makes it a behavior for commands only: the container skips it for queries.</summary>
public sealed class TransactionBehavior<TRequest, TResponse>(IUnitOfWork uow)
    : IPipelineBehavior<TRequest, TResponse> where TRequest : ICommand<TResponse>
{
    public async Task<TResponse> Handle(TRequest request, RequestHandlerDelegate<TResponse> next, CancellationToken ct)
    {
        // TODO-02: BeginAsync, call next(), CommitAsync and return the response.
        // If next() throws, RollbackAsync (never commit) and rethrow the same exception.
        await uow.BeginAsync(ct);
        try
        {
            var response = await next();
            await uow.CommitAsync(ct);
            return response;
        }
        catch
        {
            await uow.RollbackAsync(ct);
            throw;
        }
    }
}
