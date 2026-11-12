# Controlling Time in Tests with TimeProvider and FakeTimeProvider (Solution)

Official solution for the `controlling-time-with-timeprovider` lab.

## What changed from the starter

- **Every test class starts its `FakeTimeProvider` at a fixed instant** (2026-01-05 09:00 UTC), not at
  `DateTimeOffset.UtcNow`. The expected values are then exact, and code that still reads the real clock
  fails at once instead of agreeing with a fake that happens to sit at "now".
- **Boundaries are tested on both sides.** Fourteen days minus one tick is still `Active`, fourteen days
  is `Expired`; the same for the 15-minute token and the 1-minute login window. A test that advances
  "well past" the limit (16 days, 2 minutes) passes even when the limit is wrong.
- **The 14 days and 15 minutes are written in the tests.** Reusing `TrialService.Length` would make the
  test agree with whatever the constant says.
- **`LoginThrottle`** uses a timer from `TimeProvider.CreateTimer`. `FakeTimeProvider.Advance` runs a
  due timer callback synchronously, on the thread that called `Advance`, so the next line already sees
  the reset counter.
- **`QuietHours`** is tested with a custom `UTC-03` zone from `TimeZoneInfo.CreateCustomTimeZone`, set
  with `SetLocalTimeZone`, so the result does not depend on the machine. The instant is given in UTC:
  `FakeTimeProvider` keeps the offset of the value it receives, so `GetUtcNow()` would otherwise return
  a `-03:00` offset.
- **`DeliveryRetryTests`** start the task, move the clock, and await at the end. In these tests the
  continuations after `Task.Delay(wait, time, token)` ran inside `Advance` (there is no
  `SynchronizationContext` in an MSTest test), so the attempt counter is checked after each step. The
  deadline is a `CancellationTokenSource(Deadline, time)`, so the hung attempt is cancelled by the fake
  clock too. `[Timeout(2000)]` turns the classic mistake (awaiting before advancing, which never returns)
  into a failure instead of a hung run.

## Bonus notes

- TODO-05 (optional), the forgotten clock. Making the nested `Entry.IsExpiredAt` read
  `DateTimeOffset.UtcNow` turns two token tests red with the fixed start. With the fake started at
  `DateTimeOffset.UtcNow` instead, those two go green and only the "expires exactly at 15 minutes" test
  still notices.
- Mutation check, run against this solution (MSTest 4.5.2, Microsoft.Extensions.TimeProvider.Testing
  10.11.0). Each change to `src/Accounts` was applied on its own and then reverted:

| Change to the production code | Tests that fail |
| --- | --- |
| `StatusOf` uses `<=` instead of `<` | `ATrialExpiresExactlyFourteenDaysAfterItStarts` |
| The trial lasts 15 days | `ATrialExpiresExactlyFourteenDaysAfterItStarts`, `TheRemainingTimeCountsDown` |
| Nested `Entry.IsExpiredAt` reads `DateTimeOffset.UtcNow` | `ATokenIsValidRightAfterItIsIssued`, `ATokenIsStillValidOneTickBeforeFifteenMinutes` |
| Token expiry uses `>` instead of `>=` | `ATokenExpiresExactlyFifteenMinutesAfterItIsIssued` |
| The throttle timer fires 1 s late | `TheWindowResetsExactlyWhenTheMinuteEnds` |
| The backoff calls `Task.Delay(wait, token)` (real time) | `TheAttemptsFollowTheBackoffOfOneTwoAndFourSeconds` |
| The deadline is `new CancellationTokenSource(Deadline)` (real time) | `AnAttemptThatHangsIsAbandonedAtTheTenSecondDeadline` |
| `QuietHours` reads `GetUtcNow` instead of `GetLocalNow` | two `[DataRow]` rows (21:59 and 06:59) |

And the weak versions, verified the same way:

| Weaker test | Result |
| --- | --- |
| The throttle test advances 2 minutes instead of testing the boundary | the late timer above goes unnoticed (16 of 16 pass) |
| The trial tests use `TrialService.Length` | the 15-day trial is caught only by the "remaining" test, because it hard-codes 11 days |
| The token tests start the fake at `DateTimeOffset.UtcNow` | the forgotten clock is caught by one test instead of two |
