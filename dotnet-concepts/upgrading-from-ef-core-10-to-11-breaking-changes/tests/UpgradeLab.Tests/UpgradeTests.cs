using Microsoft.Data.Sqlite;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Diagnostics;

namespace UpgradeLab.Tests;

public sealed class UpgradeTests : IDisposable
{
    private readonly SqliteConnection _connection = new("DataSource=:memory:");
    private readonly DbContextOptions<ShopDbContext> _options;

    public UpgradeTests()
    {
        _connection.Open();
        _options = new DbContextOptionsBuilder<ShopDbContext>().UseSqlite(_connection).Options;

        using var db = Db();
        db.Database.EnsureCreated();
        db.Customers.AddRange(
            new Customer
            {
                Id = 1, Name = "Ana", Tags = ["vip", "beta"], Address = new Address("Rua A", "Lisboa"),
                Notes = [new Note("first order"), new Note("asked for a call")]
            },
            new Customer { Id = 2, Name = "Bruno", Tags = ["beta"], Address = new Address("Rua B", "Porto") },
            new Customer { Id = 3, Name = "Clara", Address = new Address("Platz 3", "Berlin") });
        db.SaveChanges();
    }

    private ShopDbContext Db() => new(_options);

    public void Dispose() => _connection.Dispose();

    [Fact]
    public void ShouldCreateTheSchemaOnStartupWithoutMigrations()
    {
        using var connection = new SqliteConnection("DataSource=:memory:");
        connection.Open();
        var options = new DbContextOptionsBuilder<ShopDbContext>().UseSqlite(connection).Options;

        using var db = new ShopDbContext(options);
        Bootstrap.Prepare(db);
        Bootstrap.Prepare(db);

        db.Customers.Add(new Customer { Id = 1, Name = "Ana" });
        db.SaveChanges();
        Assert.Equal(1, db.Customers.Count());
    }

    [Fact]
    public async Task ShouldTranslateContainsOverThePrimitiveCollection()
    {
        await using var db = Db();

        Assert.Equal(["Ana", "Bruno"], await CustomerQueries.NamesWithTagAsync(db, "beta"));
        Assert.Equal(["Ana"], await CustomerQueries.NamesWithTagAsync(db, "vip"));
    }

    [Fact]
    public async Task ShouldRoundTripTheNotes()
    {
        await using var db = Db();

        var ana = await db.Customers.SingleAsync(c => c.Id == 1);

        Assert.Equal(["first order", "asked for a call"], ana.Notes.Select(n => n.Text));
    }

    [Fact]
    public void ShouldMapTheNotesWithoutTheObsoleteOwnedJsonCollection()
    {
        var options = new DbContextOptionsBuilder<ShopDbContext>()
            .UseSqlite(_connection)
            .ConfigureWarnings(w => w.Throw(RelationalEventId.OwnedEntityMappedToJsonCollectionWarning))
            .Options;

        using var db = new ShopDbContext(options);

        var notes = db.Model.FindEntityType(typeof(Customer))!.FindComplexProperty(nameof(Customer.Notes));
        Assert.NotNull(notes);
        Assert.True(notes.IsCollection);
    }

    [Fact]
    public async Task ShouldCreateAnExpressionIndexOverTheCityInsideTheJson()
    {
        await using var db = Db();

        var indexes = await db.Database
            .SqlQuery<string>($"SELECT sql AS Value FROM sqlite_master WHERE type = 'index' AND tbl_name = 'Customers'")
            .ToListAsync();

        Assert.Contains(indexes, sql => sql.Contains("->> 'City'"));
        Assert.Equal(["Bruno"], await CustomerQueries.NamesInAsync(db, "Porto"));
    }

    [Fact]
    public void ShouldNotUseGreatestOnASqlServer2019Target()
    {
        var sql = ReportingOptions.BestOfSql();

        Assert.DoesNotContain("GREATEST", sql);
    }

    [Fact]
    public async Task ShouldFindTheMostTaggedCustomerWithMaxBy()
    {
        await using var db = Db();

        var customer = await CustomerQueries.MostTaggedAsync(db);

        Assert.Equal("Ana", customer?.Name);
    }
}
