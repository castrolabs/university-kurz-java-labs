using Microsoft.Extensions.DependencyInjection;

namespace DiLab;

public sealed class RequestState
{
    public Guid Id { get; } = Guid.NewGuid();
}

public interface ISystemClock
{
    DateTimeOffset UtcNow { get; }
}

public sealed class SystemClock : ISystemClock
{
    public DateTimeOffset UtcNow => DateTimeOffset.UtcNow;
}

public sealed class ReceiptNumberGenerator
{
    public string Next() => $"R-{Guid.NewGuid():N}"[..10];
}

public sealed class FakeDb : IDisposable
{
    public Guid Id { get; } = Guid.NewGuid();
    public bool Seeded { get; private set; }
    public bool IsDisposed { get; private set; }

    public void Seed() => Seeded = true;

    public string FindGameName(int id) => $"Game {id}";

    public void Dispose() => IsDisposed = true;
}

public sealed record CatalogLookup(string Name, Guid DbId);

public sealed class CatalogReader(IServiceScopeFactory scopes)
{
    public CatalogLookup Lookup(int id)
    {
        using var scope = scopes.CreateScope();
        var db = scope.ServiceProvider.GetRequiredService<FakeDb>();
        return new CatalogLookup(db.FindGameName(id), db.Id);
    }
}

public interface IPaymentGateway
{
    string Name { get; }
}

public sealed class StripeGateway : IPaymentGateway
{
    public string Name => "stripe";
}

public sealed class PaypalGateway : IPaymentGateway
{
    public string Name => "paypal";
}

public sealed class CheckoutService([FromKeyedServices("stripe")] IPaymentGateway gateway)
{
    public string GatewayName => gateway.Name;
}
