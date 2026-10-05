# EF Core CRUD Operations and Seeding (Solution)

Official solution for the `ef-core-crud-operations-and-seeding` lab.

## What changed from the starter

- `SeedGenres` checks `Any()` first, because the seed runs on every migrate, then adds the
  five genres and saves. `SeedGenresAsync` is the same with `AnyAsync` and
  `SaveChangesAsync`. `ConfigureSeeding` registers both: `Migrate()` and
  `EnsureCreated()` run the synchronous one, the `...Async` variants the other.
- `CreateAsync`: `Add` only records intent; `SaveChangesAsync` writes it and fills in the
  generated `Id`. An unknown `GenreId` fails at save time with a `DbUpdateException` from
  the foreign key.
- `GetByIdAsync` uses `FindAsync` (primary key lookup, tracker first) and maps to a DTO.
- `ListAsync` projects with `Select` and reads `g.Genre!.Name`: EF Core translates the
  join and selects only the columns it needs, so no `Include` is required and nothing is
  tracked. `OrderBy` makes the order deterministic.
- `UpdatePriceAsync` loads the entity, assigns the property and saves: the change tracker
  produces an `UPDATE` of only the changed column.
- `DeleteAsync` is `Where(...).ExecuteDeleteAsync()`: one `DELETE ... WHERE`, no load, no
  `SaveChanges`, and zero rows affected when the id is already gone.

## Bonus solutions

- `SetPriceAsync` uses `ExecuteUpdateAsync` with `SetProperty`: one statement, but it
  bypasses the change tracker, so a game already loaded in the same context would be stale.

## Running

```bash
dotnet test
```
