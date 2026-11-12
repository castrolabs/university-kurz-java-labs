using Shop.Api.Mediator;

namespace Shop.Api.Features.Orders;

public sealed record PlaceOrderRequest(string Sku, int Quantity);

public static class OrdersEndpoints
{
    public static void MapOrdersEndpoints(this IEndpointRouteBuilder app)
    {
        var group = app.MapGroup("/orders");

        group.MapGet("/{id:guid}", async (Guid id, IMediator mediator, CancellationToken ct) =>
            await mediator.Send(new GetOrder(id), ct) is { } dto ? Results.Ok(dto) : Results.NotFound());

        group.MapPost("/", async (PlaceOrderRequest body, IMediator mediator, CancellationToken ct) =>
        {
            // TODO-03: send PlaceOrder through the mediator. Answer 201 with Location /orders/{id}
            // and an OrderDto body. If ValidationException is thrown, answer 400 with
            // Results.ValidationProblem({ "order": errors }). The route knows HTTP; the handler does not.
            try
            {
                var id = await mediator.Send(new PlaceOrder(body.Sku, body.Quantity), ct);
                return Results.Created($"/orders/{id}", new OrderDto(id, body.Sku.Trim(), body.Quantity));
            }
            catch (ValidationException e)
            {
                return Results.ValidationProblem(new Dictionary<string, string[]> { ["order"] = [.. e.Errors] });
            }
        });
    }
}
