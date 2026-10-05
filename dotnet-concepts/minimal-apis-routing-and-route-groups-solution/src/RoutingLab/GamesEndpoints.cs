namespace RoutingLab;

public static class GamesEndpoints
{
    private const string GetGameEndpointName = "GetGame";

    public static IEndpointRouteBuilder MapGamesEndpoints(this IEndpointRouteBuilder app)
    {
        var group = app.MapGroup("/games").WithTags("Games");

        group.MapGet("/", (string? genre, int page = 1) => Results.Ok(new { genre, page }));

        group.MapGet("/count", (GameStore store) => Results.Ok(new { total = store.Count }));

        group.MapGet("/search", (string q, GameStore store) => Results.Ok(new { kind = "search", results = store.Search(q) }));

        group.MapGet("/{id:int}", (int id) => Results.Ok(new { kind = "byId", id }))
            .WithName(GetGameEndpointName);

        group.MapPost("/", (CreateGameRequest request, GameStore store) =>
        {
            var game = store.Add(request.Name);
            return Results.CreatedAtRoute(GetGameEndpointName, new { id = game.Id }, game);
        });

        return app;
    }
}
