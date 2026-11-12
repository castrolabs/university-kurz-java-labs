namespace Invoicing.Tests;

// The inputs every test shares. The coffee order has a subtotal of 5.50 and 19% of that is 1.045: a
// half-cent that the rounding mode has to settle (1.05 away from zero, 1.04 to even), so the snapshot of
// this order notices a change of MidpointRounding.
internal static class Orders
{
    public static Order Coffee() => new(
        "Ada Lovelace",
        [new OrderLine("Coffee", 3, 0.50m), new OrderLine("Filter paper", 1, 4.00m)],
        "Deliver to the back door");

    public static Order WithMarkup() => new(
        "<b>Acme & Sons</b>",
        [new OrderLine("Beans \"dark\"", 2, 9.90m)]);
}
