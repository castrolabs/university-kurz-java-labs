# Closed Classes and Exhaustive Hierarchies

Lab for the `closed-classes-and-exhaustive-hierarchies` concept.

## Scenario

A contracts assembly (`Shapes.Contracts`) publishes a `Shape` hierarchy with `Circle` and `Rectangle`. An application assembly (`Shapes.App`) computes areas and serializes shapes. Because the two live in different assemblies, a plain `abstract` base would force a `_` arm in every switch: another assembly could add a subclass. A `closed` class removes that need.

## What you will do

1. **Close the hierarchy (TODO-00).** In `Shape.cs`, replace `abstract` with `closed`. The test reads the metadata the compiler writes (`IsClosedTypeAttribute`).
2. **Exhaustive switch across assemblies (TODO-01).** In `ShapeService.Area`, write one arm per subclass and no discard. `Shapes.App` turns CS8509 into an error, so before TODO-00 the compiler asks for `_` and after it does not.
3. **JSON discriminator (TODO-02).** Serialize and deserialize with `InferClosedTypePolymorphism = true`. By default the serializer writes `{}` for a closed class; with the option it writes `{"$type":"Circle","R":2}`.

## Optional challenges

- Add `public record class Triangle(double B, double H) : Shape;` to `Shapes.Contracts` and rebuild: `Area` stops compiling with CS8509 (the pattern `Triangle` is not covered). That is the point of `closed`.
- Create a third project that derives from `Shape` and read error CS9382 (a closed type from another assembly cannot be a base type).

## Prerequisites

Pattern matching with `switch` expressions, records and the **.NET 11 SDK (C# 15)**. The projects target `net11.0`: `closed` relies on `IsClosedTypeAttribute` from the .NET 11 runtime.

## Running

```
dotnet test
```

Every stubbed method throws `NotImplementedException`, so the red tests are your checklist.
