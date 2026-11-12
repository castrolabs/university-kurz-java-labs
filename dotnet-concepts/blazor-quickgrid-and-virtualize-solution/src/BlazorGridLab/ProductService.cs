using Microsoft.AspNetCore.Components.QuickGrid;

namespace BlazorGridLab;

public record ProductPage(Product[] Items, int Total);

// Stands in for a remote API or a database: it only returns the slice that was asked for.
public class ProductService
{
    public List<(int Start, int Count, string Sort)> Calls { get; } = [];

    public Task<ProductPage> QueryAsync(int start, int count, IReadOnlyCollection<SortedProperty> sort)
    {
        var sorted = sort.FirstOrDefault();
        var query = Catalog.All;
        if (sorted.PropertyName == nameof(Product.Name))
            query = sorted.Direction == SortDirection.Ascending ? query.OrderBy(p => p.Name) : query.OrderByDescending(p => p.Name);
        else if (sorted.PropertyName == nameof(Product.Price))
            query = sorted.Direction == SortDirection.Ascending ? query.OrderBy(p => p.Price) : query.OrderByDescending(p => p.Price);
        lock (Calls) Calls.Add((start, count, sorted.PropertyName is null ? "" : $"{sorted.PropertyName} {sorted.Direction}"));
        return Task.FromResult(new ProductPage(query.Skip(start).Take(count).ToArray(), Catalog.All.Count()));
    }
}
