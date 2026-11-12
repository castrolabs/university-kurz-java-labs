namespace Upgrade11;

// The CLR name stays "closed" because the old system stored it. The @ makes the identifier
// a name instead of the C# 15 modifier; the compiled type is still called "closed".
public class @closed
{
    public string? Reason { get; set; }
}
