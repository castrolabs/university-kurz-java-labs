using Catalog.Contracts;
using Microsoft.AspNetCore.Builder;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Routing;
using Microsoft.Extensions.DependencyInjection;

namespace Catalog.Core;

public static class CatalogModule
{
    public static IServiceCollection AddCatalogModule(this IServiceCollection services)
    {
        services.AddSingleton<GameStore>();
        services.AddScoped<ICatalogModule, CatalogModuleApi>();
        return services;
    }

    public static IEndpointRouteBuilder MapCatalogEndpoints(this IEndpointRouteBuilder app)
    {
        var group = app.MapGroup("/games").WithTags("Catalog");

        group.MapGet("/", (GameStore store) =>
            store.All().Select(g => new GameInfoDto(g.Id, g.Name, g.Price)));

        group.MapPut("/{id}/price", (int id, SetPriceRequest request, GameStore store) =>
            store.SetPrice(id, request.Price) ? Results.NoContent() : Results.NotFound());

        return app;
    }

    private sealed record SetPriceRequest(decimal Price);
}
