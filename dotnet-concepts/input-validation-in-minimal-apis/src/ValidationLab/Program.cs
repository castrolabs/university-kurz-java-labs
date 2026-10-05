using ValidationLab;

var builder = WebApplication.CreateBuilder(args);

// TODO-00: Register the ASP.NET Core validation services so that data annotations
// are checked before a handler runs.

builder.Services.AddSingleton<GameStore>();

var app = builder.Build();

app.MapGamesEndpoints();

app.Run();

public partial class Program;
