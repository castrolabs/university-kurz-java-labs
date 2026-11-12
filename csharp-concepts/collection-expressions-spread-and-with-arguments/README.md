# Collection Expressions, Spread and with(...) Arguments

Lab for the `collection-expressions-spread-and-with-arguments` concept.

## Scenario

A small library builds lists, sets and a custom collection (`Bag<T>`) only with collection expressions. You use spread (`..`), the `with(...)` arguments of C# 15 to pass a capacity or a comparer to the constructor, a `[CollectionBuilder]` method that receives the `with(...)` arguments, and you fix a method called `with` that changed meaning when the compiler moved to C# 15.

## What you will do

1. **Capacity (TODO-00).** `Names.WithCapacity`: `[with(capacity: ...), ..values]`. The test reads `List<T>.Capacity`.
2. **Comparer (TODO-01).** `Tags.CaseInsensitive`: pass `StringComparer.OrdinalIgnoreCase` to the `HashSet<string>` constructor from the collection expression.
3. **Spread and interface target (TODO-02).** `Sequences.Combine`: `[..head, ..tail, last]` into an `IReadOnlyList<int>`. A test checks that a lazy `tail` is enumerated once.
4. **Interface with arguments (TODO-03).** `Sequences.Reserve`: `IList<int>` accepts `with(capacity: n)` and the compiler creates a `List<int>`.
5. **Collection builder (TODO-04 and TODO-05).** `BagBuilder.Create`: one overload for `[1, 2, 3]` and another, with a `string label` before the `ReadOnlySpan<T>`, for `[with("nums"), 1, 2]`.
6. **The silent change (TODO-06).** `Legacy.Build` has a method named `with`. On C# 15 `[with(10), 3]` no longer calls it. Fix it without renaming the method.

## Optional challenges

- Try `int[] a = [with(1), 1];`, `IReadOnlyCollection<int> r = [with(1), 1];` and `List<int> l = [1, with(5)];` and read the errors (CS9355, CS9357, CS9354).
- Try `Queue<int> q = [1];` and `Dictionary<string, int> d = [with(StringComparer.Ordinal), new("a", 1)];` and see why they do not compile.
- Set `<LangVersion>14</LangVersion>` in the library project and read CS9327.

## Prerequisites

Collection expressions (C# 12), `List<T>` and `HashSet<T>`, and the **.NET 11 SDK (C# 15)**. The projects target `net11.0` because `with(...)` arguments are a C# 15 feature.

## Running

```
dotnet test
```

Every stubbed method throws `NotImplementedException`, so the red tests are your checklist.
