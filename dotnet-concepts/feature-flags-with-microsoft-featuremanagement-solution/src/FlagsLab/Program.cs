using FlagsLab;
using Microsoft.FeatureManagement;
using Microsoft.FeatureManagement.AspNetCore;

var builder = WebApplication.CreateBuilder(args);

// Flags come from the "feature_management" section of IConfiguration (appsettings.json here).
builder.Services.AddFeatureManagement()
    .WithTargeting<HeaderTargetingContextAccessor>();
builder.Services.AddHttpContextAccessor();
builder.Services.AddControllers();

var app = builder.Build();

app.MapControllers();

app.MapGet("/pricing", async (IFeatureManager features) =>
    await features.IsEnabledAsync(Flags.NewPricing) ? "price v2" : "price v1");

app.MapGet("/checkout/express", () => "express checkout")
    .WithFeatureGate(Flags.ExpressCheckout);

app.MapGet("/promo", async (IFeatureManager features) =>
    await features.IsEnabledAsync(Flags.Promo) ? "promo on" : "promo off");

app.MapGet("/banner", async (IVariantFeatureManager features, CancellationToken ct) =>
{
    var variant = await features.GetVariantAsync(Flags.Banner, ct);
    return $"{variant?.Name ?? "none"}|{variant?.Configuration?.Value}";
});

app.MapGet("/flags", async (IFeatureManager features) =>
{
    var result = new SortedDictionary<string, bool>();
    await foreach (var name in features.GetFeatureNamesAsync())
        result[name] = await features.IsEnabledAsync(name);
    return result;
});

app.Run();

public partial class Program;
