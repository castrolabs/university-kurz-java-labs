namespace ValidationLab;

public static class GamesEndpoints
{
    public static IEndpointRouteBuilder MapGamesEndpoints(this IEndpointRouteBuilder app)
    {
        var group = app.MapGroup("/games");

        // TODO-03: Reject page values outside 1..100 by annotating the parameter.
        group.MapGet("/", (GameStore store, int page = 1) =>
            Results.Ok(new { page, total = store.Count }));

        group.MapPost("/", (CreateGameDto dto, GameStore store) =>
        {
            var game = store.Add(dto);
            return Results.Created($"/games/{game.Id}", game);
        });

        return app;
    }
}
