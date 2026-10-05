namespace RoutingLab;

public sealed record Game(int Id, string Name);

public sealed record CreateGameRequest(string Name);

public sealed class GameStore
{
    private readonly List<Game> _games = [new(1, "Street Fighter II"), new(2, "Final Fantasy VII"), new(3, "Astro Vault")];
    private readonly Lock _lock = new();

    public int Count
    {
        get { lock (_lock) return _games.Count; }
    }

    public Game Add(string name)
    {
        lock (_lock)
        {
            var game = new Game(_games.Count + 1, name);
            _games.Add(game);
            return game;
        }
    }

    public IReadOnlyList<Game> Search(string term)
    {
        lock (_lock)
            return [.. _games.Where(g => g.Name.Contains(term, StringComparison.OrdinalIgnoreCase))];
    }
}
