using System.Net;
using System.Net.Http.Json;
using Microsoft.AspNetCore.Mvc.Testing;
using Microsoft.Extensions.DependencyInjection;

namespace QueryLab.Tests;

public class QueryMethodTests(WebApplicationFactory<Program> factory)
    : IClassFixture<WebApplicationFactory<Program>>
{
    private HttpRequestMessage Query(object body) =>
        new(HttpMethod.Query, "/products") { Content = JsonContent.Create(body) };

    [Fact]
    public async Task ShouldFilterProductsWithTheBodyOfAQueryRequest()
    {
        var client = factory.CreateClient();

        var response = await client.SendAsync(Query(new { category = "Books" }));

        Assert.Equal(HttpStatusCode.OK, response.StatusCode);
        var products = await response.Content.ReadFromJsonAsync<List<Product>>();
        Assert.Equal(2, products!.Count);
        Assert.All(products, p => Assert.Equal("Books", p.Category));
    }

    [Fact]
    public async Task ShouldReturnTheSameAnswerWhenTheQueryIsRepeated()
    {
        var client = factory.CreateClient();

        var first = await client.SendAsync(Query(new { maxPrice = 50 }));
        var second = await client.SendAsync(Query(new { maxPrice = 50 }));

        Assert.Equal(HttpStatusCode.OK, first.StatusCode);
        Assert.Equal(await first.Content.ReadAsStringAsync(), await second.Content.ReadAsStringAsync());
        var all = await client.GetFromJsonAsync<List<Product>>("/products");
        Assert.Equal(5, all!.Count);
    }

    [Fact]
    public async Task ShouldReject415WhenTheContentTypeIsNotJson()
    {
        var client = factory.CreateClient();
        var request = new HttpRequestMessage(HttpMethod.Query, "/products")
        {
            Content = new StringContent("category=Books", System.Text.Encoding.UTF8, "text/plain"),
        };

        var response = await client.SendAsync(request);

        Assert.Equal(HttpStatusCode.UnsupportedMediaType, response.StatusCode);
    }

    [Fact]
    public async Task ShouldReject400WhenTheQueryHasNoBody()
    {
        var client = factory.CreateClient();

        var response = await client.SendAsync(new HttpRequestMessage(HttpMethod.Query, "/products"));

        Assert.Equal(HttpStatusCode.BadRequest, response.StatusCode);
    }

    [Fact]
    public async Task ShouldAdvertiseAcceptQueryOnGetAndOnQuery()
    {
        var client = factory.CreateClient();

        var get = await client.GetAsync("/products");
        var query = await client.SendAsync(Query(new { }));

        Assert.Contains("application/json", get.Headers.GetValues("Accept-Query"));
        Assert.Contains("application/json", query.Headers.GetValues("Accept-Query"));
    }

    [Fact]
    public async Task ShouldAnswerThePreflightForQueryFromTheAllowedOrigin()
    {
        var client = factory.CreateClient();
        var preflight = new HttpRequestMessage(HttpMethod.Options, "/products");
        preflight.Headers.Add("Origin", "https://app.test");
        preflight.Headers.Add("Access-Control-Request-Method", "QUERY");
        preflight.Headers.Add("Access-Control-Request-Headers", "content-type");

        var response = await client.SendAsync(preflight);

        Assert.Equal(HttpStatusCode.NoContent, response.StatusCode);
        Assert.Contains("QUERY", string.Join(",", response.Headers.GetValues("Access-Control-Allow-Methods")));
        Assert.Equal("https://app.test", response.Headers.GetValues("Access-Control-Allow-Origin").Single());
    }

    [Fact]
    public async Task ShouldSearchThroughTheTypedClient()
    {
        var products = new ProductsClient(factory.CreateClient());

        var result = await products.SearchAsync(new ProductFilter("Hardware", 100m));

        Assert.Equal(["USB-C Hub"], result.Select(p => p.Name));
    }

    [Fact]
    public async Task ShouldServeTheSameBodyFromCacheAndAnotherBodyFromTheHandler()
    {
        using var isolated = factory.WithWebHostBuilder(_ => { });
        var client = isolated.CreateClient();
        var catalog = isolated.Services.GetRequiredService<Catalog>();

        await client.SendAsync(Query(new { category = "Home" }));
        await client.SendAsync(Query(new { category = "Home" }));
        Assert.Equal(1, catalog.Searches);

        await client.SendAsync(Query(new { category = "Books" }));
        Assert.Equal(2, catalog.Searches);
    }
}
