namespace QueryLab;

public record Product(int Id, string Name, string Category, decimal Price);

public record ProductFilter(string? Category, decimal? MaxPrice);

public class Catalog
{
    private static readonly Product[] Items =
    [
        new(1, "Clean Code", "Books", 39.90m),
        new(2, "Domain-Driven Design", "Books", 59.00m),
        new(3, "Mechanical Keyboard", "Hardware", 129.00m),
        new(4, "USB-C Hub", "Hardware", 45.50m),
        new(5, "Desk Lamp", "Home", 24.90m),
    ];

    private int _searches;

    /// <summary>How many times the filter really ran (a cache hit does not count).</summary>
    public int Searches => _searches;

    public IReadOnlyList<Product> All() => Items;

    public IReadOnlyList<Product> Search(ProductFilter filter)
    {
        Interlocked.Increment(ref _searches);
        return Items
            .Where(p => filter.Category is null || p.Category.Equals(filter.Category, StringComparison.OrdinalIgnoreCase))
            .Where(p => filter.MaxPrice is null || p.Price <= filter.MaxPrice)
            .ToList();
    }
}
