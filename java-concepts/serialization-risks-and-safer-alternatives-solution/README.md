# Serialization Risks and Safer Alternatives - Solution

## Overview

This is the official solution for the Serialization Risks lab. `Quantity`
shows the hand-written defensive `readObject`, and `Reservation` shows the
serialization proxy pattern - the two techniques the article presents for
re-enforcing a constructor's invariant against a hand-crafted byte stream.

## Key Concepts

### Quantity: defensive readObject

```java
private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();

    if (amount < 0) {
        throw new InvalidObjectException("amount must not be negative: " + amount);
    }
}
```

`readObject` is effectively a second, hidden constructor: `ObjectInputStream`
builds the object directly from bytes and never calls `new Quantity(...)`,
so the constructor's `amount < 0` check simply never runs unless
`readObject` repeats it. `defaultReadObject()` populates the fields
(including the `final` one - deserialization is specially allowed to set
`final` fields exactly once), and only after that can the invariant be
checked against real values. `InvalidObjectException` is thrown rather than
`IllegalArgumentException` because that is the exception
`ObjectInputStream.readObject()` is documented to surface for a rejected
stream; a `RuntimeException` would work too, but a checked
`InvalidObjectException` says "this stream is invalid" rather than "this
argument is invalid", which is the more accurate description of what
actually failed.

### Reservation: the serialization proxy pattern

```java
private Object writeReplace() {
    return new SerializationProxy(this);
}

private void readObject(ObjectInputStream in) throws InvalidObjectException {
    throw new InvalidObjectException("Proxy required");
}

private static class SerializationProxy implements Serializable {
    private final int seats;

    SerializationProxy(Reservation reservation) {
        this.seats = reservation.seats;
    }

    private Object readResolve() {
        return new Reservation(seats);
    }
}
```

`writeReplace()` intercepts every outgoing serialization of a `Reservation`
and substitutes the proxy, so the stream never carries `Reservation`'s own
field layout at all. On the way back in, `SerializationProxy` deserializes
itself normally (it has no invariant of its own to defend - it is just a
carrier for `seats`), and `readResolve()` swaps it back out for a real
`Reservation`, built by calling `new Reservation(seats)`. That call goes
through the exact same validation every other caller of the constructor
goes through - there is no second, hand-written check to keep in sync with
the first.

The outer class's own `readObject` exists purely as a trap door: since
`writeReplace` only fires on the way *out*, nothing stops a forged stream
from claiming to be a `Reservation` directly and skipping the proxy on the
way *in*. Overriding `readObject` to unconditionally throw
`InvalidObjectException` closes that gap - a `Reservation` can now only ever
come from `SerializationProxy.readResolve()`.

## Summary

- `readObject` never runs the constructor, so any invariant the constructor
  enforces has to be re-checked by hand, against input that can be fully
  attacker-controlled.
- A defensive `readObject` (`Quantity`) re-validates after
  `defaultReadObject()` and throws `InvalidObjectException` on violation -
  straightforward, but it is one more piece of logic to keep in sync with
  the constructor as the class evolves.
- The serialization proxy pattern (`Reservation`) sidesteps that
  duplication entirely: the proxy carries the state, and `readResolve`
  reconstructs the object through the real constructor, so there is only
  ever one place the invariant is enforced.
- Blocking the enclosing class's own `readObject` is what actually makes
  the proxy pattern airtight - without it, a forged stream could still
  target the enclosing class directly and skip the proxy altogether.
