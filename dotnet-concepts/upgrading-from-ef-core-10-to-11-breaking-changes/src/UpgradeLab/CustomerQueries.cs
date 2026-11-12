using Microsoft.EntityFrameworkCore;

namespace UpgradeLab;

public static class CustomerQueries
{
    public static Task<List<string>> NamesWithTagAsync(ShopDbContext db, string tag) =>
        db.Customers.Where(c => c.Tags.Contains(tag)).OrderBy(c => c.Name).Select(c => c.Name).ToListAsync();

    public static Task<List<string>> NamesInAsync(ShopDbContext db, string city) =>
        db.Customers.Where(c => c.Address.City == city).OrderBy(c => c.Name).Select(c => c.Name).ToListAsync();

    public static Task<Customer?> MostTaggedAsync(ShopDbContext db)
    {
        // TODO-05 (optional): The customer with the most tags, in one query, with MaxByAsync
        //          (translated by EF Core 11; no OrderBy + FirstOrDefault).
        throw new NotImplementedException("Optional.");
    }
}
