namespace Shop.Tests;

// TODO-00: Test Data Builder. A valid order by default and one method per thing a test wants to say out loud:
//   Premium()            the customer is premium
//   WithEmail(email)     the customer's e-mail
//   WithLines(params OrderLine[])   replace the lines
//   Worth(total)         one line worth exactly `total` (the number that matters for the discount rule)
//   Build()              the Order
// Each method returns `this`. Use fixed values for the defaults (a literal Guid, "ana@example.com", one line of
// 10.00): a builder that calls Guid.NewGuid() or DateTime.Now makes failures impossible to reproduce.
public sealed class OrderBuilder
{
    public OrderBuilder Premium() => throw new NotImplementedException("TODO-00");

    public OrderBuilder WithEmail(string email) => throw new NotImplementedException("TODO-00");

    public OrderBuilder WithLines(params OrderLine[] lines) => throw new NotImplementedException("TODO-00");

    public OrderBuilder Worth(decimal total) => throw new NotImplementedException("TODO-00");

    public Order Build() => throw new NotImplementedException("TODO-00");
}
