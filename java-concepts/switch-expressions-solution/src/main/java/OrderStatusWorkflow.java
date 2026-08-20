enum OrderStatus {
    PLACED, PAID, SHIPPED, DELIVERED, CANCELLED
}

public class OrderStatusWorkflow {

    public static OrderStatus nextStatus(OrderStatus current) {
        return switch (current) {
            case PLACED -> OrderStatus.PAID;
            case PAID -> OrderStatus.SHIPPED;
            case SHIPPED -> OrderStatus.DELIVERED;
            case DELIVERED -> OrderStatus.DELIVERED;
            case CANCELLED -> OrderStatus.CANCELLED;
        };
    }

    public static String shippingPhase(OrderStatus status) {
        return switch (status) {
            case PLACED, PAID -> "preparing";
            case SHIPPED, DELIVERED -> "in transit or delivered";
            case CANCELLED -> "cancelled";
        };
    }

    public static String describe(OrderStatus status) {
        return switch (status) {
            case PLACED -> "Order placed";
            case PAID -> {
                String base = "Payment received";
                yield base + "; awaiting shipment";
            }
            case SHIPPED -> "Order shipped";
            case DELIVERED -> "Order delivered";
            case CANCELLED -> "Order cancelled";
        };
    }

    public static OrderStatus parseStatus(String code) {
        return switch (code) {
            case null -> throw new IllegalArgumentException("status code must not be null");
            case "PLACED" -> OrderStatus.PLACED;
            case "PAID" -> OrderStatus.PAID;
            case "SHIPPED" -> OrderStatus.SHIPPED;
            case "DELIVERED" -> OrderStatus.DELIVERED;
            case "CANCELLED" -> OrderStatus.CANCELLED;
            default -> throw new IllegalArgumentException("unknown status code: " + code);
        };
    }

    public static int priority(OrderStatus status) {
        return switch (status) {
            case DELIVERED, CANCELLED -> 0;
            case PLACED -> 1;
            case PAID -> 2;
            case SHIPPED -> 3;
        };
    }
}
