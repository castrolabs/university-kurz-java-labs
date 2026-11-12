using Serilog;
using SerilogLab;

var builder = WebApplication.CreateBuilder(args);

// TODO-00: use Serilog as the logging provider (builder.Host.UseSerilog): read sinks and levels from
// configuration, read the services (so tests can add an ILogEventSink), enrich from the LogContext,
// and mask Customer.Email when the Customer is destructured (Mask below).
builder.Services.AddSingleton<OrderService>();

var app = builder.Build();

app.UseMiddleware<TenantMiddleware>();
// TODO-03: add Serilog request logging and enrich the summary with the "Tenant" header ("none" when absent)
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
