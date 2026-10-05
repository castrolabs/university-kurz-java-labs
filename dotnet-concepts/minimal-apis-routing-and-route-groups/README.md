# Minimal APIs: Routing and Route Groups

## Goal

Map endpoints with route templates, understand how each handler parameter is bound
(route, query, body, service), name a route so a `201` can point at it, and share
configuration through a route group.

## Prerequisites

- C# lambdas and extension methods
- HTTP verbs and status codes
- .NET 10 SDK

## Task

`GamesEndpoints.MapGamesEndpoints` is an extension method on `IEndpointRouteBuilder` but
maps nothing yet. `Program` calls it directly on the app, so even the future paths would
live at the root. You will build the endpoints one binding source at a time, and the tests
check the URL space, the bound values, and the metadata the group attaches.

## Instructions

Complete the following TODOs:

- TODO-00: Put the games endpoints under `/api` using a route group in `Program.cs`.
- TODO-01: Create the `/games` group tagged `Games` in `GamesEndpoints`.
- TODO-02: `GET /` with query parameters (`genre`, `page` defaulting to 1).
- TODO-03: `GET /{id}` with an `int` route constraint and the route name.
- TODO-04: `GET /search` and make sure the literal segment wins over `{id}`.
- TODO-05: `GET /count` with an injected `GameStore`.
- TODO-06: `POST /` with a body, answering `201` and a `Location` header from the route name.

Run the tests until they all pass.

## Running the Lab

From the lab directory:

```bash
dotnet test
```

## Bonus (Optional)

- TODO-07 (optional): Move `/{id}` without the `:int` constraint and request
  `/api/games/abc`. What status do you get, and why is it different from a `404`?
- TODO-08 (optional): Change the `count` handler to a `GET` with a complex type parameter and
  call it. Read the exception and explain why the app started fine.
