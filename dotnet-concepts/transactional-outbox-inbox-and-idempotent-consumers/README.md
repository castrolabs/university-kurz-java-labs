# Transactional Outbox, Inbox and Idempotent Consumers

## Goal

Make an order and its integration event atomic with a transactional outbox, relay the outbox to a
broker from a `BackgroundService` with retries, backoff and a dead-letter state, and consume the
message idempotently with an inbox, so that at-least-once delivery has an effect exactly once.

## Prerequisites

- EF Core basics (`SaveChanges` is one transaction)
- `BackgroundService` and `Channel<T>`
- .NET 10 SDK

## Task

Placing an order must produce an `OrderPlaced` message for a shipping consumer. The broker is a
`Channel<T>` (`InMemoryBroker`) that can be told to fail. The database is SQLite in a temporary file.
The tests inject the faults that really happen: a crash between the commit and the publish, a crash
between the publish and the "published" mark, a broker outage, a consumer that throws, a poison
message.

```text
OrderService.PlaceOrderAsync -> order + OutboxMessage in ONE SaveChanges
OutboxDispatcher             -> reads pending rows in order, publishes, marks, backs off, dead-letters
ShippingConsumer             -> Shipment + InboxMessage in ONE SaveChanges, duplicates are no-ops
ConsumerRunner               -> ack, redeliver, or dead-letter after max deliveries
```

The model, the `DbContext`, the broker and the two `BackgroundService` shells are done. The logic is
not: the methods below throw `NotImplementedException`.

## Instructions

Complete the following TODOs:

- TODO-00: `OrderService.PlaceOrderAsync`: order and outbox row in the same `SaveChanges`.
- TODO-01: `OutboxDispatcher.DispatchBatchAsync`: pending and due rows, in `Seq` order, publish then mark.
- TODO-02: `OutboxDispatcher.RecordFailure`: attempts, exponential backoff, dead-letter.
- TODO-03: `ShippingConsumer.HandleAsync`: effect and inbox row in one transaction, duplicates are no-ops.
- TODO-04: `ConsumerRunner.ProcessAsync`: redeliver on failure, dead-letter after `maxDeliveries`.

The last test (TODO-05) runs both hosted services together with random-looking faults and needs no
code of its own: it passes when the five pieces work together.

Run the tests until they all pass. Pay attention to the test that delivers a message twice because the
"published" mark failed: it is the reason the consumer needs an inbox.

## Running the Lab

From the lab directory:

```bash
dotnet test
```

## Bonus (Optional)

- Remove the `AnyAsync` fast path from the consumer and run the tests again. What still prevents the
  duplicate effect?
- Change `DispatchBatchAsync` to mark the row first and publish after. Which failure test breaks, and
  what does that tell you about at-most-once?
- Run two `OutboxDispatcher` instances on the same database at the same time. What do you see in the
  broker, and what makes the final state still correct?
