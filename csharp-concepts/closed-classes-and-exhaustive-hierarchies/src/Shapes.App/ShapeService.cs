using System.Text.Json;
using Shapes.Contracts;

namespace Shapes.App;

public static class ShapeService
{
    public static double Area(Shape shape)
    {
        // TODO-01: switch expression with one arm per direct subclass and NO discard arm:
        //   Circle    -> Math.PI * R * R
        //   Rectangle -> W * H
        // The project turns CS8509 into an error. Shapes.Contracts is another assembly, so
        // this only compiles without `_` once Shape is `closed` (TODO-00). Until then the
        // compiler wants a discard arm.
        throw new NotImplementedException("Not implemented yet.");
    }

    public static string ToJson(Shape shape)
    {
        // TODO-02: System.Text.Json does not treat a closed class as polymorphic by default
        // (it writes {}). Serialize with JsonSerializerOptions { InferClosedTypePolymorphism = true }
        // to get {"$type":"Circle","R":2}.
        throw new NotImplementedException("Not implemented yet.");
    }

    public static Shape? FromJson(string json)
    {
        // TODO-02: Deserialize<Shape> with the same options, reading "$type" back to a subclass.
        throw new NotImplementedException("Not implemented yet.");
    }
}
