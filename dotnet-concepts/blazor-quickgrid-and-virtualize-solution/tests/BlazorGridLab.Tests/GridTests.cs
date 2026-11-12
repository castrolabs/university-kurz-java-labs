using System.Text.RegularExpressions;
using BlazorGridLab.Components.Pages;
using Microsoft.AspNetCore.Mvc.Testing;
using Microsoft.Extensions.DependencyInjection;

namespace BlazorGridLab.Tests;

public class GridTests(WebApplicationFactory<Program> factory)
    : IClassFixture<WebApplicationFactory<Program>>
{
    private static string[] Names(string html) =>
        Regex.Matches(html, "<td[^>]*>(Name\\d+)</td>").Select(m => m.Groups[1].Value).ToArray();

    // TODO-00 (static SSR: the page and sort state live in the URL, no interactivity)
    [Fact]
    public async Task ShouldRenderFirstPageSortedByNameByDefault()
    {
        var html = await factory.CreateClient().GetStringAsync("/products");

        Assert.Equal(["Name01", "Name02", "Name03", "Name04", "Name05"], Names(html));
        Assert.Contains("of <strong", html);
        Assert.Matches("of\\s*<strong[^>]*>5</strong>", html);
    }

    // TODO-01
    [Fact]
    public async Task ShouldReadPageAndSortFromQueryString()
    {
        var html = await factory.CreateClient().GetStringAsync("/products?page=2&sort=Name&direction=desc");

        Assert.Equal(["Name18", "Name17", "Name16", "Name15", "Name14"], Names(html));
    }

    // TODO-02
    [Fact]
    public async Task ShouldRenderHeadersAndPagerAsLinks()
    {
        var html = await factory.CreateClient().GetStringAsync("/products?page=1&sort=Name&direction=desc");

        Assert.Contains("href=\"http://localhost/products?page=1&amp;sort=Name&amp;direction=asc\"", html);
        Assert.Contains("href=\"http://localhost/products?page=1&amp;sort=Price&amp;direction=asc\"", html);
        Assert.Contains("href=\"http://localhost/products?page=2&amp;sort=Name&amp;direction=desc\"", html);
        Assert.Contains("aria-sort=\"descending\"", html);
    }

    // TODO-03
    [Fact]
    public async Task ShouldAskTheItemsProviderOnlyForTheCurrentSlice()
    {
        var service = factory.Services.GetRequiredService<ProductService>();
        service.Calls.Clear();

        var html = await factory.CreateClient().GetStringAsync("/remote?page=3&sort=Price&direction=desc");

        Assert.Equal(["Name13", "Name12", "Name11", "Name10", "Name09"], Names(html));
        Assert.Contains((10, 5, "Price Descending"), service.Calls);
        Assert.All(service.Calls, c => Assert.Equal(5, c.Count));
    }

    // TODO-04
    [Fact]
    public void ShouldRenderOnlyAWindowOfAHugeListWithVirtualize()
    {
        using var ctx = new BunitContext();
        ctx.JSInterop.Mode = JSRuntimeMode.Loose;
        Feed.Requests.Clear();

        var cut = ctx.Render<Feed>();

        var rows = cut.FindAll(".row");
        Assert.InRange(rows.Count, 1, 500); // far below the 10,000 items
        Assert.Equal("Item 0", rows[0].TextContent);
        Assert.All(Feed.Requests, r => Assert.InRange(r.Count, 0, 500));
    }

    // TODO-05
    [Fact]
    public void ShouldRenderTheGridInBunitAfterTheDataArrives()
    {
        using var ctx = new BunitContext();
        ctx.JSInterop.Mode = JSRuntimeMode.Loose;

        var cut = ctx.Render<Products>();

        cut.WaitForAssertion(() => Assert.Equal(5, cut.FindAll("tbody tr").Count));
        Assert.Equal("Name01", cut.Find("tbody tr td").TextContent);
        Assert.Equal(2, cut.FindAll("th").Count);
    }
}
