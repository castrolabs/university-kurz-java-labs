using Microsoft.Extensions.Validation;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddProblemDetails();
builder.Services.AddValidation();
builder.Services.AddSingleton<IUserRepository, InMemoryUserRepository>();

var app = builder.Build();

app.MapPost("/users", (Registration registration) => TypedResults.Created("/users/1", registration));
app.MapPost("/orders", (Order order) => TypedResults.Ok(order));

app.Run();

public partial class Program;
