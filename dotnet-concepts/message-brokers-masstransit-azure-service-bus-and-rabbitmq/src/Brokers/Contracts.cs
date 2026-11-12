namespace Brokers;

// Events (published, fan out to every subscriber) and a command (sent to exactly one queue).
public record OrderSubmitted(Guid OrderId, decimal Total);
public record PaymentCaptured(Guid OrderId);
public record ShipOrder(Guid OrderId);
