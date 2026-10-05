# Configuration and the Options Pattern

## Goal

Assemble configuration from several sources with the right precedence, read a required
connection string safely, and bind a section to a typed options class that is validated
when the application starts.

## Prerequisites

- `IConfiguration` basics and JSON
- Dependency injection (`IServiceCollection`)
- .NET 10 SDK

## Task

`AppConfiguration.Build` should behave like `WebApplication.CreateBuilder`: a base file, an
optional environment file, environment variables, and the command line, each overriding the
previous one. `StoreOptions` carries a required `Name` and a `PageSize` with a range. The
tests build configuration from in-memory streams and start a real host, so a missing
setting fails exactly where it would in production: at startup.

## Instructions

Complete the following TODOs:

- TODO-00: Implement `AppConfiguration.Build` with the four sources in the right order.
- TODO-01: Implement `GetRequiredConnectionString` with a message that names the key.
- TODO-02: Implement `AddStoreOptions` with binding, data annotations and start-up validation.

Run the tests until they all pass.

## Running the Lab

From the lab directory:

```bash
dotnet test
```

## Bonus (Optional)

- TODO-03 (optional): Print `((IConfigurationRoot)configuration).GetDebugView()` in a test and
  find which provider supplied each key.
- TODO-04 (optional): Replace `IOptions<StoreOptions>` with `IOptionsMonitor<StoreOptions>`
  in a test and change the underlying in-memory source. What does each interface return
  afterwards?
