# Clean Architecture in Four Projects (Solution)

Official solution for the `clean-architecture-in-four-projects` lab.

## What changed from the starter

- **Domain**: `Tenant` normalizes (trim, lower-case) and validates the subdomain in its
  constructor and in `Rename`, so no tenant can exist with an invalid subdomain, whatever layer
  creates it. The Id is a version 7 `Guid` (time-ordered, friendlier to indexes than a random
  one). The project has no package and no reference to another layer.
- **Application**: the handlers return a result (`Created`, `InvalidSubdomain`, `SubdomainTaken`;
  `Found`, `NotFound`, `NoSubdomain`) instead of HTTP codes or exceptions. The only thing they
  know about persistence is the `ITenantRepository` interface they declare themselves, which is why
  they are tested with a dictionary-backed fake and no database.
- **Infrastructure**: `TenantConfiguration` holds the mapping (key, max length 63, unique index),
  so the domain class has no attributes. `TenantRepository` uses `AsNoTracking` for reads and
  saves in `AddAsync`. Both are `internal`; the layer exposes only `AddInfrastructure` and
  `InitializeDatabase`. The unique index is the real guarantee: the handler pre-check gives a
  friendly `409`, the index covers the race between two concurrent requests.
- **Api**: `TenantsEndpoints` is the only place that translates results into status codes and
  DTOs. The composition root (`Program`) is the only place that knows every layer.
- The dependency tests read the compiled references: Domain depends on nothing, Application on
  Domain only, Infrastructure never on the Api.

## Bonus notes

- Moving to PostgreSQL touches only `Tenancy.Infrastructure` (package and `UseNpgsql`); Domain,
  Application and Api do not change.
- The Application layer cannot use `DbContext` because it has no reference to EF Core, and the
  dependency-rule tests fail if one is added.

## Running

```bash
dotnet test
```
