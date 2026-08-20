# Switch Expressions - Solution

## Overview

This is the official solution for the Switch Expressions lab. It shows exhaustiveness
over an `enum`, multi-value labels, a `yield`-based block arm, and explicit `null`
handling in one small workflow class.

## Key Concepts

### Exhaustiveness over an enum needs no default

```java
public static OrderStatus nextStatus(OrderStatus current) {
    return switch (current) {
        case PLACED -> OrderStatus.PAID;
        case PAID -> OrderStatus.SHIPPED;
        case SHIPPED -> OrderStatus.DELIVERED;
        case DELIVERED -> OrderStatus.DELIVERED;
        case CANCELLED -> OrderStatus.CANCELLED;
    };
}
```

Every constant of `OrderStatus` has an arm, so the compiler can prove the switch always
produces a value — no `default` needed, and none allowed to silently swallow a
constant added later without recompiling this switch.

### Multi-value labels group results without fall-through

```java
public static String shippingPhase(OrderStatus status) {
    return switch (status) {
        case PLACED, PAID -> "preparing";
        case SHIPPED, DELIVERED -> "in transit or delivered";
        case CANCELLED -> "cancelled";
    };
}
```

A comma-separated label says directly what colon-form's stacked-label-plus-fall-through
idiom said indirectly — one arm, one result, no shared mutable state to reason about.

### yield produces a value from a block-bodied arm

```java
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
```

`PLACED`'s arm is a single expression, so its value *is* the arm's value. `PAID`'s arm
is a block, which has no implicit value — `yield` is what hands one back to the switch.

### case null replaces a defensive check, default is mandatory for String

```java
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
```

Without `case null`, passing `null` would throw `NullPointerException` before any case
even ran. `String` is not an enum, so the compiler cannot prove every possible input is
covered — `default` is mandatory here, unlike in `nextStatus()`.

## Summary

- A switch *expression* must produce a value for every possible input, which is what
  makes exhaustiveness checking exist in the first place.
- Arrow-form arms run exactly one branch; there is no fall-through to reason about.
- `yield` is required (and only meaningful) inside a block-bodied arm of a switch
  expression.
- `case null` is the only way to handle a null selector without an exception; `default`
  is mandatory whenever the compiler cannot enumerate every possible input itself.
