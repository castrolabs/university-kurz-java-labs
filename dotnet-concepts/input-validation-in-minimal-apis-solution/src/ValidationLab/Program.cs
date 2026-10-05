using ValidationLab;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddValidation();
builder.Services.AddSingleton<GameStore>();

var app = builder.Build();

app.MapGamesEndpoints();

app.Run();

public partial class Program;
