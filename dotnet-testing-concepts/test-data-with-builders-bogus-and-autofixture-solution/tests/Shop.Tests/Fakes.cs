using Bogus;

namespace Shop.Tests;

// Bogus: realistic data. UseSeed makes the "random" values the same on every run and every machine.
public static class Fakes
{
    public static Faker<Customer> Customers(int seed = 1234) =>
        new Faker<Customer>("en")
            .UseSeed(seed)
            .CustomInstantiator(f => new Customer(f.Random.Guid(), f.Name.FullName(), f.Internet.Email(), f.Random.Bool()));

    public static Faker<Order> Orders(int seed = 1234)
    {
        var customers = Customers(seed + 1);
        return new Faker<Order>("en")
            .UseSeed(seed)
            .CustomInstantiator(f => new Order(
                f.Random.Guid(),
                customers.Generate(),
                f.Make(f.Random.Int(1, 5), () => new OrderLine(
                    f.Commerce.Ean8(),
                    f.Random.Int(1, 10),
                    Math.Round(f.Random.Decimal(1m, 200m), 2))).ToList()));
    }
}
