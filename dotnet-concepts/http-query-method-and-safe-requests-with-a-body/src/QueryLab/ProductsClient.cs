namespace QueryLab;

/// <summary>Typed client that searches the catalog with the QUERY method.</summary>
public class ProductsClient(HttpClient http)
{
    // TODO-03: send a request with HttpMethod.Query to /products, with the filter as a JSON
    // body (JsonContent.Create), call EnsureSuccessStatusCode and read the list of products.
    public Task<IReadOnlyList<Product>> SearchAsync(ProductFilter filter)
    {
        _ = http;
        throw new NotImplementedException("TODO-03");
    }
}
