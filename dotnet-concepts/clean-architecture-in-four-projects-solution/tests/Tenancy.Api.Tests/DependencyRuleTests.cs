using System.Reflection;

namespace Tenancy.Api.Tests;

public class DependencyRuleTests
{
    private static readonly Assembly Domain = typeof(Tenancy.Domain.Tenant).Assembly;
    private static readonly Assembly Application = typeof(Tenancy.Application.CreateTenantHandler).Assembly;
    private static readonly Assembly Infrastructure = typeof(Tenancy.Infrastructure.DependencyInjection).Assembly;

    private static IReadOnlyList<string> References(Assembly assembly) =>
        [.. assembly.GetReferencedAssemblies().Select(a => a.Name!)];

    [Fact]
    public void DomainShouldNotDependOnAnyOtherLayerOrFramework()
    {
        Assert.DoesNotContain(References(Domain), name =>
            name.StartsWith("Tenancy.") ||
            name.StartsWith("Microsoft.AspNetCore") ||
            name.StartsWith("Microsoft.EntityFrameworkCore"));
    }

    [Fact]
    public void ApplicationShouldDependOnlyOnTheDomain()
    {
        var references = References(Application);

        Assert.Contains("Tenancy.Domain", references);
        Assert.DoesNotContain("Tenancy.Infrastructure", references);
        Assert.DoesNotContain("Tenancy.Api", references);
        Assert.DoesNotContain(references, name =>
            name.StartsWith("Microsoft.AspNetCore") || name.StartsWith("Microsoft.EntityFrameworkCore"));
    }

    [Fact]
    public void InfrastructureShouldNotDependOnTheApi()
    {
        Assert.DoesNotContain("Tenancy.Api", References(Infrastructure));
    }

    [Fact]
    public void InfrastructureShouldKeepItsPersistenceTypesInternal()
    {
        var exported = Infrastructure.GetExportedTypes().Select(t => t.Name).ToList();

        Assert.DoesNotContain("TenantRepository", exported);
        Assert.DoesNotContain("TenantConfiguration", exported);
    }
}
