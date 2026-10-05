using Microsoft.Extensions.DependencyInjection;

namespace DiLab.Tests;

public class LifetimeTests
{
    private static ServiceProvider BuildProvider() =>
        new ServiceCollection()
            .AddGameStoreServices()
            .BuildServiceProvider(new ServiceProviderOptions
            {
                ValidateScopes = true,
                ValidateOnBuild = true,
            });

    [Fact]
    public void ShouldShareAScopedServiceWithinOneScope()
    {
        using var provider = BuildProvider();
        using var scope = provider.CreateScope();

        var first = scope.ServiceProvider.GetRequiredService<RequestState>();
        var second = scope.ServiceProvider.GetRequiredService<RequestState>();

        Assert.Same(first, second);
    }

    [Fact]
    public void ShouldCreateANewScopedInstancePerScope()
    {
        using var provider = BuildProvider();

        using var scopeA = provider.CreateScope();
        using var scopeB = provider.CreateScope();

        Assert.NotSame(
            scopeA.ServiceProvider.GetRequiredService<RequestState>(),
            scopeB.ServiceProvider.GetRequiredService<RequestState>());
    }

    [Fact]
    public void ShouldShareASingletonAcrossScopes()
    {
        using var provider = BuildProvider();

        using var scopeA = provider.CreateScope();
        using var scopeB = provider.CreateScope();

        Assert.Same(
            scopeA.ServiceProvider.GetRequiredService<ISystemClock>(),
            scopeB.ServiceProvider.GetRequiredService<ISystemClock>());
    }

    [Fact]
    public void ShouldCreateANewTransientInstanceEveryTime()
    {
        using var provider = BuildProvider();
        using var scope = provider.CreateScope();

        var first = scope.ServiceProvider.GetRequiredService<ReceiptNumberGenerator>();
        var second = scope.ServiceProvider.GetRequiredService<ReceiptNumberGenerator>();

        Assert.NotSame(first, second);
    }

    [Fact]
    public void ShouldBuildTheProviderWithoutACaptiveDependency()
    {
        var exception = Record.Exception(() => BuildProvider().Dispose());

        Assert.Null(exception);
    }

    [Fact]
    public void ShouldUseAFreshDbContextForEveryCatalogLookup()
    {
        using var provider = BuildProvider();
        var reader = provider.GetRequiredService<CatalogReader>();

        var first = reader.Lookup(1);
        var second = reader.Lookup(1);

        Assert.NotEqual(first.DbId, second.DbId);
    }

    [Fact]
    public void ShouldSeedInsideAScopeAndDisposeTheDbContextAfterwards()
    {
        using var provider = BuildProvider();

        var db = StartupTasks.SeedDatabase(provider);

        Assert.True(db.Seeded);
        Assert.True(db.IsDisposed);
    }
}
