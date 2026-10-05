using System.Linq.Expressions;

namespace DtoLab;

public static class GameMappingExtensions
{
    // TODO-04: Replace the body with an expression that builds a GameSummaryDto from a Game
    //   (use g.Genre!.Name). It is an Expression, not a method, so a query provider such as
    //   EF Core can translate it into SQL and select only these columns.
    public static readonly Expression<Func<Game, GameSummaryDto>> SummaryProjection =
        _ => NotImplementedYet<GameSummaryDto>();

    public static Game ToEntity(this CreateGameDto dto)
    {
        // TODO-00: Build a Game from the dto. Do not set the Id, the database owns it.
        throw new NotImplementedException("Not implemented yet.");
    }

    public static void ApplyTo(this UpdateGameDto dto, Game game)
    {
        // TODO-01: Copy every field of the dto onto the existing game, except the Id.
        throw new NotImplementedException("Not implemented yet.");
    }

    public static GameDetailsDto ToDetailsDto(this Game game)
    {
        // TODO-02: Map to the details shape (the genre is the numeric GenreId).
        throw new NotImplementedException("Not implemented yet.");
    }

    public static GameSummaryDto ToSummaryDto(this Game game)
    {
        // TODO-03: Map to the summary shape (the genre is its NAME). When the Genre navigation
        //   was not loaded, throw an InvalidOperationException whose message mentions "Genre"
        //   instead of letting a NullReferenceException escape.
        throw new NotImplementedException("Not implemented yet.");
    }

    private static T NotImplementedYet<T>() => throw new NotImplementedException("Not implemented yet.");
}
