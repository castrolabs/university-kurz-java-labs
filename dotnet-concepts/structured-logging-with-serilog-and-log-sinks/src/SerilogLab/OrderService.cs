namespace SerilogLab;

public partial class OrderService(ILogger<OrderService> logger)
{
    public void Place(int orderId, decimal total)
    {
        // TODO-01: log with a message template (placeholders OrderId and Total), never with string interpolation
        logger.LogInformation($"Order {orderId} placed, total {total}");
    }

    public void Register(Customer customer)
    {
        // TODO-04: log the customer as a destructured object ({@Customer}), not through ToString
        logger.LogInformation("Customer registered: {Customer}", customer);
    }

    public void Refuse(int orderId, string reason)
    {
        // TODO-05: replace with a [LoggerMessage] source-generated partial method (EventId 3001, Warning)
        logger.LogWarning("Order {OrderId} refused: {Reason}", orderId, reason);
    }
}
