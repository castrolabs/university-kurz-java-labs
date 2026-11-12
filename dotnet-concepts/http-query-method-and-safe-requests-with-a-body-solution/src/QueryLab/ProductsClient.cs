using System.Net.Http.Json;

namespace QueryLab;

/// <summary>Typed client that searches the catalog with the QUERY method.</summary>
public class ProductsClient(HttpClient http)
{
    public async Task<IReadOnlyList<Product>> SearchAsync(ProductFilter filter)
    {
        using var request = new HttpRequestMessage(HttpMethod.Query, "/products")
        {
            Content = JsonContent.Create(filter),
        };
        using var response = await http.SendAsync(request);
        response.EnsureSuccessStatusCode();
        return (await response.Content.ReadFromJsonAsync<List<Product>>())!;
    }
}
