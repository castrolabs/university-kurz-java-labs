# Feature Flags with Microsoft.FeatureManagement

## Goal

Drive code paths, endpoints and a banner text from feature flags kept in
configuration: a plain flag, the percentage, time window and targeting
filters, `[FeatureGate]` on a controller and on a Minimal API endpoint, and
variants. The flags are configuration, so the tests change them per test with
an in-memory JSON source and `WebApplicationFactory`.

## Prerequisites

- Minimal APIs and `IConfiguration`
- Integration tests with `WebApplicationFactory`
- .NET 10 SDK

## Task

`Program.cs` has five endpoints and `ReportsController` one, and none of them
looks at a flag yet. `appsettings.json` defines every flag as disabled (the
`feature_management` schema), and `HeaderTargetingContextAccessor` already says
who is calling through the `X-User` and `X-Groups` headers. Each test builds the
app with its own list of flags, so the filters (percentage, time window,
targeting) and the variant allocation are all configured in the tests: the
application code only asks "is it on?" or "which variant?".

## Instructions

Complete the following TODOs:

- TODO-00: Register feature management and enable targeting with `HeaderTargetingContextAccessor` in `Program.cs`.
- TODO-01: Make `/pricing` answer `price v2` when `NewPricing` is enabled, using `IFeatureManager`.
- TODO-02: Gate `/checkout/express` with `ExpressCheckout` so it answers 404 while the flag is off.
- TODO-03: Make `/promo` answer `promo on` when `Promo` is enabled. The time window is configuration.
- TODO-04: Make `/banner` return `<variant name>|<variant configuration value>` for the `Banner` flag with `IVariantFeatureManager`.
- TODO-05: Make `/flags` list every defined flag with its state, sorted by name.
- TODO-06: Gate `ReportsController` with `[FeatureGate]` on `Reports`.

Run the tests until they all pass.

## Running the Lab

```bash
dotnet test
```

## Bonus (Optional)

- TODO-07 (optional): Add a custom filter (`IFeatureFilter` with `[FilterAlias]`) that enables a flag only for requests with a header `X-Tenant: internal`, and test it.
- TODO-08 (optional): Compare `/flags` with the constants in `Flags.cs`. Which flags are defined but never read by code, and which are read but not defined? How would you fail the build on either?
