namespace Returns.Tests.Support;

/// <summary>The questions the assistant is evaluated on, and the fact each answer must contain.</summary>
public sealed record Scenario(string Name, string Question, string FactPattern);

public static class Scenarios
{
    public static readonly Scenario[] All =
    [
        new("refund-window", "How long do I have to return a jacket?", @"30 days"),
        new("refund-time", "How long until my refund arrives?", @"5 business days"),
        new("shipping-defective", "Who pays for return shipping if the item arrived broken?", @"free|we pay|brightside"),
        new("shipping-regular", "How much does return shipping cost if I just do not like my boots?", @"6 EUR|6 euros|€ ?6"),
        new("sale-items", "Can I get my money back for a sale item?", @"exchang"),
        new("pitched-tent", "I pitched my tent once in the garden. Can I return it?", @"cannot|can't|can not|not be returned|not eligible"),
        new("gift-card", "Can I return a gift card for cash?", @"not refundable|cannot|can't|no"),
        new("unknown", "Do you ship to Norway?", @"don't know"),
    ];

    public static Scenario Get(string name) => All.Single(s => s.Name == name);
}
