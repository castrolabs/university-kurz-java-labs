# EF Core CRUD Operations and Seeding

## Goal

Implement create, read, update, and delete with EF Core against SQLite, project
straight into DTOs, and seed the reference rows that a foreign key depends on.

## Prerequisites

- C# and LINQ
- `async`/`await`
- Basic EF Core concepts (`DbContext`, `DbSet`)
- .NET 10 SDK

## Task

`Game` has a mandatory `GenreId`, so no game can be stored until the `Genres` table has
rows. `GameStoreDb` should seed five genres when the database is created, and
`GameService` should talk to the database through `GameStoreContext`. The tests run
against an in-memory SQLite database and use a **new context for every check**, so a
passing test proves the data reached the database and not just the change tracker.

## Instructions

Complete the following TODOs:

- TODO-00: Implement `GameStoreDb.SeedGenres` (idempotent).
- TODO-01: Implement `GameStoreDb.SeedGenresAsync`.
- TODO-02: Wire both into `ConfigureSeeding` with `UseSeeding` and `UseAsyncSeeding`.
- TODO-03: Implement `GameService.CreateAsync` (`Add` then `SaveChangesAsync`).
- TODO-04: Implement `GameService.GetByIdAsync`.
- TODO-05: Implement `GameService.ListAsync` with a projection.
- TODO-06: Implement `GameService.UpdatePriceAsync`.
- TODO-07: Implement `GameService.DeleteAsync` as a set-based delete.

Run the tests until they all pass.

## Running the Lab

From the lab directory:

```bash
dotnet test
```

## Bonus (Optional)

- TODO-08 (optional): Implement `GameService.SetPriceAsync` with `ExecuteUpdateAsync`.
- TODO-09 (optional): Turn on `LogTo(Console.WriteLine)` in the test options and read the
  SQL that `ListAsync` and `DeleteAsync` produce. Is there a join? A `SELECT` before
  the `DELETE`?
