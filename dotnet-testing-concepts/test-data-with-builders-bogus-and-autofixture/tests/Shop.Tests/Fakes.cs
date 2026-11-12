using Bogus;

namespace Shop.Tests;

// TODO-02 (part 1): Bogus fakers. Both take a seed and call UseSeed(seed), so the data is the same on every run.
//   Customers: new Faker<Customer>("en").UseSeed(seed).CustomInstantiator(f => new Customer(...)) with
//              f.Random.Guid(), f.Name.FullName(), f.Internet.Email(), f.Random.Bool().
//   Orders:    1 to 5 lines (f.Make(count, () => new OrderLine(f.Commerce.Ean8(), f.Random.Int(1, 10),
//              Math.Round(f.Random.Decimal(1m, 200m), 2)))), and a customer from a customer faker.
//              Give the customer faker its own seed (seed + 1): why not the same one?
public static class Fakes
{
    public static Faker<Customer> Customers(int seed = 1234) => throw new NotImplementedException("TODO-02");

    public static Faker<Order> Orders(int seed = 1234) => throw new NotImplementedException("TODO-02");
}
