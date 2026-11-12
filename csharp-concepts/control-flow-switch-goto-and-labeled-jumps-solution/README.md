# Control Flow: switch, goto and Labeled Jumps (Solution)

Official solution for the `control-flow-switch-goto-and-labeled-jumps` lab.

## What changed from the starter

- `Tier` ends each section with `break` or `goto case N`; `case 0:` and `case -1:` share one section. Nothing falls through implicitly.
- `Retry` uses a label (`again:`) and `goto again`. A loop is the better tool; the label shows that a backward `goto` is legal inside a method.
- `FindFirst` puts the label `rows:` before the outer `for` and leaves both loops with `break rows;`.
- `CountOpenCells` puts `rows:` before the outer `foreach` and uses `continue rows;`.
- `RunCommands` labels the `while` as `loop:`. Inside the switch, `continue loop;` and `break loop;` act on the loop, while a plain `break` only ends the switch.
