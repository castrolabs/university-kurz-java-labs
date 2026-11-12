# Async Validation with Data Annotations

## Goal

Write validation rules that need to wait for a database or a service (an email already registered, a date range that depends on other data) with `AsyncValidationAttribute` and `IAsyncValidatableObject`, and see how the Minimal API turns the failures into a `400 application/problem+json`.

## Prerequisites

- Minimal APIs and data annotations (see the Input Validation in Minimal APIs concept)
- `async`/`await` and `CancellationToken`
- .NET 11 SDK (the lab targets `net11.0`; asynchronous validation does not exist in .NET 10)

## Task

`POST /users` takes a `Registration` and `POST /orders` takes an `Order`. The repository is a dependency injection service (`IUserRepository`). The starter has the types, but validation is off and the two rules do nothing yet. The tests use `WebApplicationFactory<Program>` with a fake repository that counts calls and records whether a lookup was cancelled. No Docker, no database.

## Instructions

Complete the following TODOs:

- TODO-00: Register the validation services in `Program.cs`.
- TODO-01: Implement `UniqueEmailAttribute.IsValidAsync` with the repository, taking it from the `ValidationContext`, and pass the cancellation token on.
- TODO-02: Implement `Order.ValidateAsync` so that an `End` before `Start` is reported against `End`.

Run the tests until they all pass. Two tests are about order: they show which rules run when another rule on the same property, or on the same object, already failed.

## Running the Lab

From the lab directory:

```bash
dotnet test
```

## Bonus (Optional)

- TODO-03 (optional): In `UniqueEmailAttribute`, replace the token with `CancellationToken.None` and see which test fails and why. What does a database query that keeps running after the client left cost you?
- TODO-04 (optional): `IsValid` and `Validate` throw `NotSupportedException`. Put a `Console.WriteLine` in each, post an invalid request, and confirm the Minimal API never calls them.
