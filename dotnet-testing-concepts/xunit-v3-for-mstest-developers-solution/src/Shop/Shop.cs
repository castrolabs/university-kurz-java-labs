namespace Shop;

public sealed class UnknownProductException(string sku)
    : ArgumentException($"Unknown product '{sku}'.", nameof(sku));

// Expensive to build in real life, read-only afterwards: the kind of thing a test fixture shares.
public sealed class PriceCatalog
{
    private static int _created;

    private readonly Dictionary<string, decimal> _prices = new() { ["apple"] = 0.50m, ["pear"] = 0.80m };

    public PriceCatalog() => Interlocked.Increment(ref _created);

    // How many catalogs the process has built so far.
    public static int Created => Volatile.Read(ref _created);

    public decimal PriceOf(string sku) =>
        _prices.TryGetValue(sku, out var price) ? price : throw new UnknownProductException(sku);
}

public sealed class Basket(PriceCatalog catalog)
{
    private readonly Dictionary<string, int> _quantities = [];

    public IReadOnlyDictionary<string, int> Quantities => _quantities;

    public decimal Total => _quantities.Sum(line => catalog.PriceOf(line.Key) * line.Value);

    public void Add(string sku, int quantity)
    {
        ArgumentOutOfRangeException.ThrowIfNegativeOrZero(quantity);
        catalog.PriceOf(sku);
        _quantities[sku] = _quantities.GetValueOrDefault(sku) + quantity;
    }
}

// A single-writer resource, like one connection to a legacy reporting database: two users at once is an error.
public sealed class ReportStore
{
    private int _users;

    public IDisposable Use()
    {
        if (Interlocked.Increment(ref _users) > 1)
        {
            Interlocked.Decrement(ref _users);
            throw new InvalidOperationException("The report store is busy.");
        }
        return new Release(this);
    }

    private sealed class Release(ReportStore store) : IDisposable
    {
        public void Dispose() => Interlocked.Decrement(ref store._users);
    }
}
