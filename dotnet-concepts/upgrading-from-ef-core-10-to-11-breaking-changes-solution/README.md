# Upgrading from EF Core 10 to 11: Breaking Changes (Solution)

## Goal

Fix the silent breaking changes of an EF Core 10 to 11 upgrade. See the starter project for
the exercise statement.

## Solution notes

- `Migrate()` on EF Core 11 throws `InvalidOperationException` (`RelationalEventId.MigrationsNotFound`)
  when the assembly has no migrations; EF Core 10 returned quietly. A project without migrations
  uses `EnsureCreated()`. A project with migrations never hits this.
- `Property(c => c.Tags)` no longer configures a primitive collection. `PrimitiveCollection(c => c.Tags)`
  does, and the `Contains` query then translates to `json_each` on SQLite.
- `OwnsMany(...).ToJson()` without a key raises `OwnedEntityMappedToJsonCollectionWarning`.
  `ComplexCollection(...).ToJson()` is the replacement.
- `HasIndex("Address.City")` over a complex property in JSON becomes `CREATE INDEX ... ON "Customers" ("Address" ->> 'City')`
  on SQLite with EF Core 11.
- SQL Server: the default compatibility level moved to 160, so `Math.Max` translates to `GREATEST`.
  `UseCompatibilityLevel(150)` makes EF Core evaluate it on the client again. `ToQueryString()` shows the
  difference without a server.
- `MaxByAsync` is translated by EF Core 11 to an `ORDER BY ... LIMIT 1`.
