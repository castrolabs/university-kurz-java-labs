using Serilog;
using SerilogLab;

var builder = WebApplication.CreateBuilder(args);

builder.Host.UseSerilog((context, services, logger) => logger
    .ReadFrom.Configuration(context.Configuration)
    .ReadFrom.Services(services)
    .Enrich.FromLogContext()
    .Destructure.ByTransforming<Customer>(c => new { c.Id, Email = Mask(c.Email) }));

builder.Services.AddSingleton<OrderService>();

var app = builder.Build();

app.UseMiddleware<TenantMiddleware>();
app.UseSerilogRequestLogging(options =>
    options.EnrichDiagnosticContext = (diagnostics, http) =>
        diagnostics.Set("Tenant", http.Request.Headers["X-Tenant"].FirstOrDefault() ?? "none"));
app.MapPost("/orders/{id:int}", (int id, decimal total, OrderService orders) => { orders.Place(id, total); return Results.Ok(); });
app.MapPost("/orders/{id:int}/refuse", (int id, OrderService orders) => { orders.Refuse(id, "out of stock"); return Results.Ok(); });
app.MapPost("/customers", (Customer customer, OrderService orders) => { orders.Register(customer); return Results.Ok(); });
app.MapGet("/boom", () => { throw new InvalidOperationException("kaboom"); });

app.Run();

static string Mask(string email)
{
    var at = email.IndexOf('@');
    return at <= 1 ? "***" : email[0] + "***" + email[at..];
}

public partial class Program;
