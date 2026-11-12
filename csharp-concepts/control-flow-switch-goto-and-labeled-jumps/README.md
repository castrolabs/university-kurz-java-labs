# Control Flow: switch, goto and Labeled Jumps

Lab for the `control-flow-switch-goto-and-labeled-jumps` concept.

## Scenario

Five small routines that need jumps the plain `break` and `continue` cannot express: a switch statement where tiers accumulate, a retry written with a label, a grid search that stops two loops at once, a row scan that skips to the next row, and a command loop with a switch inside.

## What you will do

1. **goto case (TODO-00).** `Flow.Tier`: a switch statement where `3` also earns `2` and `1`. C# forbids fall-through (CS0163), so each section ends in `break` or `goto case`.
2. **goto label (TODO-01).** `Flow.Retry`: a backward jump with a label instead of a loop.
3. **break with label (TODO-02).** `Flow.FindFirst`: leave two nested loops with `break rows;` (C# 15).
4. **continue with label (TODO-03).** `Flow.CountOpenCells`: `continue rows;` jumps to the next row of the outer `foreach` (C# 15).
5. **Loop label around a switch (TODO-04).** `Flow.RunCommands`: inside a switch, `break` only leaves the switch, so label the loop to continue or stop it.

## Optional challenges

- Rewrite TODO-02 with a boolean flag and then with a local function that returns, and compare the three versions.
- Try `break nope;`, a duplicated label, and a `continue` inside a lambda, and read CS9393, CS0140 and CS1632.
- Set `<LangVersion>14</LangVersion>` and read CS9327 for labeled `break` and `continue`.

## Prerequisites

Loops, `switch` statements, and the **.NET 11 SDK (C# 15)**. The projects target `net11.0`: labeled `break` and `continue` are C# 15.

## Running

```
dotnet test
```

Every stubbed method throws `NotImplementedException`, so the red tests are your checklist.
