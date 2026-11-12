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
        var builder = new StringBuilder(values.Count * 6);
        foreach (var value in values) builder.Append(value).Append(';');
        return builder.ToString();
    }

    public static string RenderPooled(IReadOnlyList<int> values)
    {
        var builder = Builders.Get();
        try
        {
            foreach (var value in values) builder.Append(value).Append(';');
            return builder.ToString();
        }
        finally
        {
            Builders.Return(builder);
        }
    }
}
