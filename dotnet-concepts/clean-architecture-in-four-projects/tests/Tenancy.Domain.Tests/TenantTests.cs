namespace Tenancy.Domain.Tests;

public class TenantTests
{
    [Fact]
    public void ShouldGenerateATimeOrderedVersion7Id()
    {
        var tenant = new Tenant("acme");

        Assert.Equal(7, tenant.Id.Version);
    }

    [Fact]
    public void ShouldNormalizeTheSubdomainToLowerCase()
    {
        var tenant = new Tenant("  ACME-Games ");

        Assert.Equal("acme-games", tenant.Subdomain);
    }

    [Theory]
    [InlineData("")]
    [InlineData("   ")]
    [InlineData("-acme")]
    [InlineData("acme-")]
    [InlineData("ac me")]
    [InlineData("a.b")]
    [InlineData("under_score")]
    public void ShouldRejectAnInvalidSubdomain(string subdomain)
    {
        Assert.Throws<ArgumentException>(() => new Tenant(subdomain));
    }

    [Fact]
    public void ShouldRejectASubdomainLongerThan63Characters()
    {
        Assert.Throws<ArgumentException>(() => new Tenant(new string('a', 64)));
        Assert.Equal(63, new Tenant(new string('a', 63)).Subdomain.Length);
    }

    [Fact]
    public void ShouldKeepTheIdWhenRenamed()
    {
        var tenant = new Tenant("acme");
        var id = tenant.Id;

        tenant.Rename("Globex");

        Assert.Equal(id, tenant.Id);
        Assert.Equal("globex", tenant.Subdomain);
    }

    [Fact]
    public void ShouldNotAcceptAnInvalidRename()
    {
        var tenant = new Tenant("acme");

        Assert.Throws<ArgumentException>(() => tenant.Rename("not valid"));
        Assert.Equal("acme", tenant.Subdomain);
    }

    [Fact]
    public void ShouldNotDependOnWebOrPersistenceFrameworks()
    {
        var references = typeof(Tenant).Assembly.GetReferencedAssemblies()
            .Select(a => a.Name!)
            .ToList();

        Assert.DoesNotContain(references, name =>
            name.StartsWith("Microsoft.AspNetCore") ||
            name.StartsWith("Microsoft.EntityFrameworkCore") ||
            name.StartsWith("Npgsql"));
    }
}
