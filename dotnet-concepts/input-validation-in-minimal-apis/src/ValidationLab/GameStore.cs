namespace ValidationLab;

public sealed record Game(int Id, string Name, int GenreId, decimal Price, DateOnly ReleaseDate);

internal sealed class GameStore
{
    private readonly List<Game> _games = [];
    private readonly Lock _lock = new();

    public int Count
    {
        get { lock (_lock) return _games.Count; }
    }

    public Game Add(CreateGameDto dto)
    {
        lock (_lock)
        {
            var game = new Game(_games.Count + 1, dto.Name, dto.GenreId, dto.Price, dto.ReleaseDate);
            _games.Add(game);
            return game;
        }
    }
}
