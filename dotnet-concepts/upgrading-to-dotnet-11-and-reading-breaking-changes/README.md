# Upgrading to .NET 11 and Reading the Breaking Changes

## Goal

Move a small project from .NET 10 to .NET 11 and fix what the upgrade breaks. Some of it is
silent: the code builds and runs, and the answer is different. Some of it stops compiling with the
SDK 11 compiler. The tests describe what the application must do, and they point at each break.

## Prerequisites

- C# basics, `decimal` versus `double`, `System.IO.Compression`
- **.NET 11 SDK** (11.0.100 or later). The projects target `net11.0` and C# 15 is the default.

## Task

`src/Upgrade11` is a project written for .NET 10 and already retargeted to `net11.0`. It compiles,
because each compiler break is a stub, and the tests fail. Read the comment above each TODO: it
says what the code did on .NET 10 and what is different now. Fix the code, not the tests.

## Instructions

Complete the following TODOs:

- TODO-00: `Money.FromDouble` must give the decimal the person typed (`0.1` becomes `0.1m`).
- TODO-01: `Money.RoundToCents` must round half away from zero on that typed value (`2.675` becomes `2.68`).
- TODO-02: `Archive.ReadEntry` must answer `Corrupt` when a ZIP entry fails its checksum.
- TODO-03: `Archive.IsEmptyPayload` must recognize an empty GZip payload by its content.
- TODO-04: `Syntax.WithElements` must keep calling the method named `with` inside a collection expression.
- TODO-05: `Legacy.cs` must declare a type whose CLR name is `closed`.
- TODO-06: `Syntax.LastPair` must return the last pair without a span that escapes the loop body.

Run the tests until they all pass.

## Running the Lab

```bash
dotnet --version   # 11.0.100 or later
dotnet test
```

## Bonus (Optional)

- TODO-07 (optional): `Money.ToChannel` must clamp to 0 to 255 explicitly, so the result does not depend on how a runtime casts a `double` to `byte`.
- Type the old code of TODO-04, TODO-05 or TODO-06 and build once, to read the compiler error before you fix it.
