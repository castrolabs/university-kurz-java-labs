using Microsoft.Playwright;

namespace Tasks.E2E.Tests;

// Base class: a fresh, isolated browser context (cookies, storage) per test, an empty task list, and a
// Playwright trace that is kept only when the test fails.
public abstract class E2ETest
{
    public TestContext TestContext { get; set; } = null!;

    protected IBrowserContext Context { get; private set; } = null!;
    protected IPage Page { get; private set; } = null!;

    [TestInitialize]
    public async Task OpenBrowserContext()
    {
        TestEnvironment.Store.Clear();
        Context = await TestEnvironment.Browser.NewContextAsync(new() { BaseURL = TestEnvironment.BaseUrl });
        await Context.Tracing.StartAsync(new() { Screenshots = true, Snapshots = true });
        Page = await Context.NewPageAsync();
    }

    [TestCleanup]
    public async Task CloseBrowserContext()
    {
        var failed = TestContext.CurrentTestOutcome != UnitTestOutcome.Passed;
        await Context.Tracing.StopAsync(failed
            ? new() { Path = Path.Combine(TestContext.TestResultsDirectory!, $"{TestContext.TestName}.zip") }
            : null);
        await Context.CloseAsync();
    }
}
