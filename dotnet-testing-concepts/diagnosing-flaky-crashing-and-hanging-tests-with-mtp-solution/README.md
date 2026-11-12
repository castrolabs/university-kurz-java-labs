# Diagnosing Flaky, Crashing and Hanging Tests with Microsoft.Testing.Platform (Solution)

Official solution for the `diagnosing-flaky-crashing-and-hanging-tests-with-mtp` lab.

## What changed from the starter

- **The extensions.** `tests/Dispatch.Tests/Dispatch.Tests.csproj` sets `EnableMicrosoftTestingExtensionsRetry`,
  `...HangDump`, `...CrashDump` and `...HtmlReport` to `true`. `TestingExtensionsProfile` set to
  `AllMicrosoft` also works and adds more (Azure DevOps, GitHub Actions, hot reload). The TRX report is
  already in the `Default` profile. Every extension package is 2.5.2 with `MSTest.Sdk` 4.5.2.
- **`FixedTests`** has the three causes removed instead of hidden: a `[TestInitialize]` and a `[TestCleanup]`
  that call `TicketCounter.Reset()`, `Inbox.TryReceive` with a deadline, and `Route.Length(stop, limit)`.

## What the diagnosis tools print

Measured with `DISPATCH_SUSPECTS=1` on SDK 11.0.100 (running the `net10.0` test project on the .NET 11
runtime) and, for the retry, also on SDK 10.0.100.

- **Flaky.** `TheFirstTicketOfTheDayIsOne` runs after `TicketsAreSequential` in the full run and sees ticket 3.
  Alone it passes. With `--retry-failed-tests 3` the failed test is rerun in a new process, where the counter
  starts at 0, so it passes on the second attempt: exit 0, and on SDK 11 the summary shows
  `flaky: 1 (passed after retry)` and `TheFirstTicketOfTheDayIsOne failed -> passed (2 attempts)`. SDK 10.0.100
  prints `total: 2 (+1 retried)` and no `flaky` line. The retry hid a real defect: the test depends on the
  order of the others.
- **MSTest `[Retry(3)]` is different.** It retries inside the same process, so the counter keeps growing: the
  four attempts saw 3, 4, 5 and 6, all failed, and the run exited with 2.
- **Hang.** `--hangdump --hangdump-timeout 5s --hangdump-type Mini` printed
  `The following tests were still running when dump was taken` with `[00:00:05] TheDepotAnswers`, wrote
  `TestResults/Dispatch.Tests_<pid>_hang.dmp` (about 40 MB as a Mini dump) and `..._hang.log`, killed the
  process (`exit code is '137'`) and exited with 7 (the test host exited unexpectedly).
- **Crash.** Without `--crashdump` the output is `Stack overflow.` with a trace that repeats
  `Dispatch.Route.Length` and the run exits with 134, with no test name in the summary. With
  `--crashdump --crashdump-type Mini` the run exits with 7, lists `ARouteCanBeCounted` as still running and writes
  the dump and `Dispatch.Tests_<id>_crash.sequence.log`. The log has a `STARTED` and an `ENDED` line per
  test; the last line is `STARTED ... ARouteCanBeCounted` with no `ENDED`.
- **Reports.** The top-level `TestResults/Dispatch.Tests_net10.0_arm64.trx` of the retried run holds one result
  for `TheFirstTicketOfTheDayIsOne` (Passed): the failure is not there. The TRX under
  `TestResults/Retries/<id>/1/merged/` holds three results (`counters total="3" passed="2" failed="1"`): the
  failed attempt and the passed one. `--report-html` writes the same pair of files.

## Bonus notes

- `[Timeout(2000)]` on a test that sleeps 20 s: `Test 'TimeoutAttribute' timed out after 2000ms`, a normal
  failed test (exit 2), and the process exits in about 3 seconds.
- `--timeout 3s` before `--` did not stop a 20 s test: the run took 20 s, the test passed, and the exit
  code was 3 with `Test run summary: Aborted`. With two test projects and `--max-parallel-test-modules 1`,
  the second module never started (the summary counted only the first). `-- --timeout 3s` after the
  separator killed the test host after 3 s: exit 3, `total: 0`.
