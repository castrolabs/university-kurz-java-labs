using Microsoft.EntityFrameworkCore;

namespace EfCrudLab;

public static class GameStoreDb
{
    private static readonly string[] GenreNames =
        ["Fighting", "Roleplaying", "Sports", "Racing", "Kids and Family"];

    public static void SeedGenres(DbContext context)
    {
        if (context.Set<Genre>().Any()) return;

        context.Set<Genre>().AddRange(GenreNames.Select(name => new Genre { Name = name }));
        context.SaveChanges();
    }

    public static async Task SeedGenresAsync(DbContext context, CancellationToken ct)
    {
        if (await context.Set<Genre>().AnyAsync(ct)) return;

        context.Set<Genre>().AddRange(GenreNames.Select(name => new Genre { Name = name }));
        await context.SaveChangesAsync(ct);
    }

    public static DbContextOptionsBuilder ConfigureSeeding(this DbContextOptionsBuilder options) =>
        options
            .UseSeeding((context, _) => SeedGenres(context))
            .UseAsyncSeeding((context, _, ct) => SeedGenresAsync(context, ct));
}
