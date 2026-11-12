using System.Reflection;
using System.Text.Json;
using Shapes.App;
using Shapes.Contracts;

namespace Shapes.Tests;

public class ShapeTests
{
    [Fact]
    public void ShouldMarkShapeAsAClosedType()
    {
        // The compiler records `closed` as IsClosedTypeAttribute in the assembly.
        var attributes = typeof(Shape).GetCustomAttributesData().Select(a => a.AttributeType.Name);
        Assert.Contains("IsClosedTypeAttribute", attributes);
    }

    [Fact]
    public void ShouldMakeAClosedClassAbstract()
    {
        Assert.True(typeof(Shape).IsAbstract);
    }

    [Fact]
    public void ShouldCompileTheConstructorsOfAClosedClassAsRequiringTheFeature()
    {
        // A closed class's constructors carry CompilerFeatureRequired("ClosedClasses"),
        // which stops older compilers and other languages from subclassing it.
        var ctor = typeof(Shape).GetConstructors(BindingFlags.Instance | BindingFlags.NonPublic | BindingFlags.Public).First();
        var required = ctor.GetCustomAttributesData().Select(a => a.AttributeType.Name);
        Assert.Contains("CompilerFeatureRequiredAttribute", required);
    }

    [Theory]
    [InlineData(2.0, 12.566370614359172)]
    [InlineData(0.0, 0.0)]
    public void ShouldComputeTheAreaOfACircle(double r, double expected) =>
        Assert.Equal(expected, ShapeService.Area(new Circle(r)), 9);

    [Fact]
    public void ShouldComputeTheAreaOfARectangle() =>
        Assert.Equal(12.0, ShapeService.Area(new Rectangle(3, 4)));

    [Fact]
    public void ShouldWriteATypeDiscriminator() =>
        Assert.Equal("""{"$type":"Circle","R":2}""", ShapeService.ToJson(new Circle(2)));

    [Fact]
    public void ShouldReadTheDerivedTypeBack()
    {
        var shape = ShapeService.FromJson("""{"$type":"Rectangle","W":3,"H":4}""");
        Assert.Equal(new Rectangle(3, 4), shape);
    }

    [Fact]
    public void ShouldWriteEmptyObjectWhenPolymorphismIsNotInferred()
    {
        // The default options do not infer polymorphism for closed classes.
        Assert.Equal("{}", JsonSerializer.Serialize<Shape>(new Circle(2)));
    }

    [Fact]
    public void ShouldNotReadAClosedClassWhenPolymorphismIsNotInferred()
    {
        Assert.Throws<NotSupportedException>(() => JsonSerializer.Deserialize<Shape>("""{"$type":"Circle","R":2}"""));
    }
}
