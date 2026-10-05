namespace RestLab;

public static class GamesEndpoints
{
    // TODO-01: Use this name to make the GET-by-id route reachable from the POST response.
    private const string GetGameEndpointName = "GetGame";

    public static IEndpointRouteBuilder MapGamesEndpoints(this IEndpointRouteBuilder app)
    {
        var group = app.MapGroup("/games");

        group.MapGet("/", (GameStore store) => store.All());

        // TODO-00: Answer 404 Not Found when the game does not exist.
        // TODO-04 (optional): Declare the possible results in the return type with
        //   TypedResults and Results<Ok<Game>, NotFound>.
        group.MapGet("/{id}", (int id, GameStore store) => Results.Ok(store.Find(id)));

        // TODO-01: Answer 201 Created with a Location header that points at the new game
        //   (CreatedAtRoute), and return the created game as the body.
        group.MapPost("/", (CreateGameDto dto, GameStore store) =>
        {
            var game = store.Add(dto);
            return Results.Ok(game);
        });

        // TODO-02: Answer 404 when the game does not exist and 204 No Content otherwise.
        group.MapPut("/{id}", (int id, UpdateGameDto dto, GameStore store) =>
        {
            store.Replace(id, dto);
            return Results.Ok();
        });

        // TODO-03: Answer 204 No Content whether or not the game existed.
        group.MapDelete("/{id}", (int id, GameStore store) =>
        {
            store.Remove(id);
            return Results.Ok();
        });

        return app;
    }
}
