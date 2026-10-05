using Microsoft.EntityFrameworkCore;

namespace EfCrudLab;

public class GameService(GameStoreContext db)
{
    public Task<int> CreateAsync(CreateGameDto dto)
    {
        // TODO-03: Build a Game from the dto, add it to the context and save. Return the id
        //   that the database generated.
        throw new NotImplementedException("Not implemented yet.");
    }

    public Task<GameDetailsDto?> GetByIdAsync(int id)
    {
        // TODO-04: Look the game up by primary key. Return null when it does not exist,
        //   otherwise a GameDetailsDto (never the entity itself).
        throw new NotImplementedException("Not implemented yet.");
    }

    public Task<IReadOnlyList<GameSummaryDto>> ListAsync()
    {
        // TODO-05: Project every game, ordered by Id, straight into a GameSummaryDto that
        //   carries the genre NAME. Do not load entities you will not modify.
        throw new NotImplementedException("Not implemented yet.");
    }

    public Task<bool> UpdatePriceAsync(int id, decimal price)
    {
        // TODO-06: Load the game, change only the price and save. Return false when the
        //   game does not exist.
        throw new NotImplementedException("Not implemented yet.");
    }

    public Task DeleteAsync(int id)
    {
        // TODO-07: Delete the game with a single statement and without loading it. It must
        //   not fail when the id does not exist.
        throw new NotImplementedException("Not implemented yet.");
    }

    public Task SetPriceAsync(int id, decimal price)
    {
        // TODO-08 (optional): Change the price with one UPDATE statement and no tracking.
        throw new NotImplementedException("Not implemented yet.");
    }
}
