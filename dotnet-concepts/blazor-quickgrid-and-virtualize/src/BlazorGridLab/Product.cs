namespace BlazorGridLab;

public record Product(int Id, string Name, decimal Price);

public static class Catalog
{
    // 23 products: Name01 (price 1), Name02 (price 2) ... Name23 (price 23).
    public static IQueryable<Product> All { get; } =
        Enumerable.Range(1, 23).Select(i => new Product(i, $"Name{i:00}", i)).AsQueryable();
}
