# Testing Clean Architecture with MSTest (Solution)

Official solution for the `testing-clean-architecture-with-mstest` lab.

## What changed from the starter

- **Domain tests** have no setup at all. `[DataRow]` turns the invalid subdomains and the normalization
  cases into one method each. `Tenant` throws `ArgumentException` itself, so `Assert.ThrowsExactly<ArgumentException>`
  is exact here; in the ordering lab the thrown type was a derived one and `ThrowsExactly` had to name it.
- **`FakeTenantRepository`** is a working list behind the port. Tests seed `Stored` before the act step
  and read it after, so they assert on the outcome (one tenant stored, none stored) and not on which
  methods were called. That keeps them valid if the handler later calls `ExistsAsync` twice.
- **`ResolveTenantHandlerTests`** keeps the host table in a `[DynamicData]` property of tuples. Adding a
  host is one line, and the failing row is named in the test output.
- **`TenantsApiTests`** creates a `WebApplicationFactory<Program>` and a temporary SQLite file in
  `[TestInitialize]` and removes both in an `async` `[TestCleanup]`, which can await the factory's
  `DisposeAsync` (an `IAsyncDisposable` test class would do the same). The assembly runs test methods in
  parallel (`MSTestSettings.cs`), so a shared database would let one test's tenants break another's `409`.
- **`TestContext`** arrives through the constructor (a primary constructor here). Its `CancellationToken`
  is passed to the handlers and to the HTTP calls instead of `CancellationToken.None`, so a test that
  times out, or a run that is aborted, cancels the work it started.
- **`DependencyRuleTests`** is one test over a table of layers. `DynamicDataDisplayName` gives each row a
  readable name, so a failure reads "Tenancy.Application must not reference Microsoft.EntityFrameworkCore"
  instead of "row 2". `ApplicationShouldStillReferenceTheDomain` guards the guard: a rule that matches
  nothing passes for the wrong reason.

## Bonus notes

- Mutation check, run against this solution. Each change was tried on its own:

| Change to the production code | Tests that fail |
| --- | --- |
| `Tenant.Normalize` stops lower-casing | domain normalization rows, the application "taken ignoring case" test, the API create and 409 tests |
| `CreateTenantHandler` skips `ExistsAsync` | the application "already taken" test and the API 409 test (the unique index turns the duplicate into an exception) |
| Endpoint returns `Results.Ok` instead of `Results.Conflict` | only `ShouldReturn409WhenTheSubdomainIsAlreadyTaken` |
| `.IsUnique()` removed from `TenantConfiguration` | only `ShouldEnforceUniqueSubdomainsInTheDatabaseItself` |
| `TenantRepository` made `public` | only `InfrastructureShouldKeepItsPersistenceTypesInternal` |
| `ResolveTenantHandler` stops lower-casing the host | the `ACME.example.com` row |
| `Tenancy.Application` uses EF Core | the Application row of the dependency rule |

- A reference that nothing uses is not recorded in the compiled assembly, so the dependency tests fire
  when code actually uses a type from the wrong layer. The compiler already rejects circular project
  references, so these tests mostly catch packages (EF Core, ASP.NET Core) and public types that should be internal.
