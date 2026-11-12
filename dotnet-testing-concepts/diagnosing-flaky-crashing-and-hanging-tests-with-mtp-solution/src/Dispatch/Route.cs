namespace Dispatch;

// A route is a chain of stops. `Next` is the following stop, or null at the end.
public sealed class Stop(string name)
{
    public string Name { get; } = name;

    public Stop? Next { get; set; }
}

public static class Route
{
    // Number of stops from `start` to the end of the chain.
    public static int Length(Stop? start) => start is null ? 0 : 1 + Length(start.Next);

    // Same count, but refuses a chain longer than `limit` stops (a cycle is longer than any limit).
    public static int Length(Stop? start, int limit)
    {
        var count = 0;
        for (var stop = start; stop is not null; stop = stop.Next)
        {
            count++;
            if (count > limit)
            {
                throw new InvalidOperationException($"The route has more than {limit} stops. Is it a cycle?");
            }
        }

        return count;
    }
}
