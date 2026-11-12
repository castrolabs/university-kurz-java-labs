using SecurityHeadersLab;

var builder = WebApplication.CreateBuilder(args);
var app = builder.Build();

// TODO-03: add SecurityHeadersMiddleware to the pipeline, before the endpoints.

// TODO-04: the page below has an inline script. Read the nonce from
// ctx.Items[SecurityHeadersMiddleware.NonceKey] and put it on the script tag
// as nonce="...", so the browser would run it under the policy.
app.MapGet("/", (HttpContext ctx) =>
    Results.Content(
        "<!doctype html><title>lab</title><script>document.title='ok'</script>",
        "text/html"));

app.MapGet("/api/ping", () => "pong");

// Vulnerable on purpose: the whole entity is bound, so {"name":"x","isAdmin":true} makes an admin.
app.MapPost("/users/unsafe", (User user) => Results.Ok(user));

// TODO-05: bind CreateUserRequest instead of User and build the User on the server
// (Id = 1, IsAdmin = false), ignoring any isAdmin in the body.
app.MapPost("/users/safe", (User user) => Results.Ok(user));

app.Run();

public partial class Program;
