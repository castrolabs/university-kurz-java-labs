using System.Collections;
using System.Runtime.CompilerServices;

namespace CollectionExpressionsLab;

public static class Names
{
    public static List<string> WithCapacity(string[] values)
    {
        // TODO-00: return the values in a List<string> whose Capacity is twice the number of
        // values, using a collection expression with a with(...) first element and a spread:
        //   [with(capacity: ...), ..values]
        throw new NotImplementedException("Not implemented yet.");
    }
}

public static class Tags
{
    public static HashSet<string> CaseInsensitive(params string[] values)
    {
        // TODO-01: build the HashSet<string> with a collection expression that passes
        // StringComparer.OrdinalIgnoreCase to the constructor: [with(comparer), ..values]
        throw new NotImplementedException("Not implemented yet.");
    }
}

public static class Sequences
{
    public static IReadOnlyList<int> Combine(int[] head, IEnumerable<int> tail, int last)
    {
        // TODO-02: one collection expression with two spreads and a single element:
        // [..head, ..tail, last]. The target is an interface, so the compiler picks the
        // concrete type. The tests check that a lazy `tail` is enumerated exactly once.
        throw new NotImplementedException("Not implemented yet.");
    }

    public static IList<int> Reserve(int capacity, IEnumerable<int> items)
    {
        // TODO-03: the target is IList<int>, a with(...) element is allowed, and the compiler
        // creates a List<int>. Pass the capacity: [with(capacity: capacity), ..items].
        throw new NotImplementedException("Not implemented yet.");
    }
}

[CollectionBuilder(typeof(BagBuilder), nameof(BagBuilder.Create))]
public sealed class Bag<T>(string label, IEnumerable<T> items) : IEnumerable<T>
{
    private readonly List<T> _items = [..items];

    public string Label { get; } = label;

    public IEnumerator<T> GetEnumerator() => _items.GetEnumerator();

    IEnumerator IEnumerable.GetEnumerator() => GetEnumerator();
}

public static class BagBuilder
{
    // Called for `Bag<int> b = [1, 2, 3];`
    public static Bag<T> Create<T>(ReadOnlySpan<T> items)
    {
        // TODO-04: a Bag with an empty label (""), holding the items.
        throw new NotImplementedException("Not implemented yet.");
    }

    // Called for `Bag<int> b = [with("nums"), 1, 2, 3];`. The arguments of with(...) come
    // BEFORE the ReadOnlySpan<T> parameter.
    public static Bag<T> Create<T>(string label, ReadOnlySpan<T> items)
    {
        // TODO-05: a Bag with the given label, holding the items.
        throw new NotImplementedException("Not implemented yet.");
    }
}

public static class Legacy
{
    // An old helper that happens to be called `with`.
    public static int with(int value) => value * 2;

    public static List<int> Build()
    {
        // TODO-06: this was written for C# 14, where `with(10)` inside [ ... ] was a call to
        // the method above and the result was [20, 3]. On C# 15 the same text is the first
        // element of the collection expression: capacity 10, elements [3]. Keep the old
        // meaning without renaming the method.
        return [with(10), 3];
    }
}
