using System.Text.Json;

namespace DtoLab.Tests;

public class MappingTests
{
    private static readonly DateOnly Release = new(2023, 10, 20);

    private static Game StoredGame(Genre? genre = null) => new()
    {
        Id = 7,
        Name = "Astro Vault",
        GenreId = 3,
        Genre = genre,
        Price = 59.99m,
        ReleaseDate = Release,
    };

    [Fact]
    public void ShouldMapACreateDtoToAnEntityWithoutAnId()
    {
        var game = new CreateGameDto("Astro Vault", 3, 59.99m, Release).ToEntity();

        Assert.Equal(0, game.Id);
        Assert.Equal("Astro Vault", game.Name);
        Assert.Equal(3, game.GenreId);
        Assert.Equal(59.99m, game.Price);
        Assert.Equal(Release, game.ReleaseDate);
    }

    [Fact]
    public void ShouldMapAnEntityToDetailsKeepingTheGenreId()
    {
        var details = StoredGame().ToDetailsDto();

        Assert.Equal(new GameDetailsDto(7, "Astro Vault", 3, 59.99m, Release), details);
    }

    [Fact]
    public void ShouldMapAnEntityToASummaryWithTheGenreName()
    {
        var summary = StoredGame(new Genre { Id = 3, Name = "Platformer" }).ToSummaryDto();

        Assert.Equal(new GameSummaryDto(7, "Astro Vault", "Platformer", 59.99m, Release), summary);
    }

    [Fact]
    public void ShouldExplainWhenTheGenreWasNotLoaded()
    {
        var exception = Assert.Throws<InvalidOperationException>(() => StoredGame().ToSummaryDto());

        Assert.Contains("Genre", exception.Message);
    }

    [Fact]
    public void ShouldApplyAnUpdateDtoWithoutChangingTheId()
    {
        var game = StoredGame();

        new UpdateGameDto("Renamed", 1, 9.99m, new DateOnly(2024, 1, 2)).ApplyTo(game);

        Assert.Equal(7, game.Id);
        Assert.Equal("Renamed", game.Name);
        Assert.Equal(1, game.GenreId);
        Assert.Equal(9.99m, game.Price);
        Assert.Equal(new DateOnly(2024, 1, 2), game.ReleaseDate);
    }

    [Fact]
    public void ShouldRoundTripEveryFieldFromCreateToDetails()
    {
        var dto = new CreateGameDto("Astro Vault", 3, 59.99m, Release);

        var details = dto.ToEntity().ToDetailsDto();

        Assert.Equal(new GameDetailsDto(0, dto.Name, dto.GenreId, dto.Price, dto.ReleaseDate), details);
    }

    [Fact]
    public void ShouldProjectASummaryInAQueryableWithoutLoadingEntities()
    {
        var genre = new Genre { Id = 3, Name = "Platformer" };
        var games = new[] { StoredGame(genre) }.AsQueryable();

        var summaries = games.Select(GameMappingExtensions.SummaryProjection).ToList();

        Assert.Equal(["Platformer"], summaries.Select(s => s.Genre));
    }

    [Fact]
    public void ShouldKeepTheJsonContractStable()
    {
        var summary = StoredGame(new Genre { Id = 3, Name = "Platformer" }).ToSummaryDto();

        var json = JsonSerializer.Serialize(summary, JsonSerializerOptions.Web);

        Assert.Equal(
            """{"id":7,"name":"Astro Vault","genre":"Platformer","price":59.99,"releaseDate":"2023-10-20"}""",
            json);
    }
}
