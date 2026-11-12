namespace Dispatch.Tests;

// These tests check the setup of the suite, not the production code. Each one is green when the test
// project has loaded the MTP extension that its message names.
[TestClass]
[TestCategory("Runner")]
public sealed class RunnerTests
{
    private static void RequireExtension(string assemblyName, string todo)
    {
        var loaded = AppDomain.CurrentDomain.GetAssemblies().Any(a => a.GetName().Name == assemblyName);
        Assert.IsTrue(loaded, $"{todo}: {assemblyName} is not part of this test project. See the README.");
    }

    [TestMethod]
    public void RetryIsAvailable() => RequireExtension("Microsoft.Testing.Extensions.Retry", "TODO-00");

    [TestMethod]
    public void HangDumpIsAvailable() => RequireExtension("Microsoft.Testing.Extensions.HangDump", "TODO-00");

    [TestMethod]
    public void CrashDumpIsAvailable() => RequireExtension("Microsoft.Testing.Extensions.CrashDump", "TODO-00");

    [TestMethod]
    public void TrxReportIsAvailable() => RequireExtension("Microsoft.Testing.Extensions.TrxReport", "TODO-00");
}
