using Microsoft.AspNetCore.Mvc.Testing;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Playwright;
using Tasks.Web;

namespace Tasks.E2E.Tests;

// One Kestrel host and one browser process for the whole test run. A browser context per test is cheap;
// a browser per test is not.
[TestClass]
public static class TestEnvironment
{
    private static WebApplicationFactory<Program>? _factory;
    private static IPlaywright? _playwright;

    public static string BaseUrl { get; private set; } = "";
    public static IBrowser Browser { get; private set; } = null!;
    public static TaskStore Store => _factory!.Services.GetRequiredService<TaskStore>();

    [AssemblyInitialize]
    public static async Task Start(TestContext _)
    {
        // UseKestrel(0) + StartServer(): a real socket on a free port, so a browser can reach it.
        // The default in-memory TestServer has no address a browser could open.
        _factory = new WebApplicationFactory<Program>();
        _factory.UseKestrel(0);
        _factory.StartServer();
        BaseUrl = _factory.CreateClient().BaseAddress!.ToString();

        _playwright = await Playwright.CreateAsync();
        Browser = await _playwright.Chromium.LaunchAsync(new() { Headless = true });
    }

    [AssemblyCleanup]
    public static async Task Stop()
    {
        await Browser.CloseAsync();
        _playwright?.Dispose();
        if (_factory is not null) await _factory.DisposeAsync();
    }
}
