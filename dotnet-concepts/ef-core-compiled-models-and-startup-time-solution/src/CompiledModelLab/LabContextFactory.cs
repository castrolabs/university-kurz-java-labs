using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Design;

namespace CompiledModelLab;

// Used only by `dotnet ef`. The design-time tools need a way to build the context.
public sealed class LabContextFactory : IDesignTimeDbContextFactory<LabContext>
{
    public LabContext CreateDbContext(string[] args) =>
        new(new DbContextOptionsBuilder<LabContext>().UseSqlite("Data Source=design.db").Options);
}
