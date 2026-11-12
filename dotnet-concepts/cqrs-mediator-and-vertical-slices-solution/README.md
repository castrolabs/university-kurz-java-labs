# CQRS, a Hand-Written Mediator and Vertical Slices

## Goal

Build the core of what MediatR-style libraries do, in about 40 lines: a mediator that finds a
handler for a request and wraps it in pipeline behaviors (validation, transaction), then use it
from a Minimal API organized as vertical slices (one folder per feature, not one folder per layer).

## Prerequisites

- C# generics and interfaces
- Minimal APIs and dependency injection (open generic registrations)
- .NET 10 SDK

## Task

```text
src/Shop.Api/
  Mediator/            -> IRequest, ICommand, IQuery, handlers, behaviors, the Mediator itself
  Features/Orders/     -> the slice: PlaceOrder (command), GetOrder (query), routes
```

The dispatch plumbing (finding the wrapper per request type, scanning handlers) is done. Four
pieces are missing, each marked `TODO-NN`:

1. **TODO-00** (`Mediator/Mediator.cs`): compose the pipeline. The behavior registered first must be the outermost.
2. **TODO-01** (`Mediator/Behaviors.cs`, `ValidationBehavior`): collect all validation errors, throw `ValidationException`, and never run the handler when there are errors.
3. **TODO-02** (`Mediator/Behaviors.cs`, `TransactionBehavior`): begin, run, commit; on exception roll back (never commit) and rethrow the original exception.
4. **TODO-03** (`Features/Orders/OrdersEndpoints.cs`): `POST /orders` sends `PlaceOrder`, answers 201 with `Location`, or 400 with the validation errors.

Then answer: why is `TransactionBehavior` skipped for `GetOrder` even though it is registered
for every request (look at its generic constraint)?

## Optional challenges

- **TODO-04:** add `CancelOrder` as a second command in its own file. What did you have to touch outside the slice?
- **TODO-05:** call the handler directly in a test, without the mediator. What did the mediator add, and what did it cost?
- **TODO-06:** swap the order of `ValidationBehavior` and `TransactionBehavior` in `Program.cs`. What changes for an invalid request?

## How to run

`dotnet test`. In the starter state the tests fail with `NotImplementedException` carrying the TODO number.
