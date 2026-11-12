using System.Text.Json;
using Shapes.Contracts;

namespace Shapes.App;

public static class ShapeService
{
    // TODO-01: one arm per direct subclass, NO discard arm. Shapes.Contracts is another
    // assembly, so this only compiles without `_` because Shape is `closed`.
    public static double Area(Shape shape) => shape switch
    {
        Circle c => Math.PI * c.R * c.R,
        Rectangle r => r.W * r.H,
    };

    // TODO-02: System.Text.Json does not treat a closed class as polymorphic by default
    // (it writes {}). Turn on InferClosedTypePolymorphism to get a "$type" discriminator.
    private static readonly JsonSerializerOptions Options = new() { InferClosedTypePolymorphism = true };

    public static string ToJson(Shape shape) => JsonSerializer.Serialize(shape, Options);

    public static Shape? FromJson(string json) => JsonSerializer.Deserialize<Shape>(json, Options);
}
