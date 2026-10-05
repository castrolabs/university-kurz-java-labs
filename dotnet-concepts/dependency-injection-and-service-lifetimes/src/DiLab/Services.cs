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

// TODO-03: This singleton keeps one FakeDb forever, which is a captive dependency once
//   FakeDb is scoped. Change it to depend on IServiceScopeFactory and create a scope
//   inside Lookup(), resolving FakeDb from that scope and disposing the scope afterwards.
public sealed class CatalogReader(FakeDb db)
{
    public CatalogLookup Lookup(int id) => new(db.FindGameName(id), db.Id);
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

// TODO-05 (optional): Ask the container for the "stripe" gateway with [FromKeyedServices].
public sealed class CheckoutService(IPaymentGateway gateway)
{
    public string GatewayName => gateway.Name;
}
