namespace Shapes.Contracts;

// TODO-00: make Shape a `closed` class instead of `abstract`. A closed class is
// implicitly abstract, and the compiler treats its direct subclasses (which must live
// in this assembly) as the complete list.
public abstract record class Shape;

public record class Circle(double R) : Shape;

public record class Rectangle(double W, double H) : Shape;
