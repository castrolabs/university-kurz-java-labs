using RoutingLab;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddSingleton<GameStore>();

var app = builder.Build();

// TODO-00: Serve every games endpoint under the /api prefix by mapping them inside a
//   route group, without touching the paths inside GamesEndpoints.
app.MapGamesEndpoints();

app.Run();

public partial class Program;
