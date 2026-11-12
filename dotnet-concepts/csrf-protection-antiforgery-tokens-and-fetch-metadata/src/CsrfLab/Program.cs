using Microsoft.AspNetCore.Mvc;

var builder = WebApplication.CreateBuilder(args);

// TODO-00: Register the antiforgery services. Form endpoints carry antiforgery metadata, and the
// token endpoint below needs IAntiforgery.

// TODO-01: Register a CORS policy named "partner" for https://partner.example that allows any
// header and method and allows credentials. A trusted CORS origin is the one cross-site caller
// the automatic CSRF check lets through.

var app = builder.Build();

// TODO-02: Add the CORS middleware to the pipeline.

// TODO-03: When the configuration key "Csrf:UseTokens" is true, add the antiforgery middleware.
// Without it the .NET 11 automatic check (Sec-Fetch-Site and Origin) is the only defense.

// TODO-04: Issue a token pair. GetAndStoreTokens sets the antiforgery cookie on the response and
// returns the request token. Answer { field, token } with the form field name and the request token.
// Take IAntiforgery as a handler parameter once the services are registered.
app.MapGet("/token", () => Results.Ok(new TokenResponse("", "")));

// A form endpoint: [FromForm] adds antiforgery metadata on its own.
app.MapPost("/transfer", ([FromForm] string to) => $"sent to {to}");

// TODO-05: A payment provider posts a form here from its own site and cannot send a token.
// Opt this endpoint out of antiforgery, and keep the secret check as the real defense.
app.MapPost("/webhook", ([FromForm] string eventName, HttpRequest request) =>
    request.Headers["X-Webhook-Secret"] == "s3cret"
        ? Results.Ok($"handled {eventName}")
        : Results.Unauthorized());

// TODO-06: A form that a trusted partner site posts with credentials: require the "partner" policy.
app.MapPost("/partner-transfer", ([FromForm] string to) => $"partner sent to {to}");

// A JSON endpoint has no antiforgery metadata, so neither mechanism checks it.
app.MapPost("/api/transfer", (TransferDto dto) => $"api sent to {dto.To}");

app.Run();

public record TokenResponse(string Field, string Token);

public record TransferDto(string To);

public partial class Program;
