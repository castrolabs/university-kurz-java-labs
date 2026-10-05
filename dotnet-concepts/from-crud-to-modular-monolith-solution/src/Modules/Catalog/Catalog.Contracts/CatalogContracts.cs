namespace Catalog.Contracts;

public sealed record GameInfoDto(int Id, string Name, decimal Price);

public interface ICatalogModule
{
    Task<IReadOnlyDictionary<int, GameInfoDto>> GetGamesAsync(
        IReadOnlyCollection<int> ids, CancellationToken ct);
}
