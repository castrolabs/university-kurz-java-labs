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
    public static string Describe(PaymentResult result)
    {
        // TODO-00: Use a switch expression with ONE arm per case type and NO discard arm:
        //   Approved -> "approved {Amount} ({AuthCode})"
        //   Declined -> "declined: {Reason}"
        //   Pending  -> "pending, retry in {RetryAfterSeconds}s"
        // The project turns CS8509 into an error, so forgetting a case type does not compile.
        // A default(PaymentResult) has a null Value: with no null arm the switch throws.
        throw new NotImplementedException("Not implemented yet.");
    }

    public static string DescribeSafe(PaymentResult result)
    {
        // TODO-01: Same text as Describe, plus a `null` arm that returns "no result".
        // Prefer `null` over `_`: a discard would also hide a case type added later.
        throw new NotImplementedException("Not implemented yet.");
    }

    public static string ToJson(IntOrString value)
    {
        // TODO-02: Serialize the union with System.Text.Json. The .NET 11 serializer writes
        // the case value without an envelope: 42 becomes 42, "hi" becomes "hi" in quotes.
        throw new NotImplementedException("Not implemented yet.");
    }

    public static IntOrString FromJson(string json)
    {
        // TODO-02: Deserialize back into the union. A token that matches no case type
        // (for example true) throws JsonException; the tests check it.
        throw new NotImplementedException("Not implemented yet.");
    }
}

// A hand-written union that stores the int inline instead of boxing it.
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

    // TODO-03: Implement the four members below (the compiler reads them by name):
    //   Value                    -> the int boxed, the string, or null when _tag is 0
    //   HasValue                 -> true when _tag is not 0
    //   TryGetValue(out int)     -> true only when _tag is 1
    //   TryGetValue(out string)  -> true only when _tag is 2
    // With TryGetValue present, `item switch { int i => ... }` never touches Value,
    // so the int is not boxed. The allocation test checks zero bytes.
    public object? Value => throw new NotImplementedException("Not implemented yet.");

    public bool HasValue => throw new NotImplementedException("Not implemented yet.");

    public bool TryGetValue(out int value) => throw new NotImplementedException("Not implemented yet.");

    public bool TryGetValue(out string value) => throw new NotImplementedException("Not implemented yet.");
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
