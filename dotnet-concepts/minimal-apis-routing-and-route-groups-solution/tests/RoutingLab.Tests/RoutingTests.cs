using System.Net;
using System.Net.Http.Json;
using System.Text.Json;
using Microsoft.AspNetCore.Builder;
using Microsoft.AspNetCore.Http.Metadata;
using Microsoft.AspNetCore.Mvc.Testing;
using Microsoft.AspNetCore.Routing;
using Microsoft.Extensions.DependencyInjection;

namespace RoutingLab.Tests;

public class RoutingTests : IDisposable
{
    private readonly WebApplicationFactory<Program> _factory = new();
    private readonly HttpClient _client;

    public RoutingTests() => _client = _factory.CreateClient();

    public void Dispose() => _factory.Dispose();

    [Fact]
    public async Task ShouldServeGamesUnderTheApiPrefixOnly()
    {
        Assert.Equal(HttpStatusCode.OK, (await _client.GetAsync("/api/games")).StatusCode);
        Assert.Equal(HttpStatusCode.NotFound, (await _client.GetAsync("/games")).StatusCode);
    }

    [Fact]
    public async Task ShouldBindQueryParametersAndApplyTheDefaultPage()
    {
        var defaults = await _client.GetFromJsonAsync<JsonElement>("/api/games");
        var explicitValues = await _client.GetFromJsonAsync<JsonElement>("/api/games?genre=RPG&page=3");

        Assert.Equal(1, defaults.GetProperty("page").GetInt32());
        Assert.Equal(JsonValueKind.Null, defaults.GetProperty("genre").ValueKind);
        Assert.Equal("RPG", explicitValues.GetProperty("genre").GetString());
        Assert.Equal(3, explicitValues.GetProperty("page").GetInt32());
    }

    [Fact]
    public async Task ShouldBindTheIdFromTheRoute()
    {
        var body = await _client.GetFromJsonAsync<JsonElement>("/api/games/7");

        Assert.Equal(7, body.GetProperty("id").GetInt32());
    }

    [Fact]
    public async Task ShouldReturn404WhenTheIdIsNotAnInteger()
    {
        var response = await _client.GetAsync("/api/games/abc");

        Assert.Equal(HttpStatusCode.NotFound, response.StatusCode);
    }

    [Fact]
    public async Task ShouldPreferTheLiteralSegmentOverTheParameter()
    {
        var body = await _client.GetFromJsonAsync<JsonElement>("/api/games/search?q=fighter");

        Assert.Equal("search", body.GetProperty("kind").GetString());
        Assert.Single(body.GetProperty("results").EnumerateArray());
    }

    [Fact]
    public async Task ShouldInjectARegisteredServiceIntoTheHandler()
    {
        var body = await _client.GetFromJsonAsync<JsonElement>("/api/games/count");

        Assert.Equal(3, body.GetProperty("total").GetInt32());
    }

    [Fact]
    public async Task ShouldPointTheLocationHeaderAtTheNamedRoute()
    {
        var response = await _client.PostAsJsonAsync("/api/games", new { name = "Metroid Dread" });

        Assert.Equal(HttpStatusCode.Created, response.StatusCode);
        var created = await response.Content.ReadFromJsonAsync<JsonElement>();
        var id = created.GetProperty("id").GetInt32();
        Assert.EndsWith($"/api/games/{id}", response.Headers.Location!.ToString());
    }

    [Fact]
    public void ShouldTagEveryGamesEndpointThroughTheGroup()
    {
        var endpoints = _factory.Services.GetRequiredService<EndpointDataSource>()
            .Endpoints.OfType<RouteEndpoint>()
            .Where(e => e.RoutePattern.RawText!.StartsWith("/api/games"))
            .ToList();

        Assert.NotEmpty(endpoints);
        Assert.All(endpoints, e =>
            Assert.Contains("Games", e.Metadata.GetMetadata<ITagsMetadata>()?.Tags ?? []));
    }
}
