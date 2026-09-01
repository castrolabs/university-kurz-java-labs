# The HashMap Class - Solution

## Overview

This is the official solution for the HashMap lab. It shows how `put()`'s own return value replaces a separate `containsKey()` check, how `get()`'s `null` result becomes a clean `Optional`, and why `entrySet()` — not `keySet()` or `values()` alone — is the right tool once a loop needs both the key and the value.

## Key Concepts

### put() already tells you what was there before

```java
public Optional<Double> setPrice(String product, double price) {
    return Optional.ofNullable(prices.put(product, price));
}
```

`Map.put()` returns the value that used to be mapped to that key, or `null` if the key was new — so "was this an insert or an update?" is answered by the call itself, with no separate `containsKey()` lookup (which would hash the key twice and open a window for the two answers to disagree). `Optional.ofNullable()` turns that `null`-or-value contract into a type the caller can't forget to check.

### get() returns null for a missing key, not an exception

```java
public Optional<Double> getPrice(String product) {
    return Optional.ofNullable(prices.get(product));
}
```

`HashMap.get()` on an absent key returns `null` rather than throwing, which is easy to mishandle if the caller assumes every lookup succeeds. Wrapping it in `Optional.ofNullable()` at the boundary means nothing downstream has to remember that `HashMap` behaves this way.

### The read-then-put pattern

```java
public boolean applyDiscount(String product, double percentOff) {
    Double currentPrice = prices.get(product);
    if (currentPrice == null) {
        return false;
    }
    prices.put(product, currentPrice * (1 - percentOff / 100.0));
    return true;
}
```

There's no `update()` method on `Map` — every in-place change is a `get()` to read the current value, some computation, and a `put()` with the same key to write the result back. `put()` on an existing key never creates a second entry; it replaces the value in place, which is exactly what makes this safe to call repeatedly. Note `currentPrice` has to stay the boxed `Double` here — unboxing before the `null` check would throw a `NullPointerException` on a missing product instead of returning `false`.

### entrySet() when you need the key *and* the value

```java
public Optional<String> mostExpensive() {
    String bestProduct = null;
    double bestPrice = Double.NEGATIVE_INFINITY;
    for (Map.Entry<String, Double> entry : prices.entrySet()) {
        if (entry.getValue() > bestPrice) {
            bestPrice = entry.getValue();
            bestProduct = entry.getKey();
        }
    }
    return Optional.ofNullable(bestProduct);
}
```

`values()` alone can't answer "which product," and `keySet()` alone can't answer "which price" — the question needs both at once, which is exactly what `Map.Entry` carries. `entrySet()` returns a view backed by the map itself (not a copy), so this loop pays for one pass over the table rather than one `get()` per key.

## Summary

- `Map.put()`'s return value turns "insert or update?" into a single hash lookup instead of a `containsKey()` + `put()` pair.
- `Map.get()` returns `null` for a missing key; converting that to `Optional` at the method boundary keeps `null` from leaking into the rest of the codebase.
- There's no in-place "update" on `Map` — every change is `get()`, compute, `put()` back under the same key.
- Reach for `entrySet()`/`Map.Entry` specifically when a loop needs the key and the value together; `keySet()` or `values()` alone is enough (and clearer) when it doesn't.
