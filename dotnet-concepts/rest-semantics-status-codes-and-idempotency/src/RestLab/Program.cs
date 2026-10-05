using RestLab;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddSingleton<GameStore>();

// TODO-05 (optional): Register ProblemDetails so error responses use application/problem+json.

var app = builder.Build();

// TODO-06 (optional): Add the middleware that turns a body-less 404 and an unhandled
// exception into a problem+json response.

app.MapGamesEndpoints();

app.Run();

public partial class Program;
