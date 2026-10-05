# Input Validation in Minimal APIs (Solution)

Official solution for the `input-validation-in-minimal-apis` lab.

## What changed from the starter

- `Program.cs`: `builder.Services.AddValidation()` turns on validation for every
  minimal API handler parameter that carries data annotations.
- `CreateGameDto`: the rules live on the positional parameters, and the record is
  **`public`**.
- `GamesEndpoints`: `[Range(1, 100)]` on the `page` query parameter. The optional
  parameter goes last, because C# requires optional parameters after required ones.

## Why the type has to be public

The starter declared `internal record CreateGameDto(...)`. With `AddValidation()`
and the annotations in place, an invalid body still returned `201 Created`:
the validation setup never discovered the internal type, and nothing in the logs
said so. The `[Range]` on a plain `int page` parameter kept working, which makes
the failure easy to miss. Making the record `public` fixes it. The tests that
post an invalid body and expect `400` are the safety net for this class of bug.

## Bonus solutions

- `NotFutureAttribute` is a `ValidationAttribute` whose `IsValid` returns a
  `ValidationResult` with a message when the date is later than today (UTC).
- The `errors` object in the response uses C# property names (`Name`, `GenreId`),
  not camelCase, and lists every failing field at once.

## Running

```bash
dotnet test
```
