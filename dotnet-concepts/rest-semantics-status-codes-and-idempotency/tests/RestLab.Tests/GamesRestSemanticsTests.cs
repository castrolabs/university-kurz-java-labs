using System.Net;
using System.Net.Http.Json;
using Microsoft.AspNetCore.Mvc.Testing;

namespace RestLab.Tests;

public class GamesRestSemanticsTests(WebApplicationFactory<Program> factory)
    : IClassFixture<WebApplicationFactory<Program>>
{
    private readonly HttpClient _client = factory.CreateClient();

    private static object NewGame(string name = "Astro Vault", decimal price = 59.99m) => new
    {
        name,
        genre = "Platformer",
        price,
        releaseDate = "2023-10-20",
    };

    private async Task<Game> CreateAsync(string name = "Astro Vault")
    {
        var response = await _client.PostAsJsonAsync("/games", NewGame(name));
        return (await response.Content.ReadFromJsonAsync<Game>())!;
    }

    [Fact]
    public async Task ShouldReturn404WhenGameDoesNotExist()
    {
        var response = await _client.GetAsync("/games/9999");

        Assert.Equal(HttpStatusCode.NotFound, response.StatusCode);
    }

    [Fact]
    public async Task ShouldReturn200WithTheGameWhenItExists()
    {
        var created = await CreateAsync("Street Fighter II");

        var response = await _client.GetAsync($"/games/{created.Id}");

        Assert.Equal(HttpStatusCode.OK, response.StatusCode);
        var game = await response.Content.ReadFromJsonAsync<Game>();
        Assert.Equal("Street Fighter II", game!.Name);
    }

    [Fact]
    public async Task ShouldReturn201WithALocationHeaderOnCreate()
    {
        var response = await _client.PostAsJsonAsync("/games", NewGame());

        Assert.Equal(HttpStatusCode.Created, response.StatusCode);
        var created = await response.Content.ReadFromJsonAsync<Game>();
        Assert.EndsWith($"/games/{created!.Id}", response.Headers.Location!.ToString());
    }

    [Fact]
    public async Task ShouldLetTheClientFollowTheLocationHeader()
    {
        var response = await _client.PostAsJsonAsync("/games", NewGame("Final Fantasy VII"));

        var followed = await _client.GetAsync(response.Headers.Location);

        Assert.Equal(HttpStatusCode.OK, followed.StatusCode);
    }

    [Fact]
    public async Task ShouldCreateTwoGamesWhenTheSamePostIsSentTwice()
    {
        var first = await _client.PostAsJsonAsync("/games", NewGame("Duplicate"));
        var second = await _client.PostAsJsonAsync("/games", NewGame("Duplicate"));

        var a = await first.Content.ReadFromJsonAsync<Game>();
        var b = await second.Content.ReadFromJsonAsync<Game>();
        Assert.NotEqual(a!.Id, b!.Id);
    }

    [Fact]
    public async Task ShouldReturn404WhenUpdatingAGameThatDoesNotExist()
    {
        var response = await _client.PutAsJsonAsync("/games/9999", NewGame());

        Assert.Equal(HttpStatusCode.NotFound, response.StatusCode);
    }

    [Fact]
    public async Task ShouldReturn204AndReplaceTheGameOnUpdate()
    {
        var created = await CreateAsync();

        var response = await _client.PutAsJsonAsync($"/games/{created.Id}", NewGame("Renamed", 9.99m));

        Assert.Equal(HttpStatusCode.NoContent, response.StatusCode);
        var game = await _client.GetFromJsonAsync<Game>($"/games/{created.Id}");
        Assert.Equal("Renamed", game!.Name);
        Assert.Equal(9.99m, game.Price);
    }

    [Fact]
    public async Task ShouldKeepTheSameStateWhenTheSamePutIsSentTwice()
    {
        var created = await CreateAsync();

        var first = await _client.PutAsJsonAsync($"/games/{created.Id}", NewGame("Same", 10m));
        var second = await _client.PutAsJsonAsync($"/games/{created.Id}", NewGame("Same", 10m));

        Assert.Equal(HttpStatusCode.NoContent, first.StatusCode);
        Assert.Equal(HttpStatusCode.NoContent, second.StatusCode);
        var game = await _client.GetFromJsonAsync<Game>($"/games/{created.Id}");
        Assert.Equal("Same", game!.Name);
    }

    [Fact]
    public async Task ShouldReturn204AndRemoveTheGameOnDelete()
    {
        var created = await CreateAsync();

        var response = await _client.DeleteAsync($"/games/{created.Id}");

        Assert.Equal(HttpStatusCode.NoContent, response.StatusCode);
        Assert.Equal(HttpStatusCode.NotFound, (await _client.GetAsync($"/games/{created.Id}")).StatusCode);
    }

    [Fact]
    public async Task ShouldReturn204EvenWhenTheGameWasAlreadyDeleted()
    {
        var created = await CreateAsync();
        await _client.DeleteAsync($"/games/{created.Id}");

        var again = await _client.DeleteAsync($"/games/{created.Id}");
        var unknown = await _client.DeleteAsync("/games/9999");

        Assert.Equal(HttpStatusCode.NoContent, again.StatusCode);
        Assert.Equal(HttpStatusCode.NoContent, unknown.StatusCode);
    }
}
