# Testing EF Core Queries, Concurrency and Migrations (Solution)

Official solution for the `testing-ef-core-queries-concurrency-and-migrations` lab.

Requires Docker running locally. Run with `dotnet test` from this directory (16 tests, about 3 seconds
once the `postgres:18-alpine` image is pulled).

## What changed from the starter

- **`TestDatabase.CreateAsync`** applies the real migrations with `MigrateAsync`. `EnsureCreated` would
  build the schema from the model and hide a broken or missing migration. Each class gets its own database,
  and `ResetAsync` runs `TRUNCATE "OrderLine", "Orders" RESTART IDENTITY CASCADE` before every test.
  The table is `OrderLine` (singular) because the context has no `DbSet<OrderLine>`; EF Core names it after
  the entity type.
- **`CommandCounter`** is a `DbCommandInterceptor` that overrides `ReaderExecuting` and
  `ReaderExecutingAsync`, records `CommandText`, and returns the result unchanged. Command interceptors are
  registered per context, so each test passes a new instance to `NewContext`.
- **Query translation.** `SearchByCustomerIgnoresCase` only passes because `OrderQueries` uses
  `EF.Functions.ILike`: PostgreSQL compares strings case-sensitively, and `Contains` becomes a
  case-sensitive `LIKE '%acme%'`. `SummariesAreComputedFromTheLines` includes an order with no lines: the
  count and the total are correlated subqueries, and EF Core wraps the `sum` in `COALESCE(..., 0.0)` so an
  order with no lines gets `0` and not `NULL`. `ToQueryString()` prints the SQL when you want to look. `AQueryWithAComparisonTypeDoesNotTranslate` is a learning
  test: `string.Equals(..., StringComparison.OrdinalIgnoreCase)` throws on a relational provider, while the
  InMemory provider runs it as C#.
- **Query filters.** The context declares two named filters (EF Core 10): `Tenant` and `SoftDelete`.
  `DeletedNumbersAsync` calls `IgnoreQueryFilters([SoftDeleteFilter])`, so the tenant filter still applies.
  The test that proves it deletes an order in the other tenant too; without that row, the version that
  ignores every filter returns the same list and the test passes for the wrong reason.
- **Concurrency.** `Order.Version` is a `uint` mapped with `IsRowVersion()`, which Npgsql maps to the
  `xmin` system column (no extra column, no trigger). The real test uses two contexts that read the same
  version. `OneContextCannotConflictWithItself` is the trap written down: one context refreshes its
  version after each save, so it never conflicts, with or without a token.
- **Unique index to domain error.** `PlaceAsync` catches only a `DbUpdateException` whose inner
  `PostgresException` has `SqlState == PostgresErrorCodes.UniqueViolation` and the index name
  `ix_orders_tenant_number`, detaches the order, and throws `DuplicateOrderNumberException`. The index is
  `(TenantId, Number)`, so another tenant may reuse the number.
- **Migrations.** `EveryMigrationAppliesToAnEmptyDatabase` creates a database with no schema, migrates it,
  compares `GetMigrations()` with `GetAppliedMigrationsAsync()`, and writes an order to prove the schema
  fits the model. `TheModelHasNoChangesMissingAMigration` calls `HasPendingModelChanges()` (EF Core 8 and
  later); it compares the model with the last snapshot and never opens the connection.
- **`Ordering.csproj` pins `Microsoft.EntityFrameworkCore.Relational`.** Npgsql 10.0.3 depends on EF Core
  10.0.4, and the newest `Microsoft.EntityFrameworkCore.Design` pulls 10.0.15. With `PrivateAssets=all`
  the higher version stays in `Ordering` and the test project fails with CS1705 (a referenced assembly
  needs a higher version). One explicit reference lifts both projects to 10.0.15.

## Bonus notes

Mutation check, run against this solution. Each change was tried on its own and then reverted:

| Change to the production code | Tests that fail |
| --- | --- |
| `SummariesAsync` loads the orders, then one query per order for the lines | both rows of `SummariesTakeOneCommandWhateverTheNumberOfOrders` (the functional summary test stays green) |
| `Tenant` query filter removed | `ATenantSeesOnlyItsOwnOrders`, `ListingDeletedOrdersKeepsTheTenantFilter` |
| `DeletedNumbersAsync` calls `IgnoreQueryFilters()` with no names | `ListingDeletedOrdersKeepsTheTenantFilter` |
| `PlaceAsync` no longer translates the unique violation | `ATakenNumberIsReportedAsADomainError` |
| `EF.Functions.ILike` replaced by `Contains` | `SearchByCustomerIgnoresCase` |
| A new `Note` property on `Order`, no migration (TODO-09) | all 16: `MigrateAsync` throws `PendingModelChangesWarning`, and `HasPendingModelChanges()` is true |
| `IsRowVersion()` replaced by `Ignore(o => o.Version)`, no migration | all 16, for the same reason |
| The same, plus `dotnet ef migrations add` | `TheSecondOfTwoConcurrentWritersGetsAConflict`, `EveryUpdateMovesTheVersion`; `OneContextCannotConflictWithItself` stays green |

The last row is why the one-context test is a learning test and not a concurrency test.
