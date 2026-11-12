namespace Upgrade11;

/// <summary>
/// Code that the SDK 10 compiler accepts and the SDK 11 compiler rejects. The starter compiles
/// because each method is a stub. Write the new version, and if you want to see the error first,
/// type the old version and build.
/// </summary>
public static class Syntax
{
    // An old helper that happens to be called "with".
    public static int with(int a, int b) => a + b;

    public static object[] WithElements()
    {
        // TODO-04: The project had   object[] xs = [with(1, 2), 3];   which calls the method above
        //          and gives { 3, 3 }. In C# 15 a "with(...)" as the first element of a collection
        //          expression is a collection argument list, and on object[] it is error CS9355.
        //          Return { 3, 3 } with the call to the method written so it is still a call.
        throw new NotImplementedException("TODO-04");
    }

    public static int[] LastPair(int[] source)
    {
        // TODO-06: The project had
        //              scoped Span<int> last = default;
        //              foreach (var x in source) { Span<int> pair = [x, x * 2]; last = pair; }
        //              return last.ToArray();
        //          The SDK 10 compiler accepted it, but the span points at a stack buffer that dies
        //          at the end of each iteration. The SDK 11 compiler reports CS8352.
        //          Return the last { x, x * 2 } as an array, without a span that escapes the loop body.
        throw new NotImplementedException("TODO-06");
    }
}
