# Clean Architecture in Four Projects

## Goal

Organize a small multi-tenant API into four projects with a strict dependency rule
(`Domain` <- `Application` <- `Infrastructure` and `Api`), so business rules do not know
about EF Core or HTTP, and each layer can be tested on its own.

## Prerequisites

- C# classes, interfaces and project references
- Minimal APIs and dependency injection
- EF Core basics
- .NET 10 SDK

## Task

A tenant is identified by a subdomain (`acme.example.com` belongs to the tenant `acme`).

```text
src/
  Tenancy.Domain          -> Tenant (rules and identity), no dependencies
  Tenancy.Application     -> use cases (handlers) and the ports they need (ITenantRepository)
  Tenancy.Infrastructure  -> EF Core, the repository implementation, DI registration
  Tenancy.Api             -> HTTP: endpoints, tenant-resolution middleware, composition root
```

The layout, the ports and the middleware are done. The rules of each layer are not: the
domain accepts any string as a subdomain, the handlers and the repository throw, and the
endpoint to create a tenant does not exist yet. The tests are split by layer: domain tests
need nothing, application tests use a fake repository, and API tests run the whole stack
against a temporary SQLite file. A last group of tests checks the dependency rule itself.

## Instructions

Complete the following TODOs:

- TODO-00: Validate and normalize the subdomain in `Tenant`.
- TODO-01: Add `Tenant.Rename`.
- TODO-02: Implement `CreateTenantHandler`.
- TODO-03: Implement `ResolveTenantHandler`.
- TODO-04: Map `Tenant` in `TenantConfiguration` (key, length, unique index).
- TODO-05: Implement `TenantRepository` with EF Core.
- TODO-06: Map the `POST /tenants` result to HTTP in `TenantsEndpoints`.

Run the tests until they all pass. Start from the bottom: domain, then application, then the rest.

## Running the Lab

From the lab directory:

```bash
dotnet test
```

## Bonus (Optional)

- TODO-07 (optional): Add `using Microsoft.EntityFrameworkCore;` to a file in `Tenancy.Application`
  and try to use `DbContext`. What stops you, and which test would fail if you forced it?
- TODO-08 (optional): Replace SQLite with PostgreSQL by changing only `Tenancy.Infrastructure`
  (package and `UseNpgsql`). Which projects did not change?
- TODO-09 (optional): Write a second `ITenantRepository` that keeps tenants in a dictionary and
  register it from the Api tests. What does that tell you about the layer boundary?
