# Diagnosing Flaky, Crashing and Hanging Tests with Microsoft.Testing.Platform

## Goal

`src/Dispatch` is a small dispatch desk: a process-wide ticket counter, an inbox that the depot posts to,
and a route that is a chain of stops. The code is finished. `tests/Dispatch.Tests/SuspectTests.cs` holds four
tests that fail the way suites fail in CI: one passes alone and fails in the full run, one never finishes,
one kills the test process. Your job is to give the test project the Microsoft.Testing.Platform (MTP)
extensions that diagnose each one, read what they produce, and then write the fixed versions of the tests.

The suspects are switched off unless `DISPATCH_SUSPECTS=1`, so a plain `dotnet test` never hangs or crashes.

## Prerequisites

- Microsoft.Testing.Platform, dotnet test and Code Coverage in CI
- MSTest Fundamentals: Lifecycle and TestContext
- .NET 10 SDK (10.0.100 or later). The lab was also run on the .NET 11 SDK (11.0.100); where the output
  differs, the README says so.

## Task

Run `dotnet test` once before you change anything. Nine tests fail: three in `RunnerTests` (the project
does not have the extensions yet) and six in `FixedTests` (not written yet). The four suspects are skipped.
`global.json` already selects the MTP mode of `dotnet test`.

## Instructions

Complete the following TODOs:

- TODO-00: Add the extensions. `MSTest.Sdk` loads only the `Default` set (code coverage and TRX). In
  `tests/Dispatch.Tests/Dispatch.Tests.csproj`, add either a profile or one property per extension:

  ```xml
  <TestingExtensionsProfile>AllMicrosoft</TestingExtensionsProfile>
  ```

  ```xml
  <EnableMicrosoftTestingExtensionsRetry>true</EnableMicrosoftTestingExtensionsRetry>
  <EnableMicrosoftTestingExtensionsHangDump>true</EnableMicrosoftTestingExtensionsHangDump>
  <EnableMicrosoftTestingExtensionsCrashDump>true</EnableMicrosoftTestingExtensionsCrashDump>
  <EnableMicrosoftTestingExtensionsHtmlReport>true</EnableMicrosoftTestingExtensionsHtmlReport>
  ```

  `RunnerTests` turns green when the retry, hang dump and crash dump assemblies are loaded.
- TODO-01: Flaky. Run the two order-dependent suspects, then rerun with a retry, and read the summary:

  ```bash
  DISPATCH_SUSPECTS=1 dotnet test --filter "FullyQualifiedName~SuspectTests&Name!=TheDepotAnswers&Name!=ARouteCanBeCounted"
  DISPATCH_SUSPECTS=1 dotnet test --filter "FullyQualifiedName~SuspectTests&Name!=TheDepotAnswers&Name!=ARouteCanBeCounted" --retry-failed-tests 3
  ```

  The second run exits with 0. Say why the retry passed, then write `TheFirstTicketOfTheDayIsOne` and
  `TicketsAreSequential` in `FixedTests` so that they pass in any order, with `[TestInitialize]` and
  `[TestCleanup]`.
- TODO-02: Hang. Run the hanging suspect with a hang dump and a short timeout:

  ```bash
  DISPATCH_SUSPECTS=1 dotnet test --filter "Name=TheDepotAnswers" --hangdump --hangdump-timeout 10s --hangdump-type Mini
  ```

  Read the test name that the output lists, the exit code, and the files under `TestResults/`. Then write
  the two `FixedTests` that use `Inbox.TryReceive`.
- TODO-03: Crash. Run the crashing suspect twice, without and with `--crashdump`:

  ```bash
  DISPATCH_SUSPECTS=1 dotnet test --filter "Name=ARouteCanBeCounted"
  DISPATCH_SUSPECTS=1 dotnet test --filter "Name=ARouteCanBeCounted" --crashdump --crashdump-type Mini
  ```

  Compare the two exit codes. Open the `crash.sequence.log` that the second run writes: which test has a
  `STARTED` line and no `ENDED` line? Then write the two `FixedTests` that use `Route.Length(stop, limit)`.
- TODO-04: Reports. Run the two order-dependent suspects with `--retry-failed-tests 3 --report-trx
  --report-html`. Open the TRX in `TestResults/` and the one under `TestResults/Retries/.../merged/`. How many
  results does each one hold for `TheFirstTicketOfTheDayIsOne`?

Run `dotnet test` until every test passes.

## Running the Lab

From the lab directory:

```bash
dotnet test
```

## Bonus (Optional)

- TODO-05 (optional): MSTest has its own `[Retry(3)]` attribute. Put it on `TheFirstTicketOfTheDayIsOne` in
  `SuspectTests` (a temporary edit) and run the two order-dependent suspects without `--retry-failed-tests`.
  Compare the summary and the exit code with TODO-01, and ask where the retried attempt runs.
- TODO-06 (optional): Put `[Timeout(2000)]` on `TheDepotAnswers` (a temporary edit) and run it without
  `--hangdump`. What does the failure say, and does the process exit?
- TODO-07 (optional): `--timeout 3s` before `--` and `-- --timeout 3s` after it are different options. Run a
  test that sleeps 20 seconds with each one and compare how long the run takes and what the exit code is.
