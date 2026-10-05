# Minimal APIs: Routing and Route Groups (Solution)

Official solution for the `minimal-apis-routing-and-route-groups` lab.

## What changed from the starter

- `Program` maps the endpoints on `app.MapGroup("/api")`. Because the extension method
  takes `IEndpointRouteBuilder` (not `WebApplication`), it works inside any group, and the
  inner paths do not change.
- `GamesEndpoints` creates `MapGroup("/games").WithTags("Games")`: the tag is group-level
  configuration, so every endpoint inherits it.
- Binding sources: `genre` and `page` come from the query (`page` has a default),
  `id` from the route, `q` from the query, `GameStore` from the container, and
  `CreateGameRequest` from the JSON body.
- `/{id:int}` makes `/games/abc` a `404`. Without the constraint, the route still matches,
  the `int` conversion fails, and the answer is `400`.
- Literal segments are more specific than parameters, so `/search` and `/count` are not
  shadowed by `/{id:int}`, regardless of the order of the `Map*` calls.
- `WithName(GetGameEndpointName)` plus `Results.CreatedAtRoute(name, new { id }, body)`
  builds the `Location` header from the route, so the URL follows the template.

## Bonus notes

- A complex type parameter on a `GET` compiles and the app starts. The failure appears on
  the first request as `500` with `Body was inferred but the method does not allow inferred
  body parameters`, because the endpoint delegate is built lazily.

## Running

```bash
dotnet test
```
