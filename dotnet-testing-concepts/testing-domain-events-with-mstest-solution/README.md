# Testing Domain Events with MSTest (Solution)

Official solution for the `testing-domain-events-with-mstest` lab.

## What changed from the starter

- **`OrderAggregateTests`** needs no infrastructure. `Assert.ContainsSingle` returns the only element
  and `Assert.IsInstanceOfType<OrderPlaced>` returns it typed, so the next lines read like the spec.
  A `FakeTimeProvider` makes the expected timestamp exact. The rejected totals are one test with two
  `[DataRow]` rows. `Order.Place` throws `ArgumentOutOfRangeException`, so `Assert.ThrowsExactly`
  must name that type; `Assert.Throws<ArgumentException>` would also accept it, because it allows
  derived types.
- **`TestDatabase`** opens one SQLite in-memory connection per test. MSTest creates a new instance of
  the test class for every test method, so a `readonly` field set by the constructor is private to its
  test, and `Dispose` runs after it. That is what makes `[assembly: Parallelize(Scope = ExecutionScope.MethodLevel)]`
  (in `MSTestSettings.cs`; MSTest is sequential until an assembly opts in) safe here. A `static` database
  would be shared by tests running at the same time. `[TestInitialize]` and `[TestCleanup]` do the same
  job, but cannot assign a `readonly` field; reach for them when the setup must be async.
- **The fakes** (`RecordingHandler`, `ExplodingHandler`) are hand-written. They are two lines each, and
  the test reads without a mocking library.
- **`DomainEventsInterceptorTests`** and **`OutboxTests`** always read the result back through
  `TestDatabase.Plain()`, a different context over the same connection. The failed context still tracks
  the order it tried to save, so asserting through it could pass for the wrong reason.
- **`LoyaltyConsumerTests`** calls the consumer directly, so there is no polling and no `Thread.Sleep`.
  The `TestContext` arrives through the constructor, and its `CancellationToken` is passed to the
  consumer instead of `CancellationToken.None`, so a test that times out also cancels the work it started.

## Bonus notes

- `dotnet test --filter "TestCategory=Integration"` runs the 8 tests that use SQLite;
  `--filter "TestCategory!=Integration"` runs the 4 aggregate tests in a few milliseconds.
- Mutation check, run against this solution. Each change below to `src/Ordering` was tried on its own:

| Change to the production code | Tests that fail |
| --- | --- |
| Interceptor stops clearing the events | `EventsAreClearedSoASecondSaveDoesNotDispatchThemAgain` |
| `Order.Place` does not validate the total | `AnOrderWithoutAPositiveTotalIsRejected` (both rows) |
| `Order.Place` raises no event | 7 tests, from the aggregate to the outbox |
| Dispatch moved to `SavedChangesAsync` (after the commit) | `AHandlerThatThrowsRollsBackTheOrder`, `NoOrderMeansNoOutboxRowWhenTheSaveFails`, `TheOutboxRowIsSavedInTheSameTransactionAsTheOrder` |
| `OrderPlacedHandler` calls `SaveChangesAsync` itself | `NoOrderMeansNoOutboxRowWhenTheSaveFails` |
| `LoyaltyConsumer` ignores the inbox | `ADuplicateDeliveryOfTheSameMessageAwardsPointsOnlyOnce` |
| `Customer.AwardPoints` overwrites instead of adding | `DifferentMessagesForTheSameCustomerAccumulate` |

- What no test here can see: a crash between the commit and the dispatch. That window only exists when
  the dispatch is after the commit, and it is the reason the outbox exists.
