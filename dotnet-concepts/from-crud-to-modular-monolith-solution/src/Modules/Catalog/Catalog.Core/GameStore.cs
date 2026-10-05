namespace Catalog.Core;

internal sealed class GameStore
{
    private readonly Dictionary<int, Game> _games = new()
    {
        [1] = new Game(1, "Street Fighter II", 19.99m),
        [2] = new Game(2, "Final Fantasy VII", 29.99m),
    };

    private readonly Lock _lock = new();

    public IReadOnlyList<Game> All()
    {
        lock (_lock) return [.. _games.Values];
    }

    public IReadOnlyList<Game> Find(IEnumerable<int> ids)
    {
        lock (_lock) return [.. ids.Where(_games.ContainsKey).Select(id => _games[id])];
    }

    public bool SetPrice(int id, decimal price)
    {
        lock (_lock)
        {
            if (!_games.TryGetValue(id, out var game)) return false;
            _games[id] = game with { Price = price };
            return true;
        }
    }
}
