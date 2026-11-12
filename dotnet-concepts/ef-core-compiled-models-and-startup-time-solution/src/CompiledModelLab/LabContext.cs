using Microsoft.EntityFrameworkCore;

namespace CompiledModelLab;

public class LabContext : DbContext
{
    // Counts how many times EF Core ran OnModelCreating in this process.
    public static int ModelBuildCount;

    public LabContext(DbContextOptions<LabContext> options) : base(options) { }

    // Subclasses pass their own options type through this constructor.
    protected LabContext(DbContextOptions options) : base(options) { }

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        Interlocked.Increment(ref ModelBuildCount);
        foreach (var type in typeof(LabContext).Assembly.GetTypes()
                     .Where(t => t.Namespace == "CompiledModelLab" && t.Name.StartsWith("Entity")))
        {
            modelBuilder.Entity(type);
        }
    }
}
