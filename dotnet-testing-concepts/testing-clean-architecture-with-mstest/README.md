# Testing Clean Architecture with MSTest

## Goal

The multi-tenant API from the Clean Architecture lab is already fully implemented, in four projects
(`Domain`, `Application`, `Infrastructure`, `Api`). Your job is to write the MSTest tests, one test
project per layer, and to see how each layer needs a different kind of test: none for the domain, a fake
for the use cases, a real host for HTTP, and a rule test for the architecture itself.

## Prerequisites

- Clean Architecture in Four Projects (the concept and its lab)
- Basic MSTest: `[TestClass]`, `[TestMethod]`
- .NET 10 SDK

## Task

Read `src/Tenancy.Domain/Tenant.cs` and the two handlers in `src/Tenancy.Application`, then open the
three test projects. Every test method is already declared with a hint, and fails with
`TODO-NN: write this test` until you write it.

Look at each test project's references. `Tenancy.Domain.Tests` can only see the domain, so a test there
cannot reach EF Core or HTTP even by accident. `Tenancy.Api.Tests` references everything, because it is
the only place where the layers meet.

## Instructions

Complete the following TODOs:

- TODO-00: `Tenancy.Domain.Tests/TenantTests`: id version, normalization and invalid inputs with
  `[DataRow]`, the 63 character boundary, and rename.
- TODO-01: `Tenancy.Application.Tests/FakeTenantRepository`: an in-memory implementation of the port.
- TODO-02: `CreateTenantHandlerTests`: created, invalid and taken, checking the status and what was stored.
- TODO-03: `ResolveTenantHandlerTests`: a table of hosts with `[DynamicData]`.
- TODO-04: `Tenancy.Api.Tests/TenantsApiTests`: a new API and a new SQLite file per test, with
  `[TestInitialize]` and an async `[TestCleanup]`.
- TODO-05: `TenantsApiTests`: the HTTP status for each outcome, and the unique index in the database.
- TODO-06: `DependencyRuleTests`: one `[DynamicData]` table with what each layer must not reference.

Run the tests until they all pass.

## Running the Lab

From the lab directory:

```bash
dotnet test
```

## Bonus (Optional)

- TODO-07 (optional): Mutation check. Break the production code on purpose and see which layer notices:
  stop lower-casing in `Tenant.Normalize`, skip the `ExistsAsync` check in `CreateTenantHandler`, return
  `Results.Ok` instead of `Results.Conflict`, remove `.IsUnique()` from `TenantConfiguration`, make
  `TenantRepository` public. Each change should turn at least one test red.
- TODO-08 (optional): Add `<PackageReference Include="Microsoft.EntityFrameworkCore" ... />` to
  `Tenancy.Application` and a class that derives from `DbContext`. Which test fails, and what does its
  message say?
- TODO-09 (optional): Run only the architecture tests with `dotnet test --filter "TestCategory=Architecture"`,
  then compare how long the domain project takes with how long the API project takes.
