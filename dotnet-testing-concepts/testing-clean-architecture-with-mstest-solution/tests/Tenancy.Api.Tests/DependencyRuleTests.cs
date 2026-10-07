using System.Reflection;

namespace Tenancy.Api.Tests;

// The compiler guards the direction of project references, but not what a public type exposes
// or whether a package slipped in. These tests read the compiled references.
[TestClass]
[TestCategory("Architecture")]
public sealed class DependencyRuleTests
{
    private static readonly Assembly Domain = typeof(Tenancy.Domain.Tenant).Assembly;
    private static readonly Assembly Application = typeof(Tenancy.Application.CreateTenantHandler).Assembly;
    private static readonly Assembly Infrastructure = typeof(Tenancy.Infrastructure.DependencyInjection).Assembly;

    // One row per layer: what it must never reference.
    public static IEnumerable<(Assembly Layer, string[] Forbidden)> Rules =>
    [
        (Domain, ["Tenancy.", "Microsoft.AspNetCore", "Microsoft.EntityFrameworkCore"]),
        (Application, ["Tenancy.Infrastructure", "Tenancy.Api", "Microsoft.AspNetCore", "Microsoft.EntityFrameworkCore"]),
        (Infrastructure, ["Tenancy.Api"]),
    ];

    public static string RuleName(MethodInfo method, object[] data) =>
        $"{((Assembly)data[0]).GetName().Name} must not reference {string.Join(", ", (string[])data[1])}";

    [TestMethod]
    [DynamicData(nameof(Rules), DynamicDataDisplayName = nameof(RuleName))]
    public void ALayerShouldNotReferenceWhatIsOutsideItsRing(Assembly layer, string[] forbidden)
    {
        var references = layer.GetReferencedAssemblies().Select(a => a.Name!).ToList();

        var violations = references.Where(r => forbidden.Any(r.StartsWith)).ToList();

        Assert.IsEmpty(violations, $"{layer.GetName().Name} references {string.Join(", ", violations)}");
    }

    [TestMethod]
    public void ApplicationShouldStillReferenceTheDomain()
    {
        // Guards the guard: if the rules above matched nothing because the layer lost its references,
        // they would pass for the wrong reason.
        var references = Application.GetReferencedAssemblies().Select(a => a.Name!).ToList();

        Assert.Contains("Tenancy.Domain", references);
    }

    [TestMethod]
    public void InfrastructureShouldKeepItsPersistenceTypesInternal()
    {
        var exported = Infrastructure.GetExportedTypes().Select(t => t.Name).ToList();

        Assert.DoesNotContain("TenantRepository", exported);
        Assert.DoesNotContain("TenantConfiguration", exported);
    }
}
