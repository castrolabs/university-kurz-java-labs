# Testing Blazor Components with bUnit and MSTest (Solution)

Official solution for the `testing-blazor-components-with-bunit` lab.

## What changed from the starter

- **The test classes inherit `BunitContext`** (bUnit 2.15.2). In bUnit 1.x the class was called
  `TestContext`, and a test file that imports both namespaces does not compile:
  `CS0104: 'TestContext' is an ambiguous reference between 'Bunit.TestContext' and
  'Microsoft.VisualStudio.TestTools.UnitTesting.TestContext'`. bUnit 2 renamed it. MSTest creates one instance
  per test and disposes it, so each test gets its own renderer, services and JSInterop.
- **`CounterTests`** pass parameters and an `EventCallback` with `Render<Counter>(p => p.Add(...))` and
  compare the status paragraph with `MarkupMatches`, which ignores whitespace and attribute order.
- **`NewTaskFormTests`** select inputs by `id` (`#title`, `#priority`), fill them with `Change`, and send the
  form with `Find("form").Submit()`. Each test checks both sides: the message is shown and `OnCreate` was or
  was not raised.
- **`TaskListTests` call `AddAuthorization()` in the constructor**, for every test. `TaskList` contains an
  `<AuthorizeView>`, and without it rendering fails with "There is no registered service of type
  'Microsoft.AspNetCore.Authorization.IAuthorizationPolicyProvider'". `AddAuthorization` also cascades the
  authentication state, so no `CascadingAuthenticationState` is needed.
- **The loading tests wait.** After `store.Release()` the component renders again a moment later, not inside `Release()`.
  `Assert.HasCount(2, cut.FindAll("li"))` right after `Release()` fails; `cut.WaitForAssertion(...)` retries
  on every render until it passes or one second has gone by (then `WaitForFailedException`). The empty-store
  test waits for the message with `WaitForState`, because "no `<li>`" is also true while loading.
- **JSInterop stays strict.** `Setup<bool>("confirm", "Delete 'Buy milk'?")` matches the exact arguments;
  a missing setup throws `JSRuntimeUnhandledInvocationException`, whose message prints the setup to add.
  `SetupVoid` plus `VerifyInvoke("taskBoard.announce")` proves the announcement happened. `Loose` mode would
  answer `default` for every call and hide whether the component calls JavaScript at all.
- **Selectors follow what the user sees**: `aria-label` for the checkbox and delete buttons, the text for
  "Clear completed", `li span` for titles. No test uses `.done` or `.text-muted`.
- **`AddCascadingValue(settings)`** wraps the component in a `CascadingValue`, as a parent would.

## Bonus notes

- TODO-06: changing the class `"task done text-muted"` to `"task is-complete"` leaves all 14 tests green. A
  test written as `cut.FindAll("li.done")` fails on the same change.
- Mutation check, run against this solution. Each change to `src/TaskBoard` was tried on its own, then reverted:

| Change to the production code | Tests that fail |
| --- | --- |
| `OnInitializedAsync` awaits the store but never assigns `tasks` | 8 `TaskListTests`, including `ShowsLoadingUntilTheStoreAnswers` and `AnEmptyStoreShowsTheEmptyMessage` |
| `DeleteAsync` ignores the answer of `confirm` | `DecliningTheDeleteKeepsTheTask` |
| `ToggleAsync` does not call `SaveAsync` | `TickingTheCheckboxMarksTheTaskDoneAndSavesIt` |
| `Roles="lead"` removed from `AuthorizeView` | `OnlyALeadSeesClearCompleted` |
| `HideCompleted` ignored | `HideCompletedFromTheCascadingSettingsHidesDoneTasks` |
| `AddAsync` no longer calls `taskBoard.announce` | `AddingATaskSavesItShowsItAndAnnouncesIt` |
| `AddAsync` does not trim the title | `AddingATaskSavesItShowsItAndAnnouncesIt` |
| `Counter` disables at `count > Max` | `AtTheLimitTheButtonIsDisabledAndTheCallbackGetsTheCount` |
| `<DataAnnotationsValidator />` removed | `SubmittingWithoutATitleShowsTheMessageAndCreatesNothing`, `APriorityOutOfRangeIsRejected` |
| The form is not reset after a valid submit | `AValidSubmitRaisesOnCreateAndClearsTheForm` |
| CSS class renamed (not a behavior change) | none, as it should be |

- The trap, measured: a test that renders a held store, calls `Release()` and asserts
  `Assert.IsEmpty(cut.FindAll("li"))` right away passes, and it still passes with the first mutation above,
  where the list never leaves "Loading tasks...".
- What bUnit cannot tell you: bUnit renders in memory with its own renderer. It can emulate a render mode
  (`SetAssignedRenderMode`, `SetRendererInfo`) so a branch on `RendererInfo.IsInteractive` is testable, but it
  does not run a Blazor Server circuit, WebAssembly or prerendering, real JavaScript, CSS and layout, or a
  browser's navigation. Use Playwright against the running app for those.
