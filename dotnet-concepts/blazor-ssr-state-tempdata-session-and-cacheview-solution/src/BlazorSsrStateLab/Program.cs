using BlazorSsrStateLab.Components;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddRazorComponents();
builder.Services.AddDistributedMemoryCache();
builder.Services.AddSession();

var app = builder.Build();

app.UseSession();
app.MapRazorComponents<App>();

app.Run();

public partial class Program { }
