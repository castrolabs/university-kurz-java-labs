# End-to-End Testing with Playwright for .NET

## Goal

`src/Tasks.Web` is a small, finished ASP.NET Core app: a minimal API and one static page that lists, adds,
completes and deletes tasks. The list is slow on purpose (300 ms). Your job is to write the Playwright end-to-end
tests that drive it in a real Chromium, with locators a user would use and web-first assertions that wait.

The plumbing is already there: `TestEnvironment` starts the app on a real Kestrel socket
(`WebApplicationFactory.UseKestrel(0)`) and one headless Chromium for the run, and `E2ETest` gives every test a
fresh browser context, an empty store and a trace that is saved when the test fails. You write the Page Object
and the tests.

## Prerequisites

- MSTest basics and `WebApplicationFactory` (see the integration testing lab)
- .NET 10 SDK, and a few hundred MB of disk for Chromium (installed once, see below)

## Why `Microsoft.Playwright` and not `Microsoft.Playwright.MSTest`

`Microsoft.Playwright.MSTest` 1.64.1 is compiled against the old MSTest 2.x assembly identity
(`Microsoft.VisualStudio.TestPlatform.TestFramework`). MSTest 4 ships `MSTest.TestFramework`, so a `PageTest`
subclass compiles with warning MSTEST0082, but its `[TestInitialize]` is ignored and the run discovers 0 tests.
This lab uses the core `Microsoft.Playwright` package and the small `E2ETest` base class instead.

## Install the browser once

Build, then run the script the package puts next to the test assembly:

```bash
dotnet build
pwsh tests/Tasks.E2E.Tests/bin/Debug/net10.0/playwright.ps1 install chromium
```

Without PowerShell, a console app that calls `Microsoft.Playwright.Program.Main(["install", "chromium"])` does the
same. Set `PLAYWRIGHT_BROWSERS_PATH` to keep the browsers outside the default cache.

## Instructions

Complete the following TODOs:

- TODO-00: `TasksPage`, the Page Object with role/label/test id locators.
- TODO-01: the loading state, with auto-wait.
- TODO-02: add a task.
- TODO-03: empty title: the alert, and a list that is empty for the right reason.
- TODO-04: mark a task as done.
- TODO-05: delete a task, without the typing race.
- TODO-06: the task survives a reload.

## Running the Lab

```bash
dotnet test
```

A failed test writes `<TestName>.zip` next to the test assembly. Open it with the trace viewer:
`pwsh tests/Tasks.E2E.Tests/bin/Debug/net10.0/playwright.ps1 show-trace <file>.zip`.

## Bonus (Optional)

- Break the page on purpose (rename the "Add task" button in `index.html`) and see that only `TasksPage` needs
  to change.
- Run the suite in headed mode (`Headless = false` in `TestEnvironment`) with `SlowMo = 300` to watch it.
