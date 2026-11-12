namespace SerilogLab;

public partial class OrderService(ILogger<OrderService> logger)
{
    public void Place(int orderId, decimal total)
    {
        logger.LogInformation("Order {OrderId} placed, total {Total}", orderId, total);
    }

    public void Register(Customer customer)
    {
        logger.LogInformation("Customer registered: {@Customer}", customer);
    }

    public void Refuse(int orderId, string reason) => OrderRefused(orderId, reason);

    [LoggerMessage(EventId = 3001, Level = LogLevel.Warning, Message = "Order {OrderId} refused: {Reason}")]
    private partial void OrderRefused(int orderId, string reason);
}
