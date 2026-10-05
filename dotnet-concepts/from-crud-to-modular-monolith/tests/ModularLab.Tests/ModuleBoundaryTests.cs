using System.Reflection;
using Catalog.Contracts;
using Catalog.Core;
using Orders.Contracts;
using Orders.Core;

namespace ModularLab.Tests;

public class ModuleBoundaryTests
{
    private static IEnumerable<string> ReferencedAssemblyNames(Assembly assembly) =>
        assembly.GetReferencedAssemblies().Select(a => a.Name!);

    [Fact]
    public void OrdersCoreShouldNotDependOnCatalogCore()
    {
        var references = ReferencedAssemblyNames(typeof(OrdersModule).Assembly);

        Assert.DoesNotContain("Catalog.Core", references);
    }

    [Fact]
    public void ContractsShouldNotDependOnAnyCoreAssembly()
    {
        var contracts = new[] { typeof(ICatalogModule).Assembly, typeof(OrderDto).Assembly };

        foreach (var assembly in contracts)
            Assert.DoesNotContain(ReferencedAssemblyNames(assembly), name => name.EndsWith(".Core"));
    }

    [Fact]
    public void CatalogCoreShouldExposeOnlyItsModuleClass()
    {
        var exported = typeof(CatalogModule).Assembly.GetExportedTypes();

        Assert.Equal([typeof(CatalogModule)], exported);
    }

    [Fact]
    public void OrdersCoreShouldExposeOnlyItsModuleClass()
    {
        var exported = typeof(OrdersModule).Assembly.GetExportedTypes();

        Assert.Equal([typeof(OrdersModule)], exported);
    }
}
