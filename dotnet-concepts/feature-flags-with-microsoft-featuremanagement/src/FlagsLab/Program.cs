using FlagsLab;
using Microsoft.FeatureManagement;
using Microsoft.FeatureManagement.AspNetCore;

var builder = WebApplication.CreateBuilder(args);

// TODO-00: Register feature management (flags are read from the "feature_management" section of IConfiguration)
//          and enable targeting with HeaderTargetingContextAccessor.
builder.Services.AddHttpContextAccessor();
builder.Services.AddControllers();

var app = builder.Build();

app.MapControllers();

// TODO-01: Return "price v2" when the NewPricing flag is enabled (IFeatureManager), otherwise "price v1".
app.MapGet("/pricing", () => "price v1");

// TODO-02: Gate this endpoint with the ExpressCheckout flag (WithFeatureGate); it must answer 404 while the flag is off.
app.MapGet("/checkout/express", () => "express checkout");

// TODO-03: Return "promo on" when the Promo flag is enabled, otherwise "promo off". The time window lives in configuration, not here.
app.MapGet("/promo", () => "promo off");

// TODO-04: Ask IVariantFeatureManager for the variant of the Banner flag and return "<variant name>|<variant configuration value>".
app.MapGet("/banner", () => "none|");

// TODO-05: Return every defined flag name with its current state (GetFeatureNamesAsync), sorted by name.
app.MapGet("/flags", () => new SortedDictionary<string, bool>());

app.Run();

public partial class Program;
