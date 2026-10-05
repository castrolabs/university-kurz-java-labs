# DTOs and the API Contract (Solution)

Official solution for the `dtos-and-the-api-contract` lab.

## What changed from the starter

- `ToEntity` and `ApplyTo` are the only places that know how a request becomes an entity:
  `Id` is never copied from a client, which closes the mass-assignment hole.
- `ToDetailsDto` and `ToSummaryDto` are different on purpose: the edit form needs the id
  of the genre, the list needs its name.
- `ToSummaryDto` throws `InvalidOperationException` with a message when `Genre` is `null`
  instead of using `Genre!.Name` and failing with a `NullReferenceException`.
- `SummaryProjection` is an `Expression<Func<Game, GameSummaryDto>>`. With EF Core,
  `db.Games.Select(SummaryProjection)` translates to a join that selects only the columns
  of the DTO, so no `Include` and no tracking are needed. The `!` is safe there because
  the translation happens in SQL; it would not be safe on an entity you loaded without
  the navigation.

## Bonus notes

- Compiling `SummaryProjection` and reusing it in `ToSummaryDto` removes duplication but
  makes the in-memory path depend on `Genre!` (a `NullReferenceException` again). Keeping
  the two paths separate is the explicit trade-off the solution made.
- A round-trip test with every field filled (`dto.ToEntity().ToDetailsDto()`) catches a
  field that was added to the DTO and forgotten in the mapping.

## Running

```bash
dotnet test
```
