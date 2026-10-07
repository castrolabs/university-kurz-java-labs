using System.Reflection;

namespace Tenancy.Api.Tests;

// The compiler guards the direction of project references, but not which packages a layer uses or what
// a public type exposes. These tests read the compiled references.
[TestClass]
[TestCategory("Architecture")]
public sealed class DependencyRuleTests
{
    private static readonly Assembly Domain = typeof(Tenancy.Domain.Tenant).Assembly;
    private static readonly Assembly Application = typeof(Tenancy.Application.CreateTenantHandler).Assembly;
    private static readonly Assembly Infrastructure = typeof(Tenancy.Infrastructure.DependencyInjection).Assembly;

    // TODO-06: One table, one test. Make a public static IEnumerable<(Assembly Layer, string[] Forbidden)>
    // with a row per layer: what it must never reference (Domain: other Tenancy projects, ASP.NET Core,
    // EF Core. Application: Infrastructure, Api, ASP.NET Core, EF Core. Infrastructure: Api).
    // Point [DynamicData] at it, and give the test a readable name with DynamicDataDisplayName.
    [TestMethod]
    public void ALayerShouldNotReferenceWhatIsOutsideItsRing()
    {
        // Assembly.GetReferencedAssemblies() lists the assemblies the layer actually uses.
        Assert.Fail("TODO-06: write this test.");
    }

    [TestMethod]
    public void ApplicationShouldStillReferenceTheDomain()
    {
        // Guards the guard: if the rules matched nothing because the layer lost its references,
        // they would pass for the wrong reason.
        Assert.Fail("TODO-06: write this test.");
    }

    [TestMethod]
    public void InfrastructureShouldKeepItsPersistenceTypesInternal()
    {
        // GetExportedTypes() lists the public ones. Neither TenantRepository nor TenantConfiguration may be there.
        Assert.Fail("TODO-06: write this test.");
    }
}
