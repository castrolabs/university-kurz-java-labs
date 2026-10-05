using Microsoft.EntityFrameworkCore;

namespace EfCrudLab;

public class GameService(GameStoreContext db)
{
    public async Task<int> CreateAsync(CreateGameDto dto)
    {
        var game = new Game
        {
            Name = dto.Name,
            GenreId = dto.GenreId,
            Price = dto.Price,
            ReleaseDate = dto.ReleaseDate,
        };

        db.Games.Add(game);
        await db.SaveChangesAsync();

        return game.Id;
    }

    public async Task<GameDetailsDto?> GetByIdAsync(int id)
    {
        var game = await db.Games.FindAsync(id);

        return game is null
            ? null
            : new GameDetailsDto(game.Id, game.Name, game.GenreId, game.Price, game.ReleaseDate);
    }

    public async Task<IReadOnlyList<GameSummaryDto>> ListAsync() =>
        await db.Games
            .OrderBy(g => g.Id)
            .Select(g => new GameSummaryDto(g.Id, g.Name, g.Genre!.Name, g.Price, g.ReleaseDate))
            .AsNoTracking()
            .ToListAsync();

    public async Task<bool> UpdatePriceAsync(int id, decimal price)
    {
        var game = await db.Games.FindAsync(id);
        if (game is null) return false;

        game.Price = price;
        await db.SaveChangesAsync();

        return true;
    }

    public async Task SetPriceAsync(int id, decimal price) =>
        await db.Games
            .Where(g => g.Id == id)
            .ExecuteUpdateAsync(setters => setters.SetProperty(g => g.Price, price));

    public async Task DeleteAsync(int id) =>
        await db.Games.Where(g => g.Id == id).ExecuteDeleteAsync();
}
