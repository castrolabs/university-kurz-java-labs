# Dependency Injection and Service Lifetimes

## Goal

Register services with the right lifetime (transient, scoped, singleton), avoid a
captive dependency, and create a scope by hand for code that runs outside a request.

## Prerequisites

- Constructor injection
- `IServiceCollection` and `IServiceProvider`
- .NET 10 SDK

## Task

`ServiceRegistration.AddGameStoreServices` registers nothing yet, so every test that
resolves a service fails. The tests build the provider with `ValidateScopes` and
`ValidateOnBuild` turned on, the same checks ASP.NET Core applies in `Development`.
Work out the lifetime each service needs, then fix the one class that cannot be a
singleton as written.

## Instructions

Complete the following TODOs:

- TODO-00: Register `RequestState` and `FakeDb` as scoped.
- TODO-01: Register `ISystemClock` with `SystemClock` as a singleton.
- TODO-02: Register `ReceiptNumberGenerator` as transient.
- TODO-03: Register `CatalogReader` as a singleton, read the validation error, and
  refactor `CatalogReader` to use `IServiceScopeFactory`.
- TODO-04: Implement `StartupTasks.SeedDatabase` with a scope you create and dispose.

Run the tests until they all pass.

## Running the Lab

From the lab directory:

```bash
dotnet test
```

## Bonus (Optional)

- TODO-05 (optional): Register two `IPaymentGateway` implementations as keyed
  singletons and make `CheckoutService` ask for the `"stripe"` one.
- TODO-06 (optional): Switch `ValidateOnBuild` and `ValidateScopes` off in the test
  helper and run the tests again. Which test still catches the captive dependency?
