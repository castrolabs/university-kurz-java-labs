using SecurityHeadersLab;

var builder = WebApplication.CreateBuilder(args);
var app = builder.Build();

app.UseMiddleware<SecurityHeadersMiddleware>();

// An inline script is allowed only when it carries this request's nonce.
app.MapGet("/", (HttpContext ctx) =>
{
    var nonce = (string)ctx.Items[SecurityHeadersMiddleware.NonceKey]!;
    return Results.Content(
        $"<!doctype html><title>lab</title><script nonce=\"{nonce}\">document.title='ok'</script>",
        "text/html");
});

app.MapGet("/api/ping", () => "pong");

// Vulnerable: the whole entity is bound, so {"name":"x","isAdmin":true} makes an admin.
app.MapPost("/users/unsafe", (User user) => Results.Ok(user));

// Safe: the request type has no IsAdmin, the server decides the rest.
app.MapPost("/users/safe", (CreateUserRequest request) =>
    Results.Ok(new User { Id = 1, Name = request.Name, IsAdmin = false }));

app.Run();

public partial class Program;
