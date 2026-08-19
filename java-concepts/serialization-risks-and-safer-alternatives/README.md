# Serialization Risks and Safer Alternatives

## Goal

See, first-hand, why `implements Serializable` is a bigger commitment than
it looks: `ObjectInputStream.readObject()` builds an object straight from
bytes and never calls the class's constructor, so any invariant the
constructor enforces is silently skipped unless you defend it explicitly.
You'll harden two classes against that gap, using the two techniques the
article covers.

## Prerequisites

- Constructors and invariants
- Checked exceptions
- Basic reflection (`Class`, `Field`, `setAccessible`) - only to read the
  tests, not to write them

## Task

`Quantity` and `Reservation` both have a real invariant enforced in their
constructor. Neither one defends that invariant during deserialization yet.

- `Quantity` should defend itself with a custom `readObject` that
  re-validates the field after `defaultReadObject()`.
- `Reservation` should defend itself with the serialization proxy pattern:
  a private nested `SerializationProxy` that is what actually gets
  serialized, and whose `readResolve()` rebuilds the real object through its
  validating constructor.

## Instructions

Complete the following TODOs in `Quantity`:

- TODO-00: Read the default fields from the stream in `readObject`.
- TODO-01: Re-check the invariant (amount must not be negative) after
  reading, and throw `InvalidObjectException` if it's violated.

Complete the following TODOs in `Reservation`:

- TODO-02: Implement `writeReplace()` so a `SerializationProxy` is
  serialized in place of the `Reservation` itself.
- TODO-03: Implement `readObject()` so it unconditionally rejects any stream
  that tries to produce a `Reservation` directly, bypassing the proxy.
- TODO-04: Implement `SerializationProxy.readResolve()` so it rebuilds the
  `Reservation` by calling `new Reservation(seats)` - the same constructor
  every other caller goes through.

Run the tests until they all pass.

## Running the Lab

From the project root:

```bash
mvn -pl java-concepts/serialization-risks-and-safer-alternatives test
```

Or from the lab directory:

```bash
cd java-concepts/serialization-risks-and-safer-alternatives
mvn test
```

## Bonus (Optional)

- TODO-05 (optional): Declare a `private static final long serialVersionUID`
  on `Quantity` to pin its serial form explicitly, instead of relying on the
  compiler-generated default.
