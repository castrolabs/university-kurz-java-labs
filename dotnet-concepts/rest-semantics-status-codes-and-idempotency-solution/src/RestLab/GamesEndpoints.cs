using Microsoft.AspNetCore.Http.HttpResults;

namespace RestLab;

public static class GamesEndpoints
{
    private const string GetGameEndpointName = "GetGame";

    public static IEndpointRouteBuilder MapGamesEndpoints(this IEndpointRouteBuilder app)
    {
        var group = app.MapGroup("/games");

        group.MapGet("/", (GameStore store) => store.All());

        group.MapGet("/{id}", Results<Ok<Game>, NotFound> (int id, GameStore store) =>
            store.Find(id) is { } game ? TypedResults.Ok(game) : TypedResults.NotFound())
            .WithName(GetGameEndpointName);

        group.MapPost("/", (CreateGameDto dto, GameStore store) =>
        {
            var game = store.Add(dto);
            return Results.CreatedAtRoute(GetGameEndpointName, new { id = game.Id }, game);
        });

        group.MapPut("/{id}", (int id, UpdateGameDto dto, GameStore store) =>
            store.Replace(id, dto) ? Results.NoContent() : Results.NotFound());

        group.MapDelete("/{id}", (int id, GameStore store) =>
        {
            store.Remove(id);
            return Results.NoContent();
        });

        return app;
    }
}
