# Structured Logging with Serilog and Log Sinks

## Goal

Replace the default logging setup of a minimal API with Serilog: message
templates instead of interpolation, properties from `LogContext`, a
destructured object that masks personal data, a source-generated
`[LoggerMessage]`, one summary line per request, and sinks (compact JSON on
the console and a rolling file) configured from `appsettings.json`.

## Prerequisites

- `ILogger<T>`, log levels and categories
- Minimal APIs and middleware order
- .NET 10 SDK

## Task

`Program.cs` still uses the default logging providers, so nothing reaches
Serilog. `appsettings.json` already has the `Serilog` section (levels, a
Console sink and a rolling File sink, both with `CompactJsonFormatter`). The
tests use `WebApplicationFactory` and register an in-memory `ILogEventSink`
through the service provider, so they assert on the structured properties of
each `LogEvent` and never parse text. One test also reads the rolling file.

## Instructions

Complete the following TODOs:

- TODO-00: Call `UseSerilog` with `ReadFrom.Configuration`, `ReadFrom.Services`, `Enrich.FromLogContext` and a destructuring transform that masks `Customer.Email`.
- TODO-01: In `OrderService.Place`, use the template `"Order {OrderId} placed, total {Total}"`.
- TODO-02: In `TenantMiddleware`, push the `Tenant` property into the `LogContext` for the whole request.
- TODO-03: Add `UseSerilogRequestLogging` and set `Tenant` on the summary with `EnrichDiagnosticContext`.
- TODO-04: Log the customer with `{@Customer}` so the structure (and the masked e-mail) is kept.
- TODO-05: Turn `Refuse` into a `[LoggerMessage]` partial method (EventId 3001, `Warning`).

Run the tests until they all pass.

## Running the Lab

```bash
dotnet test
```

## Bonus (Optional)

- TODO-06 (optional): Run `dotnet run`, call the endpoints with `curl` and read `logs/lab-YYYYMMDD.ndjson`. Which properties did ASP.NET Core add (`RequestId`, `RequestPath`, `@tr`)?
- TODO-07 (optional): Remove the `Microsoft.AspNetCore` override from `appsettings.json` and count how many lines one request produces.
- TODO-08 (optional): Misspell a sink name in `appsettings.json`. Does the app fail to start? Enable `Serilog.Debugging.SelfLog` to find out why.
