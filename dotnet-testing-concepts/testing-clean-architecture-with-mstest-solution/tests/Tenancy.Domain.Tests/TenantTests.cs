namespace Tenancy.Domain.Tests;

// The domain needs no setup, no fakes and no framework: construct, act, assert.
[TestClass]
public sealed class TenantTests
{
    [TestMethod]
    public void ShouldGenerateATimeOrderedVersion7Id()
    {
        var tenant = new Tenant("acme");

        Assert.AreEqual(7, tenant.Id.Version);
    }

    [TestMethod]
    [DataRow("acme", "acme")]
    [DataRow("  ACME-Games ", "acme-games")]
    [DataRow("A", "a")]
    public void ShouldNormalizeTheSubdomain(string input, string expected)
    {
        var tenant = new Tenant(input);

        Assert.AreEqual(expected, tenant.Subdomain);
    }

    [TestMethod]
    [DataRow("")]
    [DataRow("   ")]
    [DataRow("-acme")]
    [DataRow("acme-")]
    [DataRow("ac me")]
    [DataRow("a.b")]
    [DataRow("under_score")]
    public void ShouldRejectAnInvalidSubdomain(string subdomain)
    {
        Assert.ThrowsExactly<ArgumentException>(() => new Tenant(subdomain));
    }

    [TestMethod]
    public void ShouldAcceptExactly63CharactersAndRejectMore()
    {
        Assert.AreEqual(63, new Tenant(new string('a', 63)).Subdomain.Length);
        Assert.ThrowsExactly<ArgumentException>(() => new Tenant(new string('a', 64)));
    }

    [TestMethod]
    public void ShouldKeepTheIdWhenRenamed()
    {
        var tenant = new Tenant("acme");
        var id = tenant.Id;

        tenant.Rename("Globex");

        Assert.AreEqual(id, tenant.Id);
        Assert.AreEqual("globex", tenant.Subdomain);
    }

    [TestMethod]
    public void ShouldKeepTheOldSubdomainWhenTheRenameIsInvalid()
    {
        var tenant = new Tenant("acme");

        Assert.ThrowsExactly<ArgumentException>(() => tenant.Rename("not valid"));

        Assert.AreEqual("acme", tenant.Subdomain);
    }
}
