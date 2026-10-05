# REST Semantics: Status Codes and Idempotency

## Goal

Make a small CRUD API answer with the status codes REST clients rely on: `404`
for a missing resource, `201` with a `Location` header on create, `204` for
update and delete, and an idempotent `DELETE`.

## Prerequisites

- Basic minimal APIs and route groups
- HTTP verbs and status code families
- .NET 10 SDK

## Task

`GamesEndpoints` stores games in memory and compiles, but every handler answers
`200 OK`, even for a game that does not exist. Fix each endpoint so a generic HTTP
client, a retrying proxy, and a front end can all trust the response. The tests
describe the contract; read their names first.

## Instructions

Complete the following TODOs:

- TODO-00: `GET /games/{id}` answers `404` when the game does not exist.
- TODO-01: `POST /games` answers `201 Created` with a `Location` header (use the
  route name and `CreatedAtRoute`) and the created game in the body.
- TODO-02: `PUT /games/{id}` answers `404` for an unknown id and `204 No Content`
  after replacing the game.
- TODO-03: `DELETE /games/{id}` answers `204 No Content` every time, including when
  the game was already gone.

Run the tests until they all pass.

## Running the Lab

From the lab directory:

```bash
dotnet test
```

## Bonus (Optional)

- TODO-04 (optional): Return `Results<Ok<Game>, NotFound>` from the GET-by-id handler
  so the possible responses are visible in its signature.
- TODO-05 (optional): Register `AddProblemDetails()`.
- TODO-06 (optional): Add `UseExceptionHandler()` and `UseStatusCodePages()`, then call
  `GET /games/9999` and compare the body with and without TODO-05 and TODO-06.
