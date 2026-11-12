var builder = WebApplication.CreateBuilder(args);

builder.Services.AddProblemDetails();
// TODO-00: Register the ASP.NET Core validation services. Without them the attributes are ignored
// and every invalid request below is accepted.
builder.Services.AddSingleton<IUserRepository, InMemoryUserRepository>();

var app = builder.Build();

app.MapPost("/users", (Registration registration) => TypedResults.Created("/users/1", registration));
app.MapPost("/orders", (Order order) => TypedResults.Ok(order));

app.Run();

public partial class Program;
