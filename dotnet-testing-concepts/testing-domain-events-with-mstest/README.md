# Testing Domain Events with MSTest

## Goal

The ordering module from the Domain Events lab is already fully implemented. Your job is to write the
MSTest tests that prove it works: the aggregate on its own, the `SaveChanges` interceptor with a real
EF Core pipeline, the outbox that must commit together with the order, and a consumer that must survive
duplicate delivery.

## Prerequisites

- Domain Events and Consistency Boundaries (the concept and its lab)
- Basic MSTest: `[TestClass]`, `[TestMethod]`
- .NET 10 SDK

## Task

Read `src/Ordering` first (`Model.cs`, `Dispatching.cs`, `Handlers.cs`). Then open the test project.
Every test method is already declared with a hint, and fails with `TODO-NN: write this test` until you
write it. Some tests need helpers (`RecordingHandler`, `ExplodingHandler`) that are only shells for now.

This is not about copying the article. Several tests only prove something if you assert in the right
place: a test that reads back through the same `DbContext` that just failed can pass for the wrong reason.

## Instructions

Complete the following TODOs in `tests/Ordering.Tests`:

- TODO-00: `OrderAggregateTests`: the event raised by `Order.Place`, its timestamp with a
  `FakeTimeProvider`, and the rejected totals with `[DataRow]`.
- TODO-01: `TestDoubles.cs`: write `RecordingHandler` and `ExplodingHandler`.
- TODO-02: `DomainEventsInterceptorTests`: a fresh `TestDatabase` per test, in a `readonly` field,
  disposed after the test (`IDisposable`).
- TODO-03: `DomainEventsInterceptorTests`: dispatch, clearing of events, and rollback when a handler throws.
- TODO-04: `OutboxTests`: the outbox row is saved with the order, and neither exists after a failed save.
- TODO-05: `LoyaltyConsumerTests`: points are awarded once per message, and different messages accumulate.

Run the tests until they all pass.

## Running the Lab

From the lab directory:

```bash
dotnet test
```

## Bonus (Optional)

- TODO-06 (optional): You tagged the EF Core tests with `[TestCategory("Integration")]`. Run only those
  with `dotnet test --filter "TestCategory=Integration"`, and everything else with
  `--filter "TestCategory!=Integration"`.
- TODO-07 (optional): Mutation check. Change the production code on purpose and see whether your suite
  notices. Remove `aggregates.ForEach(a => a.ClearDomainEvents())` from the interceptor, then move the
  dispatch to `SavedChangesAsync`, then make `LoyaltyConsumer` skip the inbox check. Each change should
  turn at least one of your tests red. A change that leaves everything green means a missing test.
