using Microsoft.Data.Sqlite;
using Microsoft.EntityFrameworkCore;

namespace EfCrudLab.Tests;

public sealed class GameServiceTests : IDisposable
{
    private readonly SqliteConnection _connection = new("Data Source=:memory:");
    private readonly DbContextOptions<GameStoreContext> _options;

    public GameServiceTests()
    {
        _connection.Open();
        var builder = new DbContextOptionsBuilder<GameStoreContext>().UseSqlite(_connection);
        ((DbContextOptionsBuilder)builder).ConfigureSeeding();
        _options = builder.Options;

        using var db = NewContext();
        db.Database.EnsureCreated();
    }

    public void Dispose() => _connection.Dispose();

    private GameStoreContext NewContext() => new(_options);

    private static CreateGameDto NewGame(string name = "Astro Vault", int genreId = 3) =>
        new(name, genreId, 59.99m, new DateOnly(2023, 10, 20));

    [Fact]
    public void ShouldSeedTheFiveGenresWhenTheDatabaseIsCreated()
    {
        using var db = NewContext();

        Assert.Equal(5, db.Genres.Count());
        Assert.Contains(db.Genres, g => g.Name == "Fighting");
    }

    [Fact]
    public void ShouldNotDuplicateGenresWhenSeedingRunsAgain()
    {
        using var db = NewContext();

        GameStoreDb.SeedGenres(db);
        GameStoreDb.SeedGenres(db);

        Assert.Equal(5, db.Genres.Count());
    }

    [Fact]
    public async Task ShouldPersistTheGameAndReturnTheGeneratedId()
    {
        int id;
        await using (var db = NewContext())
            id = await new GameService(db).CreateAsync(NewGame());

        Assert.True(id > 0);
        await using var other = NewContext();
        var stored = await other.Games.SingleAsync(g => g.Id == id);
        Assert.Equal("Astro Vault", stored.Name);
        Assert.Equal(3, stored.GenreId);
    }

    [Fact]
    public async Task ShouldRejectAGameWhoseGenreDoesNotExist()
    {
        await using var db = NewContext();

        await Assert.ThrowsAsync<DbUpdateException>(
            () => new GameService(db).CreateAsync(NewGame(genreId: 40)));
    }

    [Fact]
    public async Task ShouldReturnNullWhenTheGameDoesNotExist()
    {
        await using var db = NewContext();

        Assert.Null(await new GameService(db).GetByIdAsync(9999));
    }

    [Fact]
    public async Task ShouldReturnTheDetailsOfAStoredGame()
    {
        int id;
        await using (var db = NewContext())
            id = await new GameService(db).CreateAsync(NewGame("Street Fighter II", 1));

        await using var other = NewContext();
        var details = await new GameService(other).GetByIdAsync(id);

        Assert.Equal("Street Fighter II", details!.Name);
        Assert.Equal(1, details.GenreId);
    }

    [Fact]
    public async Task ShouldListGamesWithTheGenreNameInsteadOfTheId()
    {
        await using (var db = NewContext())
        {
            var service = new GameService(db);
            await service.CreateAsync(NewGame("Street Fighter II", 1));
            await service.CreateAsync(NewGame("Final Fantasy VII", 2));
        }

        await using var other = NewContext();
        var games = await new GameService(other).ListAsync();

        Assert.Equal(["Fighting", "Roleplaying"], games.Select(g => g.Genre));
        Assert.Empty(other.ChangeTracker.Entries());
    }

    [Fact]
    public async Task ShouldUpdateOnlyThePriceAndReportIfTheGameExisted()
    {
        int id;
        await using (var db = NewContext())
            id = await new GameService(db).CreateAsync(NewGame());

        bool found, missing;
        await using (var db = NewContext())
        {
            var service = new GameService(db);
            found = await service.UpdatePriceAsync(id, 9.99m);
            missing = await service.UpdatePriceAsync(9999, 9.99m);
        }

        Assert.True(found);
        Assert.False(missing);
        await using var other = NewContext();
        var stored = await other.Games.SingleAsync(g => g.Id == id);
        Assert.Equal(9.99m, stored.Price);
        Assert.Equal("Astro Vault", stored.Name);
    }

    [Fact]
    public async Task ShouldDeleteTheGameAndAcceptDeletingItAgain()
    {
        int id;
        await using (var db = NewContext())
            id = await new GameService(db).CreateAsync(NewGame());

        await using (var db = NewContext())
        {
            var service = new GameService(db);
            await service.DeleteAsync(id);
            await service.DeleteAsync(id);
        }

        await using var other = NewContext();
        Assert.Empty(other.Games);
    }
}
