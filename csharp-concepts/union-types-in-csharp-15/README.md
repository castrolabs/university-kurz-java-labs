# Union Types in C# 15

Lab for the `union-types-in-csharp-15` concept.

## Scenario

A payment gateway answers with one of three things: `Approved`, `Declined` or `Pending`. You model the answer as a C# 15 `union`, write switches the compiler checks for exhaustiveness, handle the `default` value, serialize a union with System.Text.Json and finish with a hand-written union that does not box an `int`.

## What you will do

1. **Exhaustive switch (TODO-00).** `Payments.Describe` switches over `PaymentResult` with one arm per case type and no `_`. The `.csproj` turns CS8509 and CS8655 into errors: delete an arm and the project stops compiling.
2. **The default value (TODO-01).** `default(PaymentResult)` holds a null `Value`. `DescribeSafe` adds a `null` arm. The test for `Describe(default)` expects `SwitchExpressionException`.
3. **JSON (TODO-02).** `ToJson` and `FromJson` for `IntOrString`: the case is written without an envelope, and a token that fits no case type throws `JsonException`.
4. **No boxing (TODO-03).** Complete `IntOrText` with `Value`, `HasValue` and two `TryGetValue` methods. The test measures `GC.GetAllocatedBytesForCurrentThread` and expects zero bytes.

## Optional challenges

- Remove the `Pending` arm from `Describe` and read the CS8509 message.
- Change `IntOrText.TryGetValue(out int)` to always return false and see which test notices.

## Prerequisites

Pattern matching with `switch` expressions and the **.NET 11 SDK (C# 15)**. The projects target `net11.0`; `union` needs types from the .NET 11 runtime, so an older SDK or target framework does not build this lab.

## Running

```
dotnet test
```

Every stubbed member throws `NotImplementedException`, so the red tests are your checklist.
