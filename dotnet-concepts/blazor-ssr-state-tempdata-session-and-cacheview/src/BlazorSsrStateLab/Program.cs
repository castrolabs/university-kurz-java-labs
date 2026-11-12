using BlazorSsrStateLab.Components;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddRazorComponents();
// TODO-02: register what the session needs (cache + AddSession) and add UseSession below.

var app = builder.Build();

app.MapRazorComponents<App>();

app.Run();

public partial class Program { }
