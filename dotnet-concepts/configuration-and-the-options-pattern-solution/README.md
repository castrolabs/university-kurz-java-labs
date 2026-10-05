# Configuration and the Options Pattern (Solution)

Official solution for the `configuration-and-the-options-pattern` lab.

## What changed from the starter

- `Build` adds the sources in order: base JSON, environment JSON (only when provided),
  environment variables with the `GSLAB_` prefix, then command-line arguments. Providers are
  consulted from the last to the first, so the command line wins. Environment variables use
  `__` as the hierarchy separator (`GSLAB_Store__PageSize`) because `:` is not valid in
  every shell.
- `GetRequiredConnectionString` turns a silent `null` into an exception that names the
  missing key.
- `AddStoreOptions` is `AddOptions<T>().Bind(section).ValidateDataAnnotations().ValidateOnStart()`.
  `ValidateOnStart` runs the checks when the host starts, so a missing `Name` or a
  `PageSize` of 500 stops the application with an `OptionsValidationException` instead of
  failing on the first request that reads the options.
- `PageSize` keeps its default of 20 when the key is absent: binding only overwrites what is
  present.

## Bonus notes

- `GetDebugView()` lists every key with the provider that supplied the winning value, which
  is the fastest way to debug "my setting does not take".
- `IOptions<T>` never reloads; `IOptionsMonitor<T>` returns the current value after the
  source changes.

## Running

```bash
dotnet test
```
