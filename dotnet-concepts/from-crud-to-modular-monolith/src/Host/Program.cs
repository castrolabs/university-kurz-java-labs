using Catalog.Core;
using Orders.Core;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddCatalogModule();
builder.Services.AddOrdersModule();

var app = builder.Build();

app.MapCatalogEndpoints();
app.MapOrdersEndpoints();

app.Run();

public partial class Program;
