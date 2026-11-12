using System.Net;
using System.Net.Http.Json;
using Microsoft.AspNetCore.Mvc.Testing;
using Shop.Api.Features.Orders;

namespace Shop.Tests;

public class OrdersApiTests(WebApplicationFactory<Program> factory) : IClassFixture<WebApplicationFactory<Program>>
{
    [Fact]
    public async Task Post_creates_the_order_and_get_reads_it_back()
    {
        var client = factory.CreateClient();

        var created = await client.PostAsJsonAsync("/orders", new PlaceOrderRequest(" ABC-1 ", 3));

        Assert.Equal(HttpStatusCode.Created, created.StatusCode);
        var dto = await created.Content.ReadFromJsonAsync<OrderDto>();
        Assert.Equal("ABC-1", dto!.Sku);
        Assert.Equal($"/orders/{dto.Id}", created.Headers.Location!.OriginalString);

        var read = await client.GetFromJsonAsync<OrderDto>($"/orders/{dto.Id}");
        Assert.Equal(3, read!.Quantity);
    }

    [Fact]
    public async Task Post_with_invalid_data_answers_400_with_every_error()
    {
        var response = await factory.CreateClient().PostAsJsonAsync("/orders", new PlaceOrderRequest("", 0));

        Assert.Equal(HttpStatusCode.BadRequest, response.StatusCode);
        var body = await response.Content.ReadAsStringAsync();
        Assert.Contains("Sku is required.", body);
        Assert.Contains("Quantity must be between 1 and 100.", body);
    }

    [Fact]
    public async Task Get_unknown_order_answers_404()
    {
        var response = await factory.CreateClient().GetAsync($"/orders/{Guid.NewGuid()}");
        Assert.Equal(HttpStatusCode.NotFound, response.StatusCode);
    }
}
