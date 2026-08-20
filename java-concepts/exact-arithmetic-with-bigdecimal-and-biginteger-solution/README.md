# Exact Arithmetic with BigDecimal and BigInteger - Solution

## Overview

This is the official solution for the `MoneySplitter` lab. It shows how to build a `BigDecimal` that carries exactly the digits a human wrote, how to make `divide()` name a rounding policy instead of guessing one, and how to redistribute rounding remainders so a set of shares sums back to the original total.

## Key Concepts

### Constructing from text, never from a double

```java
public static BigDecimal parseExact(String rawAmount) {
    return new BigDecimal(rawAmount);
}
```

The `String` constructor never touches a `double`, so `parseExact("0.1").add(parseExact("0.2"))` compares equal to `0.3` — no binary-floating-point tail. Had this routed through `Double.parseDouble(rawAmount)` and then `new BigDecimal(double)`, the exact same binary approximation that makes `0.1 + 0.2 == 0.3` false for primitives would have been faithfully recorded into the `BigDecimal` too. `BigDecimal.valueOf(double)` (via `Double.toString()`) is the fallback when the value truly starts life as a `double`; here it starts as text, so the `String` constructor is the right tool.

### divide() needs a scale and a RoundingMode

```java
public static BigDecimal shareOf(BigDecimal total, int numberOfParties, RoundingMode roundingMode) {
    return total.divide(BigDecimal.valueOf(numberOfParties), 2, roundingMode);
}
```

The one-argument `divide(BigDecimal)` throws `ArithmeticException` the instant the result doesn't terminate in base 10 — dividing `$5` three ways is exactly that case. Supplying a scale (2 decimal places, since this is money) and an explicit `RoundingMode` makes the rounding policy a decision in the code, not a hardware default.

### Redistributing the rounding remainder

```java
public static List<BigDecimal> splitEvenly(BigDecimal total, int numberOfParties) {
    if (numberOfParties <= 0) {
        throw new IllegalArgumentException("numberOfParties must be positive, got " + numberOfParties);
    }

    BigDecimal baseShare = shareOf(total, numberOfParties, RoundingMode.DOWN);
    BigDecimal distributed = baseShare.multiply(BigDecimal.valueOf(numberOfParties));
    BigDecimal remainder = total.subtract(distributed);

    BigDecimal cent = new BigDecimal("0.01");
    int leftoverCents = remainder.divide(cent, 0, RoundingMode.HALF_UP).intValueExact();

    List<BigDecimal> shares = new ArrayList<>(numberOfParties);
    for (int i = 0; i < numberOfParties; i++) {
        shares.add(i < leftoverCents ? baseShare.add(cent) : baseShare);
    }
    return shares;
}
```

Rounding every share down first guarantees no share ever exceeds its true value, which turns "how much is left over" into a small non-negative number of whole cents. Splitting `$10.00` three ways gives a base share of `$3.33` (rounded down from `3.3333...`), a remainder of `$0.01`, and one party bumped up to `$3.34` — the shares sum back to exactly `$10.00` instead of losing a cent to rounding.

### multiply() is always exact; setScale() is where rounding happens

```java
public static BigDecimal convertCurrency(BigDecimal amount, BigDecimal exchangeRate,
                                          int targetScale, RoundingMode roundingMode) {
    return amount.multiply(exchangeRate).setScale(targetScale, roundingMode);
}
```

`multiply()` never needs a `RoundingMode` — the product of two exact decimals is always representable exactly. Rounding only becomes necessary when the result is trimmed down to a target scale, which is why it's `setScale()`, not `multiply()`, that takes the `RoundingMode`.

### Never discard the result of an immutable operation

```java
public static BigDecimal sumExact(List<BigDecimal> amounts) {
    BigDecimal total = BigDecimal.ZERO;
    for (BigDecimal amount : amounts) {
        total = total.add(amount);
    }
    return total;
}
```

`add()` returns a new `BigDecimal` and leaves both operands untouched — `total.add(amount);` on its own line would silently compute and discard a value, since `BigDecimal` has no compile-time signal for a dropped return value the way an assignment operator does for primitives.

## Summary

- Build a `BigDecimal` from text with the `String` constructor; reach for `BigDecimal.valueOf(double)` only when the value genuinely starts as a `double`, and never use `new BigDecimal(double)` for anything meant to be exact.
- `divide()` throws on a non-terminating result unless you pass a scale and `RoundingMode` (or a `MathContext`) — that's a feature, not a papercut: it forces every division to state its rounding policy explicitly.
- `add`, `subtract`, and `multiply` always have an exact answer; rounding only enters at `divide()` or `setScale()`.
- `BigDecimal` is immutable — every arithmetic method returns a new instance, so the result of every call must be captured, never left as a bare statement.
