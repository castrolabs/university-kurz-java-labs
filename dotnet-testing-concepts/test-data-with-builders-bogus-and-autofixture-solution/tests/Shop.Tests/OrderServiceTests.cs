using AutoFixture;
using AutoFixture.AutoNSubstitute;

namespace Shop.Tests;

[TestClass]
public sealed class OrderServiceTests(TestContext testContext)
{
    private CancellationToken Ct => testContext.CancellationToken;

    // AutoFixture creates the object graph, AutoNSubstitute turns every interface into a substitute.
    private static IFixture NewFixture() => new Fixture().Customize(new AutoNSubstituteCustomization());

    [TestMethod]
    public async Task ShouldSaveTheOrderAndEmailTheCustomer()
    {
        var fixture = NewFixture();
        // Freeze: the substitute the test inspects is the one the service receives.
        var repository = fixture.Freeze<IOrderRepository>();
        var email = fixture.Freeze<IEmailSender>();
        var order = fixture.Create<Order>(); // anonymous: nothing in this test depends on its values
        var sut = fixture.Create<OrderService>();

        await sut.PlaceAsync(order, Ct);

        await repository.Received(1).SaveAsync(order, Ct);
        await email.Received(1).SendAsync(order.Customer.Email, Arg.Any<string>(), Ct);
    }

    [TestMethod]
    public async Task ShouldRejectAnOrderWithoutLinesAndSaveNothing()
    {
        var fixture = NewFixture();
        var repository = fixture.Freeze<IOrderRepository>();
        var order = fixture.Create<Order>() with { Lines = [] }; // the one thing this test is about
        var sut = fixture.Create<OrderService>();

        await Assert.ThrowsExactlyAsync<ArgumentException>(() => sut.PlaceAsync(order, Ct));

        await repository.DidNotReceiveWithAnyArgs().SaveAsync(default!, default);
    }

    [TestMethod]
    public async Task ShouldChargeTheDiscountedTotal()
    {
        var fixture = NewFixture();
        // Anonymous everywhere except the two facts the expected value depends on: premium, and a 100.00 line.
        var order = fixture.Create<Order>() with
        {
            Customer = fixture.Create<Customer>() with { IsPremium = true },
            Lines = [new OrderLine("SKU-1", 1, 100m)],
        };
        var sut = fixture.Create<OrderService>();

        var toPay = await sut.PlaceAsync(order, Ct);

        Assert.AreEqual(90m, toPay);
    }
}
