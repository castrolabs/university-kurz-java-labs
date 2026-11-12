using BlazorGridLab;
using BlazorGridLab.Components;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddRazorComponents();
builder.Services.AddSingleton<ProductService>();

var app = builder.Build();

app.MapRazorComponents<App>();

app.Run();

public partial class Program { }
