using Catalog.Contracts;

namespace Catalog.Core;

internal sealed class CatalogModuleApi(GameStore store) : ICatalogModule
{
    public Task<IReadOnlyDictionary<int, GameInfoDto>> GetGamesAsync(
        IReadOnlyCollection<int> ids, CancellationToken ct)
    {
        IReadOnlyDictionary<int, GameInfoDto> games = store.Find(ids)
            .ToDictionary(g => g.Id, g => new GameInfoDto(g.Id, g.Name, g.Price));

        return Task.FromResult(games);
    }
}
