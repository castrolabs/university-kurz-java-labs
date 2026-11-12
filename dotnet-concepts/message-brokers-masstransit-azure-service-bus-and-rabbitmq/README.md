# Message Brokers: MassTransit, Azure Service Bus and RabbitMQ

## Goal

Use MassTransit with the in-memory transport and its test harness to see the broker concepts at work: publish
versus send, retry, a fault after the retries are exhausted, an idempotent consumer, and the consumer outbox.
Nothing here needs RabbitMQ or Azure: the transport is the only thing that changes in production.

## Prerequisites

- `IConsumer<T>` and dependency injection basics
- At-least-once delivery and idempotency (see the transactional outbox concept)
- .NET 10 SDK

## License note

The lab pins **MassTransit 8.5.12**, the last release under Apache-2.0 (checked on the NuGet package). MassTransit v9 is
a commercial product and needs a license. Do not bump the major version here. For production, read the current license
terms and decide between v9, staying on v8, or an alternative.

## Task

```text
OrderApi.SubmitAsync          -> Publish an OrderSubmitted event (many subscribers possible)
OrderApi.RequestShipmentAsync -> Send a ShipOrder command to one queue
BillingConsumer               -> inbox check, charge, publish PaymentCaptured
BillingFaultConsumer          -> receives Fault<OrderSubmitted> after the retries run out
BillingConsumerDefinition     -> retry and in-memory outbox for the billing endpoint
```

The contracts, the `Ledger` (what the consumers did), `ShippingConsumer`, `AuditConsumer`, the DI setup and the
tests are done. Methods marked with a TODO throw `NotImplementedException` or are missing configuration.

## Instructions

- TODO-00: `BillingConsumer.Consume`: reject negative totals, record the charge, publish `PaymentCaptured`.
- TODO-01: `OrderApi.SubmitAsync`: publish `OrderSubmitted`, optionally with a fixed `MessageId`.
- TODO-02: `OrderApi.RequestShipmentAsync`: send `ShipOrder` to `queue:shipping`.
- TODO-03: `BillingConsumer`: skip a message id that was already processed, mark it as the last step.
- TODO-04: `BillingConsumerDefinition`: retry 3 times, 10 ms apart.
- TODO-05: `BillingFaultConsumer`: record the order in `DeadLetters`.
- TODO-06: `BillingConsumerDefinition`: add the in-memory outbox after the retry.

## Verification

`dotnet test` must be green, 8 of 8. The starter compiles and all 8 tests fail until you do the TODOs.

## Optional challenges

- Remove `UseInMemoryOutbox`. Which test fails, and how many `PaymentCaptured` were published?
- Remove the `IsProcessed` check. Which test fails?
- Replace `x.UsingInMemory(...)` with `UsingRabbitMq(...)` in `Setup.cs` (do not run it): what else must exist at runtime?
