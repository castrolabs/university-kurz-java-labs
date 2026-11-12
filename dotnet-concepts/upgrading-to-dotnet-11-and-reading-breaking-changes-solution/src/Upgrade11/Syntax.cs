namespace Upgrade11;

public static class Syntax
{
    // An old helper that happens to be called "with".
    public static int with(int a, int b) => a + b;

    public static object[] WithElements()
    {
        // [with(1, 2), 3] is a collection argument list in C# 15. @with is the method call.
        object[] xs = [@with(1, 2), 3];
        return xs;
    }

    public static int[] LastPair(int[] source)
    {
        // The span of each iteration dies with the iteration, so copy it out before the next one.
        int[] last = [];
        foreach (var x in source)
        {
            Span<int> pair = [x, x * 2];
            last = pair.ToArray();
        }

        return last;
    }
}
