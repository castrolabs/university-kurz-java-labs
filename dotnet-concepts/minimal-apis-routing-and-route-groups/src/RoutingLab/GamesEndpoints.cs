namespace RoutingLab;

public static class GamesEndpoints
{
    private const string GetGameEndpointName = "GetGame";

    public static IEndpointRouteBuilder MapGamesEndpoints(this IEndpointRouteBuilder app)
    {
        // TODO-01: Create a route group for "/games" and tag it "Games". Every endpoint
        //   below is mapped on this group with a path relative to it.

        // TODO-02: GET "/" binds an optional "genre" (string) and a "page" (int) that
        //   defaults to 1 from the query string. Return Results.Ok(new { genre, page }).

        // TODO-03: GET "/{id}" binds "id" from the route, but only when it is an integer:
        //   "/games/abc" must be a 404, not a 400. Return Results.Ok(new { kind = "byId", id })
        //   and give the route the name GetGameEndpointName.

        // TODO-04: GET "/search" binds "q" from the query and uses GameStore.Search. Return
        //   Results.Ok(new { kind = "search", results }). Make sure it is not shadowed by the
        //   "/{id}" route.

        // TODO-05: GET "/count" receives the GameStore from the container and returns
        //   Results.Ok(new { total = store.Count }).

        // TODO-06: POST "/" receives a CreateGameRequest from the body, adds the game to the
        //   store and answers 201 with a Location header that points at the named route.

        return app;
    }
}
