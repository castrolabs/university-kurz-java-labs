using System.Diagnostics;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata;
using CompiledModelLab.CompiledModels;

namespace CompiledModelLab;

public static class LabDb
{
    // TODO-01: opens the context, with or without the generated compiled model.
    public static LabContext Create(string connectionString, bool compiled)
    {
        // EF Core caches the built model per context type inside its internal service
        // provider. Turning the cache off makes every context build (or load) its own
        // model, so the counters and timings in this lab are repeatable.
        var builder = new DbContextOptionsBuilder<LabContext>()
            .UseSqlite(connectionString)
            .EnableServiceProviderCaching(false);
        if (compiled)
        {
            builder.UseModel(LabContextModel.Instance);
        }
        return new LabContext(builder.Options);
    }

    // TODO-02: times two phases separately. First the model (db.Model builds or loads it),
    // then the first query against the database.
    public static (TimeSpan Model, TimeSpan FirstQuery) MeasureStartup(string connectionString, bool compiled)
    {
        using var db = Create(connectionString, compiled);

        var watch = Stopwatch.StartNew();
        _ = db.Model;
        var model = watch.Elapsed;

        watch.Restart();
        _ = db.Set<Entity01>().Count();
        return (model, watch.Elapsed);
    }

    // TODO-03: a compiled model is stale when the model EF Core would build today differs
    // from the generated one. Compare the entity types and their property names.
    public static IReadOnlyList<string> Shape(IModel model) =>
        model.GetEntityTypes()
            .Select(e => $"{e.ClrType.Name}({string.Join(",", e.GetProperties().Select(p => p.Name).Order())})")
            .Order()
            .ToList();

    public static bool IsStale(IModel compiled, IModel current) =>
        !Shape(compiled).SequenceEqual(Shape(current));
}
