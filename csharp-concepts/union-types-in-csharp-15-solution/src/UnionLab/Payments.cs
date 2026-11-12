using System.Text.Json;

namespace UnionLab;

public record class Approved(int Amount, string AuthCode);
public record class Declined(string Reason);
public record class Pending(int RetryAfterSeconds);

// The compiler turns this into a struct with [Union], IUnion, one constructor per
// case type and an `object? Value` property.
public union PaymentResult(Approved, Declined, Pending);

public union IntOrString(int, string);

public static class Payments
{
    // TODO-00: switch over the union with one arm per case type and NO discard arm.
    public static string Describe(PaymentResult result) => result switch
    {
        Approved a => $"approved {a.Amount} ({a.AuthCode})",
        Declined d => $"declined: {d.Reason}",
        Pending p => $"pending, retry in {p.RetryAfterSeconds}s",
    };

    // TODO-01: same switch, plus a `null` arm for default(PaymentResult).
    public static string DescribeSafe(PaymentResult result) => result switch
    {
        Approved a => $"approved {a.Amount} ({a.AuthCode})",
        Declined d => $"declined: {d.Reason}",
        Pending p => $"pending, retry in {p.RetryAfterSeconds}s",
        null => "no result",
    };

    // TODO-02: System.Text.Json round trip of a union.
    public static string ToJson(IntOrString value) => JsonSerializer.Serialize(value);

    public static IntOrString FromJson(string json) => JsonSerializer.Deserialize<IntOrString>(json);
}

// TODO-03: a hand-written union that stores the int inline (no boxing).
[System.Runtime.CompilerServices.Union]
public struct IntOrText : System.Runtime.CompilerServices.IUnion
{
    private readonly int _int;
    private readonly string? _text;
    private readonly byte _tag; // 0 = none, 1 = int, 2 = text

    public IntOrText(int value) { _int = value; _tag = 1; }

    public IntOrText(string? value)
    {
        if (value is not null) { _text = value; _tag = 2; }
    }

    public object? Value => _tag switch { 1 => _int, 2 => _text, _ => null };

    public bool HasValue => _tag != 0;

    public bool TryGetValue(out int value) { value = _int; return _tag == 1; }

    public bool TryGetValue(out string value) { value = _text!; return _tag == 2; }
}

public static class Totals
{
    public static long Sum(IntOrText[] items)
    {
        long total = 0;
        foreach (var item in items)
        {
            total += item switch
            {
                int i => i,
                string s => s.Length,
                null => 0,
            };
        }
        return total;
    }
}
