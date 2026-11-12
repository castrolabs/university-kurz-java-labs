# Decimal Floating Point: Decimal32, Decimal64 and Decimal128 (Solution)

Official solution for the `decimal-floating-point-decimal32-decimal64-decimal128` lab.

## What changed from the starter

- `RoundToCents` uses `Decimal64.Quantize(amount, 0.01)`: the result has exactly two decimal places (`19.90`) and ties go to even. `Round(x, 2)` would leave `19.9`.
- `RoundHalfAwayToCents` passes `MidpointRounding.AwayFromZero`; the default is ties to even.
- `ToDecimalOrNull` catches the `OverflowException` of the explicit conversion; `Decimal128.MaxValue`, infinities and NaN all fail that way.
- `FitsInDecimal32` converts to `Decimal32` and back and compares: more than 7 digits are rounded, and a huge exponent becomes Infinity.
- `SumTenths<T>` is written once with `INumber<T>`; ten tenths is exactly 1 in `decimal` and `Decimal64`, and 0.9999999999999999 in `double`.
- `SameValueDifferentQuantum` combines `==` with `HaveSameQuantum`: equality ignores the exponent, the bits do not.
- `ToBid` and `FromBid` use `EncodeBinary` and `DecodeBinary`; `1.0` is `31A000000000000A` and `1.50` is `3180000000000096`.
- `MultiplyChecked` throws only when finite operands produce an infinite result. `checked` has no effect on these types.
- `ToJson` and `FromJson` use `JsonSerializer` directly: the type is written as a JSON number and the trailing zeros survive.

## Running

```bash
dotnet test
```
