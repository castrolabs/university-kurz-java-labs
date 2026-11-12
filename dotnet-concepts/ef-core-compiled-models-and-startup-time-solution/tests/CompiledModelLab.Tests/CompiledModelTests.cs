using CompiledModelLab;
using CompiledModelLab.CompiledModels;
using Microsoft.Data.Sqlite;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Infrastructure;
using Microsoft.EntityFrameworkCore.Metadata;
using Xunit.Abstractions;

namespace CompiledModelLab.Tests;

// ModelBuildCount is process-wide, so the tests in this class must not run in parallel
// with each other (xUnit runs the tests of one class sequentially).
public sealed class CompiledModelTests : IDisposable
{
    private readonly ITestOutputHelper _output;
    private readonly string _path = Path.Combine(Path.GetTempPath(), $"compiled-{Guid.NewGuid():N}.db");
    private string ConnectionString => $"Data Source={_path}";

    public CompiledModelTests(ITestOutputHelper output)
    {
        _output = output;
        // Create the schema once with the regular model. This runs OnModelCreating, so the
        // counter is reset afterwards.
        using var db = LabDb.Create(ConnectionString, compiled: false);
        db.Database.EnsureCreated();
        db.Set<Entity01>().Add(new Entity01 { Name = "first", Amount = 10m, CreatedAt = DateTime.UnixEpoch });
        db.SaveChanges();
        LabContext.ModelBuildCount = 0;
    }

    public void Dispose()
    {
        SqliteConnection.ClearAllPools();
        File.Delete(_path);
    }

    [Fact]
    public void Generated_model_covers_all_sixty_entity_types()
    {
        Assert.Equal(60, LabContextModel.Instance.GetEntityTypes().Count());
    }

    [Fact]
    public void Without_a_compiled_model_OnModelCreating_runs()
    {
        using var db = LabDb.Create(ConnectionString, compiled: false);
        Assert.Equal(1, db.Set<Entity01>().Count());
        Assert.Equal(1, LabContext.ModelBuildCount);
    }

    [Fact]
    public void With_a_compiled_model_OnModelCreating_does_not_run()
    {
        using var db = LabDb.Create(ConnectionString, compiled: true);
        Assert.Equal("first", db.Set<Entity01>().Single().Name);
        Assert.Equal(0, LabContext.ModelBuildCount);
    }

    [Fact]
    public void Startup_probe_reports_both_phases()
    {
        var plain = LabDb.MeasureStartup(ConnectionString, compiled: false);
        var compiled = LabDb.MeasureStartup(ConnectionString, compiled: true);

        _output.WriteLine($"plain:    model {plain.Model.TotalMilliseconds:F1} ms, first query {plain.FirstQuery.TotalMilliseconds:F1} ms");
        _output.WriteLine($"compiled: model {compiled.Model.TotalMilliseconds:F1} ms, first query {compiled.FirstQuery.TotalMilliseconds:F1} ms");

        Assert.True(plain.Model > TimeSpan.Zero && plain.FirstQuery > TimeSpan.Zero);
        Assert.True(compiled.Model > TimeSpan.Zero && compiled.FirstQuery > TimeSpan.Zero);
    }

    private static IModel CurrentModel()
    {
        var options = new DbContextOptionsBuilder<LabContext>().UseSqlite("Data Source=:memory:").Options;
        using var db = new LabContext(options);
        return db.GetService<IDesignTimeModel>().Model;
    }

    [Fact]
    public void Fresh_compiled_model_is_not_stale()
    {
        Assert.False(LabDb.IsStale(LabContextModel.Instance, CurrentModel()));
    }

    [Fact]
    public void Compiled_model_is_stale_after_the_model_grows()
    {
        var options = new DbContextOptionsBuilder<GrownContext>().UseSqlite("Data Source=:memory:").Options;
        using var grown = new GrownContext(options);
        var current = grown.GetService<IDesignTimeModel>().Model;

        Assert.True(LabDb.IsStale(LabContextModel.Instance, current));
    }

    // Same model as LabContext plus one more entity, as if a developer had added it
    // and forgotten to run `dotnet ef dbcontext optimize`.
    private sealed class GrownContext(DbContextOptions<GrownContext> options) : LabContext(options)
    {
        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            base.OnModelCreating(modelBuilder);
            modelBuilder.Entity<AuditLog>();
        }
    }

    private sealed class AuditLog
    {
        public int Id { get; set; }
        public string Message { get; set; } = "";
    }
}
