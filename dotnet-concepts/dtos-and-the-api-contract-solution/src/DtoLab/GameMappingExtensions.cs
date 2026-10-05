using System.Linq.Expressions;

namespace DtoLab;

public static class GameMappingExtensions
{
    public static readonly Expression<Func<Game, GameSummaryDto>> SummaryProjection =
        g => new GameSummaryDto(g.Id, g.Name, g.Genre!.Name, g.Price, g.ReleaseDate);

    public static Game ToEntity(this CreateGameDto dto) => new()
    {
        Name = dto.Name,
        GenreId = dto.GenreId,
        Price = dto.Price,
        ReleaseDate = dto.ReleaseDate,
    };

    public static void ApplyTo(this UpdateGameDto dto, Game game)
    {
        game.Name = dto.Name;
        game.GenreId = dto.GenreId;
        game.Price = dto.Price;
        game.ReleaseDate = dto.ReleaseDate;
    }

    public static GameDetailsDto ToDetailsDto(this Game game) =>
        new(game.Id, game.Name, game.GenreId, game.Price, game.ReleaseDate);

    public static GameSummaryDto ToSummaryDto(this Game game) =>
        new(
            game.Id,
            game.Name,
            game.Genre?.Name ?? throw new InvalidOperationException(
                $"Game {game.Id} has no Genre loaded. Include it or project in the query."),
            game.Price,
            game.ReleaseDate);
}
