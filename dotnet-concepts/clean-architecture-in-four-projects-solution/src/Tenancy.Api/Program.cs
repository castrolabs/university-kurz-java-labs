using Tenancy.Api.Tenants;
using Tenancy.Application;
using Tenancy.Infrastructure;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddApplication();
builder.Services.AddInfrastructure(
    builder.Configuration.GetConnectionString("Tenancy") ?? "Data Source=tenancy.db");

var app = builder.Build();

app.Services.InitializeDatabase();

app.UseMiddleware<TenantResolutionMiddleware>();
app.MapTenantsEndpoints();

app.Run();

public partial class Program;
