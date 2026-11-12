using Yarp.ReverseProxy.Configuration;

namespace GatewayLab;

public static class GatewayConfig
{
    public static IReadOnlyList<RouteConfig> Routes() =>
    [
        // TODO-00: Route "api" to cluster "backends", matching "/api/{**catch-all}", with the transform
        //          PathRemovePrefix = "/api" so the backend sees "/whoami" and not "/api/whoami".
        // TODO-01: Add two more transforms to the same route: a RequestHeader "X-Forwarded-By" with Set = "gateway"
        //          and a ResponseHeader "X-Gateway" with Set = "yarp".
        new RouteConfig
        {
            RouteId = "limited",
            ClusterId = "backends",
            Match = new RouteMatch { Path = "/limited/{**catch-all}" },
            // TODO-04: Attach the rate limiter policy "per-client" (defined in Program.cs) to this route.
            Transforms = [new Dictionary<string, string> { ["PathRemovePrefix"] = "/limited" }],
        },
        new RouteConfig
        {
            RouteId = "admin",
            ClusterId = "backends",
            Match = new RouteMatch { Path = "/admin/{**catch-all}" },
            // TODO-05: Attach the authorization policy "apikey" (defined in Program.cs) to this route.
            Transforms = [new Dictionary<string, string> { ["PathRemovePrefix"] = "/admin" }],
        },
    ];

    public static IReadOnlyList<ClusterConfig> Clusters(IEnumerable<string> backendAddresses) =>
    [
        new ClusterConfig
        {
            ClusterId = "backends",
            // TODO-02: Spread requests across the destinations in turn with the "RoundRobin" load balancing policy.
            Destinations = backendAddresses
                .Select((address, i) => KeyValuePair.Create($"d{i}", new DestinationConfig { Address = address }))
                .ToDictionary(),
            HealthCheck = new HealthCheckConfig
            {
                // TODO-03: Enable the passive health check with the "TransportFailureRate" policy and a 30 s ReactivationPeriod.
                // TODO-06: Enable the active health check: Path "/health", Interval and Timeout of 1 s, Policy "ConsecutiveFailures".
            },
        },
    ];
}
