using RestLab;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddSingleton<GameStore>();
builder.Services.AddProblemDetails();

var app = builder.Build();

app.UseExceptionHandler();
app.UseStatusCodePages();

app.MapGamesEndpoints();

app.Run();

public partial class Program;
