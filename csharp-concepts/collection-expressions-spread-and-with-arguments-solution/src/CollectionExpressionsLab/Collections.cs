using System.Collections;
using System.Runtime.CompilerServices;

namespace CollectionExpressionsLab;

public static class Names
{
    public static List<string> WithCapacity(string[] values) =>
        [with(capacity: values.Length * 2), ..values];
}

public static class Tags
{
    public static HashSet<string> CaseInsensitive(params string[] values) =>
        [with(StringComparer.OrdinalIgnoreCase), ..values];
}

public static class Sequences
{
    // The target is an interface: the compiler chooses the concrete type.
    public static IReadOnlyList<int> Combine(int[] head, IEnumerable<int> tail, int last) =>
        [..head, ..tail, last];

    // With IList<int> the compiler creates a List<int>, so with(capacity) is accepted.
    public static IList<int> Reserve(int capacity, IEnumerable<int> items) =>
        [with(capacity: capacity), ..items];
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
    public static Bag<T> Create<T>(ReadOnlySpan<T> items) => new("", items.ToArray());

    // The with(...) arguments come before the ReadOnlySpan<T> parameter.
    public static Bag<T> Create<T>(string label, ReadOnlySpan<T> items) => new(label, items.ToArray());
}

public static class Legacy
{
    // An old helper that happens to be called `with`.
    public static int with(int value) => value * 2;

    // @with escapes the name, so the C# 15 compiler reads a call, not a with(...) element.
    public static List<int> Build() => [@with(10), 3];
}
