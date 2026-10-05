using System.Net;
using System.Net.Http.Json;
using System.Text.Json;
using Microsoft.AspNetCore.Mvc.Testing;

namespace ValidationLab.Tests;

public class GamesValidationTests(WebApplicationFactory<Program> factory)
    : IClassFixture<WebApplicationFactory<Program>>
{
    private readonly HttpClient _client = factory.CreateClient();

    private static object ValidGame() => new
    {
        name = "Astro Vault",
        genreId = 3,
        price = 59.99,
        releaseDate = "2023-10-20",
    };

    private async Task<int> TotalGamesAsync()
    {
        var body = await _client.GetFromJsonAsync<JsonElement>("/games");
        return body.GetProperty("total").GetInt32();
    }

    private static async Task<JsonElement> ErrorsAsync(HttpResponseMessage response)
    {
        var body = await response.Content.ReadFromJsonAsync<JsonElement>();
        return body.GetProperty("errors");
    }

    [Fact]
    public async Task ShouldReturn201WhenGameIsValid()
    {
        var response = await _client.PostAsJsonAsync("/games", ValidGame());

        Assert.Equal(HttpStatusCode.Created, response.StatusCode);
    }

    [Fact]
    public async Task ShouldReturn400WithNameErrorWhenNameIsMissing()
    {
        var response = await _client.PostAsJsonAsync("/games",
            new { genreId = 3, price = 59.99, releaseDate = "2023-10-20" });

        Assert.Equal(HttpStatusCode.BadRequest, response.StatusCode);
        Assert.True((await ErrorsAsync(response)).TryGetProperty("Name", out _));
    }

    [Fact]
    public async Task ShouldReturn400WhenNameIsLongerThan50Characters()
    {
        var response = await _client.PostAsJsonAsync("/games", new
        {
            name = new string('x', 51),
            genreId = 3,
            price = 59.99,
            releaseDate = "2023-10-20",
        });

        Assert.Equal(HttpStatusCode.BadRequest, response.StatusCode);
        Assert.True((await ErrorsAsync(response)).TryGetProperty("Name", out _));
    }

    [Fact]
    public async Task ShouldReturn400WithPriceErrorWhenPriceIsAbove100()
    {
        var response = await _client.PostAsJsonAsync("/games", new
        {
            name = "Astro Vault",
            genreId = 3,
            price = 559,
            releaseDate = "2023-10-20",
        });

        Assert.Equal(HttpStatusCode.BadRequest, response.StatusCode);
        Assert.True((await ErrorsAsync(response)).TryGetProperty("Price", out _));
    }

    [Fact]
    public async Task ShouldReturn400WhenGenreIdIsOutOfRange()
    {
        var response = await _client.PostAsJsonAsync("/games", new
        {
            name = "Astro Vault",
            genreId = 0,
            price = 59.99,
            releaseDate = "2023-10-20",
        });

        Assert.Equal(HttpStatusCode.BadRequest, response.StatusCode);
        Assert.True((await ErrorsAsync(response)).TryGetProperty("GenreId", out _));
    }

    [Fact]
    public async Task ShouldReportEveryInvalidFieldInOneResponse()
    {
        var response = await _client.PostAsJsonAsync("/games", new
        {
            name = "",
            genreId = 0,
            price = 559,
            releaseDate = "2023-10-20",
        });

        var errors = await ErrorsAsync(response);
        Assert.True(errors.TryGetProperty("Name", out _));
        Assert.True(errors.TryGetProperty("GenreId", out _));
        Assert.True(errors.TryGetProperty("Price", out _));
    }

    [Fact]
    public async Task ShouldNotStoreAnInvalidGame()
    {
        var before = await TotalGamesAsync();

        await _client.PostAsJsonAsync("/games", new { name = "", genreId = 3, price = 59.99, releaseDate = "2023-10-20" });

        Assert.Equal(before, await TotalGamesAsync());
    }

    [Fact]
    public async Task ShouldReturn400WhenPageParameterIsZero()
    {
        var response = await _client.GetAsync("/games?page=0");

        Assert.Equal(HttpStatusCode.BadRequest, response.StatusCode);
    }

    [Fact]
    public async Task ShouldReturn200WhenPageParameterIsValidOrOmitted()
    {
        Assert.Equal(HttpStatusCode.OK, (await _client.GetAsync("/games?page=2")).StatusCode);
        Assert.Equal(HttpStatusCode.OK, (await _client.GetAsync("/games")).StatusCode);
    }
}
