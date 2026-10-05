# Input Validation in Minimal APIs

## Goal

Declare validation rules on a request DTO and turn on ASP.NET Core's built-in
validation so invalid requests are rejected with `400 Bad Request` before the
handler runs.

## Prerequisites

- Basic minimal APIs (`MapGet`, `MapPost`, route groups)
- C# records and attributes
- .NET 10 SDK

## Task

`POST /games` accepts a `CreateGameDto` and stores it with no checks, so a game
with no name or a price of 500 is created happily. `GET /games?page=` accepts any
page number. You will declare the rules, enable validation, and discover one trap
that makes the rules silently do nothing.

## Instructions

Complete the following TODOs:

- TODO-00: Register the validation services in `Program.cs`.
- TODO-01: Add data annotations to `CreateGameDto` (`Name` required and at most
  50 characters, `GenreId` from 1 to 50, `Price` from 1 to 100).
- TODO-02: The POST tests still fail after TODO-00 and TODO-01. Investigate why
  the annotations are ignored and fix it.
- TODO-03: Validate the `page` query parameter (1 to 100) in `GamesEndpoints`.

Run the tests until they all pass.

## Running the Lab

From the lab directory:

```bash
dotnet test
```

## Bonus (Optional)

- TODO-04 (optional): Add a `NotFutureAttribute` (a custom `ValidationAttribute`)
  and apply it to `ReleaseDate` so a release date in the future is rejected.
- TODO-05 (optional): Send an invalid body with `curl` and read the response
  carefully. Note the casing of the keys under `errors`.
