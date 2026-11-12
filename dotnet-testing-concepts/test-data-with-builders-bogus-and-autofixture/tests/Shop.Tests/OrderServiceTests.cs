using AutoFixture;
using AutoFixture.AutoNSubstitute;

namespace Shop.Tests;

// TODO-03: AutoFixture with AutoNSubstitute. Create the fixture with
// new Fixture().Customize(new AutoNSubstituteCustomization()).
[TestClass]
public sealed class OrderServiceTests(TestContext testContext)
{
    private CancellationToken Ct => testContext.CancellationToken;

    [TestMethod]
    public async Task ShouldSaveTheOrderAndEmailTheCustomer()
    {
        // fixture.Freeze<IOrderRepository>() and Freeze<IEmailSender>() so the substitute you inspect is the one
        // the service gets, then fixture.Create<Order>() and fixture.Create<OrderService>().
        // Assert with Received(1) on both. Nothing here depends on the order's values: anonymous is right.
        await Task.CompletedTask;
        Assert.Fail("TODO-03: write this test.");
    }

    [TestMethod]
    public async Task ShouldRejectAnOrderWithoutLinesAndSaveNothing()
    {
        // fixture.Create<Order>() with { Lines = [] }: pin only the one thing the test is about.
        // Assert.ThrowsExactlyAsync<ArgumentException>, and DidNotReceiveWithAnyArgs on the repository.
        await Task.CompletedTask;
        Assert.Fail("TODO-03: write this test.");
    }

    [TestMethod]
    public async Task ShouldChargeTheDiscountedTotal()
    {
        // The expected value depends on two facts: a premium customer and one 100.00 line. Pin exactly those two
        // with `with` expressions and leave the rest anonymous. A fully anonymous order here would pass or fail
        // depending on the random IsPremium and the random amounts: this is where AutoFixture hides intent.
        await Task.CompletedTask;
        Assert.Fail("TODO-03: write this test.");
    }
}
