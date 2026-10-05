# Dependency Injection and Service Lifetimes (Solution)

Official solution for the `dependency-injection-and-service-lifetimes` lab.

## What changed from the starter

- `RequestState` and `FakeDb` are **scoped**: one per request, shared inside it. A
  `DbContext` is registered this way for the same reasons (not thread-safe, tracks
  everything it loads, connections are a limited resource).
- `ISystemClock` is a **singleton** and `ReceiptNumberGenerator` is **transient**.
- `CatalogReader` is a singleton, so it cannot hold a `FakeDb`: that would capture the
  first request's instance forever. It takes `IServiceScopeFactory`, opens a scope per
  lookup, and the `using` disposes the scope and the db with it.
- `StartupTasks.SeedDatabase` creates its own scope. Resolving a scoped service from the
  root provider would keep it alive for the whole application.

## Why validation matters

With `ValidateOnBuild` the provider refuses to build when a singleton consumes a scoped
service: `Cannot consume scoped service 'FakeDb' from singleton 'CatalogReader'`.
ASP.NET Core enables the same check only in the `Development` environment, so the same
code can start fine in `Production` and misbehave under load.

## Bonus solutions

- `AddKeyedSingleton<IPaymentGateway, StripeGateway>("stripe")` plus
  `[FromKeyedServices("stripe")]` on the `CheckoutService` constructor parameter picks
  one implementation without injecting a collection and filtering.

## Running

```bash
dotnet test
```
