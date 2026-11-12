using Microsoft.EntityFrameworkCore;

namespace UpgradeLab;

public static class CustomerQueries
{
    public static Task<List<string>> NamesWithTagAsync(ShopDbContext db, string tag) =>
        db.Customers.Where(c => c.Tags.Contains(tag)).OrderBy(c => c.Name).Select(c => c.Name).ToListAsync();

    public static Task<List<string>> NamesInAsync(ShopDbContext db, string city) =>
        db.Customers.Where(c => c.Address.City == city).OrderBy(c => c.Name).Select(c => c.Name).ToListAsync();

    public static async Task<Customer?> MostTaggedAsync(ShopDbContext db) =>
        await db.Customers.MaxByAsync(c => c.Tags.Count);
}
