# Collection Expressions, Spread and with(...) Arguments (Solution)

Official solution for the `collection-expressions-spread-and-with-arguments` lab.

## What changed from the starter

- `Names.WithCapacity` and `Tags.CaseInsensitive` use a `with(...)` first element, so the constructor gets the capacity and the comparer without falling back to `new List<string>(n)` or `new HashSet<string>(comparer)`.
- `Sequences.Combine` uses two spreads and one element. The target is an interface, so the compiler picks the concrete type, and a lazy `tail` is enumerated once.
- `Sequences.Reserve` works because an `IList<int>` target makes the compiler create a `List<int>`. For `IReadOnlyList<int>` or `IReadOnlyCollection<int>` only an empty `with()` is accepted (CS9357), and for arrays and spans no `with` at all (CS9355).
- `BagBuilder.Create` has two overloads: the second one takes the `with(...)` arguments before the `ReadOnlySpan<T>`.
- `Legacy.Build` uses `[@with(10), 3]`. The `@` makes the compiler read an identifier, so the method call keeps its C# 14 meaning. The silent change is that, without the `@`, the same code still compiles on C# 15 and gives `[3]` with capacity 10.
