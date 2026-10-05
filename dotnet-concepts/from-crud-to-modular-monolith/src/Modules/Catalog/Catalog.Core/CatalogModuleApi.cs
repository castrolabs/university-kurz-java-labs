using Catalog.Contracts;

namespace Catalog.Core;

// TODO-00: Make this type (and Game and GameStore) internal. Only CatalogModule stays public.
// TODO-01: Implement GetGamesAsync: look the ids up in the store and answer a dictionary of
//   GameInfoDto keyed by id. Ids that do not exist are simply absent from the result.
public sealed class CatalogModuleApi(GameStore store) : ICatalogModule
{
    public Task<IReadOnlyDictionary<int, GameInfoDto>> GetGamesAsync(
        IReadOnlyCollection<int> ids, CancellationToken ct)
    {
        throw new NotImplementedException("Not implemented yet.");
    }
}
