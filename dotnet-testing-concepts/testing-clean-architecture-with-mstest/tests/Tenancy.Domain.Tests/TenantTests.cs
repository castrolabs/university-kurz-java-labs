namespace Tenancy.Domain.Tests;

// TODO-00: The domain needs no setup, no fakes and no framework: construct, act, assert.
// Read src/Tenancy.Domain/Tenant.cs first, then write the tests below.
[TestClass]
public sealed class TenantTests
{
    [TestMethod]
    public void ShouldGenerateATimeOrderedVersion7Id()
    {
        // Guid has a Version property.
        Assert.Fail("TODO-00: write this test.");
    }

    [TestMethod]
    public void ShouldNormalizeTheSubdomain()
    {
        // Several inputs, one expectation each: give the method two string parameters and add [DataRow] rows.
        // Include surrounding spaces and upper case.
        Assert.Fail("TODO-00: write this test.");
    }

    [TestMethod]
    public void ShouldRejectAnInvalidSubdomain()
    {
        // One test, many bad inputs: empty, blank, leading hyphen, trailing hyphen, a space, a dot, an underscore.
        // Assert.ThrowsExactly<T> wants exactly T. Which type does the constructor throw?
        Assert.Fail("TODO-00: write this test.");
    }

    [TestMethod]
    public void ShouldAcceptExactly63CharactersAndRejectMore()
    {
        // Test both sides of the boundary in the same test.
        Assert.Fail("TODO-00: write this test.");
    }

    [TestMethod]
    public void ShouldKeepTheIdWhenRenamed()
    {
        Assert.Fail("TODO-00: write this test.");
    }

    [TestMethod]
    public void ShouldKeepTheOldSubdomainWhenTheRenameIsInvalid()
    {
        // A failed operation must not leave the object half changed.
        Assert.Fail("TODO-00: write this test.");
    }
}
