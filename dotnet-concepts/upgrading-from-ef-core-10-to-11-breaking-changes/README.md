# Upgrading from EF Core 10 to 11: Breaking Changes

## Goal

Move a small EF Core 10 application to EF Core 11 and fix the changes that do not show up
as compile errors: a startup call that now throws, a mapping that silently stops
translating a query, an obsolete JSON mapping and a SQL Server default that changed.
Tests run on SQLite in memory, and the SQL Server case only inspects the generated SQL.

## Prerequisites

- EF Core basics, LINQ and JSON columns
- .NET 11 SDK (the project targets `net11.0` and `Microsoft.EntityFrameworkCore` 11.0.0)

## Task

The code in `src/UpgradeLab` is what an EF Core 10 project looked like. The packages are
already on 11.0.0 and the tests describe what the application must do. Several of them fail
for reasons that the compiler does not report. Find each cause and fix it.

## Instructions

Complete the following TODOs:

- TODO-00: `Bootstrap.Prepare` must create the schema in a project that has no migrations.
- TODO-01: Make `Tags` a primitive collection again so `Tags.Contains(...)` translates.
- TODO-02: Map `Notes` as a complex collection in JSON, without the owned-entity warning.
- TODO-03: Create an index over the city inside the `Address` JSON document.
- TODO-04: Keep `Math.Max` from becoming `GREATEST` for a SQL Server 2019 target.

Run the tests until they all pass.

## Running the Lab

```bash
dotnet test
```

## Bonus (Optional)

- TODO-05 (optional): Find the customer with the most tags with `MaxByAsync`, which EF Core 11 translates.
