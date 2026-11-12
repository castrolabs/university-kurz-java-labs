# Upgrading to .NET 11 and Reading the Breaking Changes (Solution)

## Goal

Fix the silent and the compile-time breaks of a .NET 10 to .NET 11 upgrade. See the starter
project for the exercise statement.

## Solution notes

- `(decimal)someDouble` kept about 15 significant digits on .NET 10 and keeps the exact binary
  value on .NET 11 (`0.1` becomes `0.1000000000000000055511151231`). Going through
  `value.ToString("R")` recovers the number that was typed. Better still: do not start from a
  `double`, keep the price as `decimal` from the source.
- `Math.Round(double, digits)` rounds the exact binary value on .NET 11, so `Math.Round(2.675, 2)`
  is `2.67`. Rounding the typed decimal gives the business answer, `2.68`, on both runtimes.
- A ZIP entry whose bytes do not match the stored CRC32 throws `InvalidDataException` on
  .NET 11. On .NET 10 it was read without any error.
- An empty `GZipStream` writes 20 bytes on .NET 11 and 0 on .NET 10, so "is it empty" has to look
  at the decompressed content.
- `[with(1, 2), 3]` is a collection argument list in C# 15. `@with(1, 2)` is a call to a method
  named `with`.
- A type named `closed` is error CS9380 in C# 15. `class @closed` keeps the CLR name `closed`.
- `scoped Span<int> last = default;` followed by `last = pair;` inside the loop is error CS8352 on
  the SDK 11 compiler. Copy the data out of the span (`ToArray()`) inside the iteration.
- `(byte)300.0` is `44` on .NET 10 and `255` on .NET 11. `Math.Clamp` states the intent.
