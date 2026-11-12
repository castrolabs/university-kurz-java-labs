using Microsoft.Extensions.DependencyInjection;
using Shop.Api.Mediator;

namespace Shop.Tests;

public class MediatorTests
{
    public sealed record Ping(string Text) : IRequest<string>;
    public sealed record Boom : ICommand<int>;
    public sealed record Read : IQuery<int>;
    public sealed record Save : ICommand<int>;

    public sealed class SaveHandler : IRequestHandler<Save, int>
    {
        public Task<int> Handle(Save r, CancellationToken ct) => Task.FromResult(7);
    }

    public sealed class PingHandler(List<string> trace) : IRequestHandler<Ping, string>
    {
        public Task<string> Handle(Ping r, CancellationToken ct) { trace.Add("handler"); return Task.FromResult(r.Text + "!"); }
    }

    public sealed class BoomHandler : IRequestHandler<Boom, int>
    {
        public Task<int> Handle(Boom r, CancellationToken ct) => throw new InvalidOperationException("boom");
    }

    public sealed class ReadHandler : IRequestHandler<Read, int>
    {
        public Task<int> Handle(Read r, CancellationToken ct) => Task.FromResult(42);
    }

    public sealed class Outer(List<string> t) : IPipelineBehavior<Ping, string>
    {
        public async Task<string> Handle(Ping r, RequestHandlerDelegate<string> next, CancellationToken ct)
        { t.Add("outer>"); var x = await next(); t.Add("<outer"); return x; }
    }

    public sealed class Inner(List<string> t) : IPipelineBehavior<Ping, string>
    {
        public async Task<string> Handle(Ping r, RequestHandlerDelegate<string> next, CancellationToken ct)
        { t.Add("inner>"); var x = await next(); t.Add("<inner"); return x; }
    }

    public sealed class PingValidator : IValidator<Ping>
    {
        public IEnumerable<string> Validate(Ping p)
        {
            if (p.Text.Length == 0) yield return "empty";
            if (p.Text.Length > 3) yield return "too long";
            if (p.Text.Contains(' ')) yield return "has space";
        }
    }

    private static ServiceProvider Build(Action<IServiceCollection> configure)
    {
        var s = new ServiceCollection();
        var trace = new List<string>();
        s.AddSingleton(trace);
        s.AddMediator(typeof(MediatorTests).Assembly);
        s.AddScoped<FakeUnitOfWork>();
        s.AddScoped<IUnitOfWork>(sp => sp.GetRequiredService<FakeUnitOfWork>());
        configure(s);
        return s.BuildServiceProvider();
    }

    [Fact]
    public async Task Send_without_behaviors_calls_the_handler()
    {
        using var sp = Build(_ => { });
        Assert.Equal("hi!", await sp.GetRequiredService<IMediator>().Send(new Ping("hi")));
    }

    [Fact]
    public async Task Send_without_a_handler_fails_with_a_clear_message()
    {
        using var sp = Build(_ => { });
        var ex = await Assert.ThrowsAsync<InvalidOperationException>(
            () => sp.GetRequiredService<IMediator>().Send(new Unhandled()));
        Assert.Contains("Unhandled", ex.Message);
    }

    public sealed record Unhandled : IRequest<int>;

    [Fact]
    public async Task First_registered_behavior_is_the_outermost()
    {
        using var sp = Build(s =>
        {
            s.AddScoped<IPipelineBehavior<Ping, string>, Outer>();
            s.AddScoped<IPipelineBehavior<Ping, string>, Inner>();
        });
        await sp.GetRequiredService<IMediator>().Send(new Ping("a"));
        Assert.Equal(["outer>", "inner>", "handler", "<inner", "<outer"], sp.GetRequiredService<List<string>>());
    }

    [Fact]
    public async Task Validation_collects_every_error_and_skips_the_handler()
    {
        using var sp = Build(s =>
        {
            s.AddSingleton<IValidator<Ping>, PingValidator>();
            s.AddScoped(typeof(IPipelineBehavior<,>), typeof(ValidationBehavior<,>));
        });
        var ex = await Assert.ThrowsAsync<ValidationException>(
            () => sp.GetRequiredService<IMediator>().Send(new Ping("a b c")));
        Assert.Equal(["too long", "has space"], ex.Errors);
        Assert.DoesNotContain("handler", sp.GetRequiredService<List<string>>());
    }

    [Fact]
    public async Task Validation_passes_valid_requests_through()
    {
        using var sp = Build(s =>
        {
            s.AddSingleton<IValidator<Ping>, PingValidator>();
            s.AddScoped(typeof(IPipelineBehavior<,>), typeof(ValidationBehavior<,>));
        });
        Assert.Equal("ok!", await sp.GetRequiredService<IMediator>().Send(new Ping("ok")));
    }

    [Fact]
    public async Task Transaction_commits_when_the_command_succeeds()
    {
        using var sp = Build(s => s.AddScoped(typeof(IPipelineBehavior<,>), typeof(TransactionBehavior<,>)));
        Assert.Equal(7, await sp.GetRequiredService<IMediator>().Send(new Save()));
        Assert.Equal(["begin", "commit"], sp.GetRequiredService<FakeUnitOfWork>().Calls);
    }

    [Fact]
    public async Task Transaction_never_commits_a_failed_command_and_keeps_the_original_exception()
    {
        using var sp = Build(s => s.AddScoped(typeof(IPipelineBehavior<,>), typeof(TransactionBehavior<,>)));
        var ex = await Assert.ThrowsAsync<InvalidOperationException>(
            () => sp.GetRequiredService<IMediator>().Send(new Boom()));
        Assert.Equal("boom", ex.Message);
        Assert.DoesNotContain("commit", sp.GetRequiredService<FakeUnitOfWork>().Calls);
    }

    [Fact]
    public async Task Transaction_does_not_apply_to_queries()
    {
        using var sp = Build(s => s.AddScoped(typeof(IPipelineBehavior<,>), typeof(TransactionBehavior<,>)));
        Assert.Equal(42, await sp.GetRequiredService<IMediator>().Send(new Read()));
        Assert.Empty(sp.GetRequiredService<FakeUnitOfWork>().Calls);
    }
}
