using RoutingLab;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddSingleton<GameStore>();

var app = builder.Build();

app.MapGroup("/api").MapGamesEndpoints();

app.Run();

public partial class Program;
