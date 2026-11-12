using System.Text;
using Microsoft.Extensions.ObjectPool;

namespace BenchLab;

public static class ReportRenderer
{
    private static readonly ObjectPool<StringBuilder> Builders =
        new DefaultObjectPoolProvider().CreateStringBuilderPool();

    // Provided: every += copies the whole string built so far.
    public static string RenderSlow(IReadOnlyList<int> values)
    {
        var text = "";
        foreach (var value in values) text += value + ";";
        return text;
    }

    public static string RenderFast(IReadOnlyList<int> values)
    {
        // TODO-02: Return the same text as RenderSlow ("0;1;2;"), but allocate a few times the
        //   size of the result instead of copying the string once per value. Use one StringBuilder
        //   (with a starting capacity if you like) and ToString() once at the end.
        throw new NotImplementedException("Not implemented yet.");
    }

    public static string RenderPooled(IReadOnlyList<int> values)
    {
        // TODO-05 (optional): Same text again, but take the builder from `Builders`
        //   (Get/Return, with Return in a finally) so the only allocation left is the result string.
        //   Notice that the default policy discards builders that grew past 4096 characters.
        throw new NotImplementedException("Not implemented yet.");
    }
}
