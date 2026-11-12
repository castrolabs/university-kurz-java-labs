using System.Diagnostics;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata;
using CompiledModelLab.CompiledModels;

namespace CompiledModelLab;

public static class LabDb
{
    // TODO-01: open a LabContext on SQLite. When `compiled` is true, hand EF Core the
    // generated model (LabContextModel.Instance) with UseModel. Also call
    // EnableServiceProviderCaching(false) so each context builds or loads its own model,
    // which keeps the counters and timings of this lab repeatable.
    public static LabContext Create(string connectionString, bool compiled)
    {
        throw new NotImplementedException("TODO-01");
    }

    // TODO-02: time two phases separately with a Stopwatch. First `db.Model` (it builds
    // the model, or loads the compiled one), then the first query on the database,
    // for example db.Set<Entity01>().Count().
    public static (TimeSpan Model, TimeSpan FirstQuery) MeasureStartup(string connectionString, bool compiled)
    {
        throw new NotImplementedException("TODO-02");
    }

    // TODO-03: describe a model as a sorted list of "ClrTypeName(prop1,prop2,...)", one
    // entry per entity type, so two models can be compared.
    public static IReadOnlyList<string> Shape(IModel model)
    {
        throw new NotImplementedException("TODO-03");
    }

    // TODO-03: a compiled model is stale when the model EF Core would build today
    // differs from the generated one.
    public static bool IsStale(IModel compiled, IModel current)
    {
        throw new NotImplementedException("TODO-03");
    }
}
