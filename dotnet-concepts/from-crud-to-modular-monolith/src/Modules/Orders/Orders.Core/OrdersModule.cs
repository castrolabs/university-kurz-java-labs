using Catalog.Core;
using Microsoft.AspNetCore.Builder;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Routing;
using Microsoft.Extensions.DependencyInjection;
using Orders.Contracts;

namespace Orders.Core;

public static class OrdersModule
{
    private const string GetOrderEndpointName = "GetOrder";

    public static IServiceCollection AddOrdersModule(this IServiceCollection services)
    {
        services.AddSingleton<OrderStore>();
        return services;
    }

    // TODO-02: Orders reaches into Catalog's GameStore (its internals). Replace GameStore with
    //   ICatalogModule (from Catalog.Contracts) in both endpoints and in ToDto, and in
    //   Orders.Core.csproj swap the reference to Catalog.Core for Catalog.Contracts.
    public static IEndpointRouteBuilder MapOrdersEndpoints(this IEndpointRouteBuilder app)
    {
        var group = app.MapGroup("/orders").WithTags("Orders");

        group.MapGet("/{id:guid}", (Guid id, OrderStore store, GameStore games) =>
        {
            var order = store.Find(id);
            return order is null ? Results.NotFound() : Results.Ok(ToDto(order, games));
        }).WithName(GetOrderEndpointName);

        group.MapPost("/", (PlaceOrderRequest request, OrderStore store, GameStore games) =>
        {
            if (request.Lines.Count == 0 || request.Lines.Any(l => l.Quantity <= 0))
                return Results.BadRequest("An order needs at least one line with a positive quantity.");

            var found = games.Find(request.Lines.Select(l => l.GameId).Distinct()).ToDictionary(g => g.Id);
            var unknown = request.Lines.Where(l => !found.ContainsKey(l.GameId)).Select(l => l.GameId).ToList();
            if (unknown.Count > 0)
                return Results.BadRequest($"Unknown game ids: {string.Join(", ", unknown)}.");

            // TODO-04: Copy the game's current price into UnitPrice. An order must keep the price
            //   that was paid, even if the catalog price changes later.
            var order = new Order(
                Guid.NewGuid(),
                [.. request.Lines.Select(l => new OrderLine(l.GameId, l.Quantity, 0m))]);
            store.Add(order);

            return Results.CreatedAtRoute(GetOrderEndpointName, new { id = order.Id }, ToDto(order, games));
        });

        return app;
    }

    private static OrderDto ToDto(Order order, GameStore games)
    {
        var found = games.Find(order.Lines.Select(l => l.GameId).Distinct()).ToDictionary(g => g.Id);
        var lines = order.Lines
            .Select(l => new OrderLineDto(l.GameId, found[l.GameId].Name, l.Quantity, l.UnitPrice))
            .ToList();

        return new OrderDto(order.Id, lines, lines.Sum(l => l.UnitPrice * l.Quantity));
    }
}
