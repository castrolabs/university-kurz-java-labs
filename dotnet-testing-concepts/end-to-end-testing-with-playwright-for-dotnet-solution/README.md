# End-to-End Testing with Playwright for .NET (Solution)

Official solution for the `end-to-end-testing-with-playwright-for-dotnet` lab. Run `dotnet build`, install
Chromium (see the starter README), then `dotnet test`: 6 tests pass.

## What changed from the starter

- **`TasksPage`** finds everything by role, label, text or test id: `GetByRole(AriaRole.List, Name = "Task list")`,
  `GetByLabel("New task", Exact = true)`, `GetByTestId("open-count")`. No CSS selector, so a restyle cannot break it.
- **Web-first assertions** (`Expect(locator).ToHaveCountAsync(1)`) retry for 5 s. The list arrives 300 ms after
  navigation and nothing in the tests sleeps.
- **The empty-title test waits for "0 open" first.** The list is hidden until the first load, so
  `ToHaveCountAsync(0)` alone would pass before the page had loaded anything.
- **The delete test waits for the first item before adding the second.** Without that wait the run failed:
  the page clears the input when the first POST returns, which erased the second title.
- **`Microsoft.Playwright.MSTest` is not used.** Its 1.64.1 build targets the MSTest 2.x assembly identity; with
  MSTest 4.5.3 it compiles (warning MSTEST0082) and discovers 0 tests. It does work with MSTest 3.11.1.
- **Traces**: `E2ETest` records a trace per test and saves `<TestName>.zip` only when the outcome is not Passed.
