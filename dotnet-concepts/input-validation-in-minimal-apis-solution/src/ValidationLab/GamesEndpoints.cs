using System.ComponentModel.DataAnnotations;

namespace ValidationLab;

public static class GamesEndpoints
{
    public static IEndpointRouteBuilder MapGamesEndpoints(this IEndpointRouteBuilder app)
    {
        var group = app.MapGroup("/games");

        group.MapGet("/", (GameStore store, [Range(1, 100)] int page = 1) =>
            Results.Ok(new { page, total = store.Count }));

        group.MapPost("/", (CreateGameDto dto, GameStore store) =>
        {
            var game = store.Add(dto);
            return Results.Created($"/games/{game.Id}", game);
        });

        return app;
    }
}
