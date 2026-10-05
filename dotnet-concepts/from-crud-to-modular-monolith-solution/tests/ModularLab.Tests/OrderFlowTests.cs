using System.Net;
using System.Net.Http.Json;
using Microsoft.AspNetCore.Mvc.Testing;
using Orders.Contracts;

namespace ModularLab.Tests;

public class OrderFlowTests : IDisposable
{
    private readonly WebApplicationFactory<Program> _factory = new();
    private readonly HttpClient _client;

    public OrderFlowTests() => _client = _factory.CreateClient();

    public void Dispose() => _factory.Dispose();

    private static PlaceOrderRequest OrderFor(int gameId, int quantity = 2) =>
        new([new OrderLineRequest(gameId, quantity)]);

    [Fact]
    public async Task ShouldPlaceAnOrderAndReturnTheGameNamesFromTheCatalog()
    {
        var response = await _client.PostAsJsonAsync("/orders", OrderFor(1));

        Assert.Equal(HttpStatusCode.Created, response.StatusCode);
        var order = await response.Content.ReadFromJsonAsync<OrderDto>();
        Assert.Equal("Street Fighter II", order!.Lines.Single().GameName);
        Assert.Equal(2 * 19.99m, order.Total);
    }

    [Fact]
    public async Task ShouldLetTheClientFollowTheLocationHeader()
    {
        var response = await _client.PostAsJsonAsync("/orders", OrderFor(2, 1));

        var followed = await _client.GetAsync(response.Headers.Location);

        Assert.Equal(HttpStatusCode.OK, followed.StatusCode);
    }

    [Fact]
    public async Task ShouldKeepThePriceThatWasPaidWhenTheCatalogPriceChanges()
    {
        var placed = await _client.PostAsJsonAsync("/orders", OrderFor(1));
        var order = await placed.Content.ReadFromJsonAsync<OrderDto>();

        await _client.PutAsJsonAsync("/games/1/price", new { price = 24.99m });
        var reloaded = await _client.GetFromJsonAsync<OrderDto>($"/orders/{order!.Id}");

        Assert.Equal(19.99m, reloaded!.Lines.Single().UnitPrice);
        Assert.Equal(2 * 19.99m, reloaded.Total);
    }

    [Fact]
    public async Task ShouldRejectAnOrderForAGameThatDoesNotExist()
    {
        var response = await _client.PostAsJsonAsync("/orders", OrderFor(999));

        Assert.Equal(HttpStatusCode.BadRequest, response.StatusCode);
    }

    [Fact]
    public async Task ShouldRejectAnOrderWithoutLines()
    {
        var response = await _client.PostAsJsonAsync("/orders", new PlaceOrderRequest([]));

        Assert.Equal(HttpStatusCode.BadRequest, response.StatusCode);
    }
}
