namespace RestLab;

public sealed record Game(int Id, string Name, string Genre, decimal Price, DateOnly ReleaseDate);

public sealed record CreateGameDto(string Name, string Genre, decimal Price, DateOnly ReleaseDate);

public sealed record UpdateGameDto(string Name, string Genre, decimal Price, DateOnly ReleaseDate);

internal sealed class GameStore
{
    private readonly Dictionary<int, Game> _games = [];
    private readonly Lock _lock = new();
    private int _nextId = 1;

    public IReadOnlyList<Game> All()
    {
        lock (_lock) return [.. _games.Values];
    }

    public Game? Find(int id)
    {
        lock (_lock) return _games.GetValueOrDefault(id);
    }

    public Game Add(CreateGameDto dto)
    {
        lock (_lock)
        {
            var game = new Game(_nextId++, dto.Name, dto.Genre, dto.Price, dto.ReleaseDate);
            _games[game.Id] = game;
            return game;
        }
    }

    public bool Replace(int id, UpdateGameDto dto)
    {
        lock (_lock)
        {
            if (!_games.ContainsKey(id)) return false;
            _games[id] = new Game(id, dto.Name, dto.Genre, dto.Price, dto.ReleaseDate);
            return true;
        }
    }

    public void Remove(int id)
    {
        lock (_lock) _games.Remove(id);
    }
}
