# Decimal Floating Point: Decimal32, Decimal64 and Decimal128

## Goal

Use the IEEE 754 decimal types of .NET 11 on purpose: round to cents, convert between them and `decimal`, see the quantum (`1.0` versus `1.00`), read the BID bits, and get the overflow and JSON behavior you want.

## Prerequisites

- C# numeric types and rounding (`numeric-types-and-binary-representation`, `numeric-conversions-rounding-and-overflow`)
- Generic math (`INumber<T>`)
- the .NET 11 SDK (the projects target `net11.0`, and the types live in `System.Numerics`)

## Task

`Ieee` is stubbed. Each method has one place where the obvious code is wrong: a rounding mode, a conversion that throws, a value that looks equal but is not the same representation, an overflow that `checked` does not catch.

## Instructions

Complete the following TODOs:

- TODO-00: `RoundToCents`: `Decimal64.Quantize` to a 0.01 quantum, ties to even.
- TODO-01: `RoundHalfAwayToCents`: `Decimal64.Round` with an explicit `MidpointRounding`.
- TODO-02: `ToDecimalOrNull`: `Decimal128` to `decimal`, or null when it does not fit.
- TODO-03: `FitsInDecimal32`: a round trip through the 7-digit type.
- TODO-04: `SumTenths<T>`: generic math, exact for the decimal types and not for `double`.
- TODO-05: `SameValueDifferentQuantum`: `1.0` and `1.00`.
- TODO-06: `ToBid`: the 64 bits of the value as hex.
- TODO-07: `FromBid`: the inverse.
- TODO-08: `MultiplyChecked`: throw `OverflowException` when finite operands overflow to Infinity.
- TODO-09: `ToJson`: a JSON number that keeps the trailing zeros.
- TODO-10: `FromJson`: read it back.

Run the tests until they all pass.

## Running the Lab

From the lab directory:

```bash
dotnet test
```

## Bonus (Optional)

- Run `SumTenths<Decimal32>` and `SumTenths<Decimal128>` and compare with `SumTenths<decimal>`.
- Time 20 million additions of `double`, `decimal` and `Decimal64` in a Release build and compare with the numbers in the concept.
