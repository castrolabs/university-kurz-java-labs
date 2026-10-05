using Catalog.Contracts;
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

    public static IEndpointRouteBuilder MapOrdersEndpoints(this IEndpointRouteBuilder app)
    {
        var group = app.MapGroup("/orders").WithTags("Orders");

        group.MapGet("/{id:guid}", async (Guid id, OrderStore store, ICatalogModule catalog, CancellationToken ct) =>
        {
            var order = store.Find(id);
            return order is null ? Results.NotFound() : Results.Ok(await ToDtoAsync(order, catalog, ct));
        }).WithName(GetOrderEndpointName);

        group.MapPost("/", async (PlaceOrderRequest request, OrderStore store, ICatalogModule catalog, CancellationToken ct) =>
        {
            if (request.Lines.Count == 0 || request.Lines.Any(l => l.Quantity <= 0))
                return Results.BadRequest("An order needs at least one line with a positive quantity.");

            var games = await catalog.GetGamesAsync([.. request.Lines.Select(l => l.GameId).Distinct()], ct);
            var unknown = request.Lines.Where(l => !games.ContainsKey(l.GameId)).Select(l => l.GameId).ToList();
            if (unknown.Count > 0)
                return Results.BadRequest($"Unknown game ids: {string.Join(", ", unknown)}.");

            var order = new Order(
                Guid.NewGuid(),
                [.. request.Lines.Select(l => new OrderLine(l.GameId, l.Quantity, games[l.GameId].Price))]);
            store.Add(order);

            return Results.CreatedAtRoute(GetOrderEndpointName, new { id = order.Id }, await ToDtoAsync(order, catalog, ct));
        });

        return app;
    }

    private static async Task<OrderDto> ToDtoAsync(Order order, ICatalogModule catalog, CancellationToken ct)
    {
        var games = await catalog.GetGamesAsync([.. order.Lines.Select(l => l.GameId).Distinct()], ct);
        var lines = order.Lines
            .Select(l => new OrderLineDto(l.GameId, games[l.GameId].Name, l.Quantity, l.UnitPrice))
            .ToList();

        return new OrderDto(order.Id, lines, lines.Sum(l => l.UnitPrice * l.Quantity));
    }
}
