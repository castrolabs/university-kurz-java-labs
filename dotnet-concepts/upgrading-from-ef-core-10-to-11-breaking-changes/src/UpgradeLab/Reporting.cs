using Microsoft.EntityFrameworkCore;

namespace UpgradeLab;

public class Score
{
    public int Id { get; set; }
    public int Rating { get; set; }
    public int Views { get; set; }
}

public class ReportingDbContext(DbContextOptions<ReportingDbContext> options) : DbContext(options)
{
    public DbSet<Score> Scores => Set<Score>();
}

public static class ReportingOptions
{
    /// <summary>The reporting database is still a SQL Server 2019 instance (compatibility level 150).</summary>
    public static DbContextOptions<ReportingDbContext> Create() =>
        new DbContextOptionsBuilder<ReportingDbContext>()
            // TODO-04: EF Core 11 assumes SQL Server 2022 (level 160) and translates Math.Max to
            //          GREATEST, which a 2019 server does not have. Tell the provider the real level.
            .UseSqlServer("Server=reporting.invalid;Database=Reports;Trusted_Connection=True")
            .Options;

    /// <summary>The SQL EF Core generates for the query. It never opens a connection.</summary>
    public static string BestOfSql()
    {
        using var db = new ReportingDbContext(Create());
        return db.Scores.Select(s => Math.Max(s.Rating, s.Views)).ToQueryString();
    }
}
