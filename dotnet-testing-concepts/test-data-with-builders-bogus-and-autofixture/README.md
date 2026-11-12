# Test Data with Builders, Bogus and AutoFixture

## Goal

`src/Shop` is finished: a `DiscountPolicy` (premium 10%, orders of 500 or more another 5%) and an `OrderService`
that saves an order and e-mails the customer. Your job is to build the test data three ways and see what each
one says (or hides) about the test: a hand-written Test Data Builder, Bogus with a fixed seed, and AutoFixture
with AutoNSubstitute.

## Prerequisites

- MSTest basics and test doubles with NSubstitute
- .NET 10 SDK

## Instructions

Complete the following TODOs:

- TODO-00: `OrderBuilder`, with fixed defaults and one method per fact a test states.
- TODO-01: `DiscountPolicyTests` with the builder, including the boundary at exactly 500.
- TODO-02: `Fakes` (Bogus, seeded) and `BogusTests`: same seed gives the same data, another seed does not, and a
  bulk check that first proves the data covers both branches.
- TODO-03: `OrderServiceTests` with AutoFixture and `AutoNSubstituteCustomization`: `Freeze`, `Create`, and `with`
  expressions that pin only what the assertion depends on.

## Running the Lab

```bash
dotnet test
```

`dotnet restore` prints NU1608: `AutoFixture.AutoNSubstitute` 4.18.1 declares NSubstitute below 6.0.0, and the lab
uses NSubstitute 6.2.0. The tests run fine with it; the warning is the package's declared range, not a failure.

## Bonus (Optional)

- Remove `UseSeed` from one faker and run the bulk test 20 times. Does it ever fail? What would you see in the CI
  log when it does, and could you reproduce it?
- Change `Worth(500m)` to `Worth(499.99m)` in the boundary test and see which rule it stops testing.
