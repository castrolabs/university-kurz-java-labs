namespace Shapes.Contracts;

// TODO-00: `closed` makes the class abstract AND lets the compiler treat the direct
// subclasses below as the complete list (only code in this assembly can add more).
public closed record class Shape;

public record class Circle(double R) : Shape;

public record class Rectangle(double W, double H) : Shape;
