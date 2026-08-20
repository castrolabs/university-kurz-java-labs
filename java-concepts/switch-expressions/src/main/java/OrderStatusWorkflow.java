enum OrderStatus {
    PLACED, PAID, SHIPPED, DELIVERED, CANCELLED
}

public class OrderStatusWorkflow {

    public static OrderStatus nextStatus(OrderStatus current) {
        // TODO-00: Return the status that follows `current` in the normal workflow,
        // using a switch EXPRESSION with a case for every OrderStatus constant and
        // NO default branch:
        //   PLACED    -> PAID
        //   PAID      -> SHIPPED
        //   SHIPPED   -> DELIVERED
        //   DELIVERED -> DELIVERED   (terminal: stays the same)
        //   CANCELLED -> CANCELLED   (terminal: stays the same)
        // Hint: an exhaustive switch expression over an enum needs every constant
        // covered — if you leave one out, this simply will not compile.
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public static String shippingPhase(OrderStatus status) {
        // TODO-01: Return a phase label using multi-value case labels (comma-separated
        // constants on one arrow arm) to group statuses that share a result:
        //   PLACED, PAID           -> "preparing"
        //   SHIPPED, DELIVERED     -> "in transit or delivered"
        //   CANCELLED               -> "cancelled"
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public static String describe(OrderStatus status) {
        // TODO-02: Return a human-readable description. The PAID arm must be
        // block-bodied (`case PAID -> { ... }`) and use `yield` to produce its value,
        // computing the message in two steps:
        //   1. a local variable holding "Payment received"
        //   2. yield that value with "; awaiting shipment" appended
        // The other arms are simple single-expression arrows:
        //   PLACED    -> "Order placed"
        //   SHIPPED   -> "Order shipped"
        //   DELIVERED -> "Order delivered"
        //   CANCELLED -> "Order cancelled"
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public static OrderStatus parseStatus(String code) {
        // TODO-03: Parse `code` into an OrderStatus using a switch expression over
        // String:
        //   - an explicit `case null` arm that throws
        //     new IllegalArgumentException("status code must not be null")
        //     (without it, a null selector throws NullPointerException before any
        //     case even runs)
        //   - "PLACED", "PAID", "SHIPPED", "DELIVERED", "CANCELLED" map to their
        //     matching constant
        //   - a `default` arm (required — String is not an enum, so the compiler
        //     cannot prove exhaustiveness) that throws
        //     new IllegalArgumentException("unknown status code: " + code)
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public static int priority(OrderStatus status) {
        // TODO-04 (optional): Return a queue priority using multi-value labels:
        //   DELIVERED, CANCELLED -> 0   (terminal states need no attention)
        //   PLACED                -> 1
        //   PAID                  -> 2
        //   SHIPPED                -> 3
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
