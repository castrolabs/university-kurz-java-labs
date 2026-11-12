using Tasks.Web;

var builder = WebApplication.CreateBuilder(args);
builder.Services.AddSingleton<TaskStore>();

var app = builder.Build();

app.UseDefaultFiles();
app.UseStaticFiles();

var api = app.MapGroup("/api/tasks");

// The list is slow on purpose (Tasks:ListDelayMs, 300 ms by default): the page shows "Loading tasks..." first.
// A test that reads the list right after navigation must wait for it, and Playwright's auto-wait does that.
api.MapGet("/", async (TaskStore store, IConfiguration configuration, CancellationToken ct) =>
{
    await Task.Delay(configuration.GetValue("Tasks:ListDelayMs", 300), ct);
    return store.All();
});

api.MapPost("/", (NewTask request, TaskStore store) =>
{
    if (string.IsNullOrWhiteSpace(request.Title))
    {
        return Results.ValidationProblem(new Dictionary<string, string[]> { ["title"] = ["Title is required."] });
    }

    var task = store.Add(request.Title.Trim());
    return Results.Created($"/api/tasks/{task.Id}", task);
});

api.MapPost("/{id:int}/toggle", (int id, TaskStore store) =>
    store.Toggle(id) is { } task ? Results.Ok(task) : Results.NotFound());

api.MapDelete("/{id:int}", (int id, TaskStore store) =>
    store.Remove(id) ? Results.NoContent() : Results.NotFound());

app.Run();

public record NewTask(string? Title);
