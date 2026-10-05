using Microsoft.EntityFrameworkCore;

namespace EfCrudLab;

public static class GameStoreDb
{
    private static readonly string[] GenreNames =
        ["Fighting", "Roleplaying", "Sports", "Racing", "Kids and Family"];

    public static void SeedGenres(DbContext context)
    {
        // TODO-00: Do nothing when the Genres table already has rows (the seed runs on every
        //   migrate). Otherwise add one Genre per entry in GenreNames and save.
        throw new NotImplementedException("Not implemented yet.");
    }

    public static Task SeedGenresAsync(DbContext context, CancellationToken ct)
    {
        // TODO-01: Same as SeedGenres, using the asynchronous EF Core methods.
        throw new NotImplementedException("Not implemented yet.");
    }

    public static DbContextOptionsBuilder ConfigureSeeding(this DbContextOptionsBuilder options)
    {
        // TODO-02: Hook both seed methods into the options: UseSeeding for Migrate/EnsureCreated
        //   and UseAsyncSeeding for MigrateAsync/EnsureCreatedAsync.
        return options;
    }
}
