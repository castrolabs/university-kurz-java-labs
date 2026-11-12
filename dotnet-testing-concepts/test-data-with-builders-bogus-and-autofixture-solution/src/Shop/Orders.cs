namespace Shop;

public record Customer(Guid Id, string Name, string Email, bool IsPremium);

public record OrderLine(string Sku, int Quantity, decimal UnitPrice);

public record Order(Guid Id, Customer Customer, IReadOnlyList<OrderLine> Lines)
{
    public decimal Total => Lines.Sum(line => line.Quantity * line.UnitPrice);
}

// Premium customers get 10%. Orders of 500 or more get another 5%. The two add up (15% at most).
public static class DiscountPolicy
{
    public const decimal PremiumRate = 0.10m;
    public const decimal BulkRate = 0.05m;
    public const decimal BulkThreshold = 500m;

    public static decimal RateFor(Order order)
    {
        var rate = 0m;
        if (order.Customer.IsPremium) rate += PremiumRate;
        if (order.Total >= BulkThreshold) rate += BulkRate;
        return rate;
    }
}

public interface IOrderRepository
{
    Task SaveAsync(Order order, CancellationToken cancellationToken);
}

public interface IEmailSender
{
    Task SendAsync(string to, string subject, CancellationToken cancellationToken);
}

public sealed class OrderService(IOrderRepository repository, IEmailSender email)
{
    // Saves the order, e-mails the customer, and returns what the customer pays after the discount.
    public async Task<decimal> PlaceAsync(Order order, CancellationToken cancellationToken = default)
    {
        if (order.Lines.Count == 0)
        {
            throw new ArgumentException("An order needs at least one line.", nameof(order));
        }

        var toPay = order.Total * (1 - DiscountPolicy.RateFor(order));
        await repository.SaveAsync(order, cancellationToken);
        await email.SendAsync(order.Customer.Email, $"Order {order.Id} confirmed", cancellationToken);
        return toPay;
    }
}
