# Closed Classes and Exhaustive Hierarchies (Solution)

Official solution for the `closed-classes-and-exhaustive-hierarchies` lab.

## What changed from the starter

- `Shape` is `closed` instead of `abstract`. It is still abstract (`new Shape()` does not compile), but `Shapes.App`, in another assembly, can now switch over `Circle` and `Rectangle` without a discard arm and without CS8509.
- Exhaustiveness covers only the direct subclasses. If you add a `closed` intermediate class, its own subclasses become part of the check; a non-closed intermediate class leaves a hole.
- `ToJson` and `FromJson` use `InferClosedTypePolymorphism = true`. Without it, serializing a `Shape` writes `{}` and reading one fails, because the serializer sees an abstract type with no discriminator.
- Adding a subclass to `Shape` or making a published class `closed` is a breaking change for consumers: their switches now have a hole.

## Running

```
dotnet test
```

Requires the .NET 11 SDK.
