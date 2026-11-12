using System.Net;
using System.Text.RegularExpressions;
using Microsoft.AspNetCore.Components.Endpoints;
using Microsoft.AspNetCore.Mvc.Testing;
using Microsoft.Extensions.DependencyInjection;

namespace BlazorSsrStateLab.Tests;

public class StateTests(WebApplicationFactory<Program> factory)
    : IClassFixture<WebApplicationFactory<Program>>
{
    // Each client keeps its own cookies, so each client is one "browser".
    private HttpClient NewClient(WebApplicationFactory<Program>? f = null) =>
        (f ?? factory).CreateClient(new WebApplicationFactoryClientOptions { AllowAutoRedirect = false });

    private static FormUrlEncodedContent Form(string handler, string field, string value) =>
        new(new Dictionary<string, string> { ["_handler"] = handler, [field] = value });

    private static string Tag(string html, string id) =>
        Regex.Match(html, $"<p id=\"{id}\">(.*?)</p>").Groups[1].Value;

    // TODO-00
    [Fact]
    public async Task ShouldShowFlashMessageOnceAfterRedirect()
    {
        var client = NewClient();

        var post = await client.PostAsync("/flash", Form("flash", "Model.Text", "hello"));
        Assert.Equal(HttpStatusCode.Redirect, post.StatusCode);
        Assert.Equal("/flash", post.Headers.Location?.AbsolutePath);
        Assert.Contains(post.Headers.GetValues("Set-Cookie"), c => c.StartsWith(".AspNetCore.Components.TempData="));

        Assert.Equal("saved hello", Tag(await client.GetStringAsync("/flash"), "message"));
        Assert.Equal("", Tag(await client.GetStringAsync("/flash"), "message"));
    }

    // TODO-01
    [Fact]
    public async Task ShouldKeepTempDataPerBrowser()
    {
        var a = NewClient();
        var b = NewClient();
        await a.PostAsync("/flash", Form("flash", "Model.Text", "only-a"));

        Assert.Equal("", Tag(await b.GetStringAsync("/flash"), "message"));
        Assert.Equal("saved only-a", Tag(await a.GetStringAsync("/flash"), "message"));
    }

    // TODO-02
    [Fact]
    public async Task ShouldAccumulateCartInSession()
    {
        var client = NewClient();
        Assert.Equal("empty", Tag(await client.GetStringAsync("/cart"), "items"));

        await client.PostAsync("/cart", Form("cart", "Model.Item", "apple"));
        await client.PostAsync("/cart", Form("cart", "Model.Item", "pear"));

        Assert.Equal("apple,pear", Tag(await client.GetStringAsync("/cart"), "items"));
        Assert.Equal("apple,pear", Tag(await client.GetStringAsync("/cart"), "items"));
        Assert.Equal("empty", Tag(await NewClient().GetStringAsync("/cart"), "items"));
    }

    // TODO-03
    [Fact]
    public async Task ShouldCacheNewsPerCategory()
    {
        var client = NewClient();
        var all1 = Tag(await client.GetStringAsync("/news"), "stamp");
        var all2 = Tag(await client.GetStringAsync("/news"), "stamp");
        var a1 = Tag(await client.GetStringAsync("/news?category=a"), "stamp");
        var a2 = Tag(await client.GetStringAsync("/news?category=a"), "stamp");
        var b = Tag(await client.GetStringAsync("/news?category=b"), "stamp");

        Assert.Equal(all1, all2);
        Assert.Equal(a1, a2);
        Assert.NotEqual(all1, a1);
        Assert.NotEqual(a1, b);
    }

    // TODO-04
    [Fact]
    public async Task ShouldRenderBasePathInsteadOfHandWrittenBase()
    {
        var html = await NewClient().GetStringAsync("/news");

        Assert.Contains("<base href=\"/\"", html);
        Assert.DoesNotContain("<BasePath", html);
    }

    [Fact]
    [Trait("Category", "Bonus")]
    public async Task ShouldStoreTempDataInSessionWhenConfigured()
    {
        var f = factory.WithWebHostBuilder(b => b.ConfigureServices(s =>
            s.Configure<RazorComponentsServiceOptions>(o => o.TempDataProviderType = TempDataProviderType.SessionStorage)));
        var client = NewClient(f);

        var post = await client.PostAsync("/flash", Form("flash", "Model.Text", "hi"));

        Assert.DoesNotContain(post.Headers.GetValues("Set-Cookie"), c => c.StartsWith(".AspNetCore.Components.TempData="));
        Assert.Equal("saved hi", Tag(await client.GetStringAsync("/flash"), "message"));
    }
}
