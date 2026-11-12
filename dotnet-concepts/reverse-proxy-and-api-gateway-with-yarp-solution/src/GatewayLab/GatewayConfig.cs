using Yarp.ReverseProxy.Configuration;

namespace GatewayLab;

public static class GatewayConfig
{
    public static IReadOnlyList<RouteConfig> Routes() =>
    [
        new RouteConfig
        {
            RouteId = "api",
            ClusterId = "backends",
            Match = new RouteMatch { Path = "/api/{**catch-all}" },
            Transforms =
            [
                new Dictionary<string, string> { ["PathRemovePrefix"] = "/api" },
                new Dictionary<string, string> { ["RequestHeader"] = "X-Forwarded-By", ["Set"] = "gateway" },
                new Dictionary<string, string> { ["ResponseHeader"] = "X-Gateway", ["Set"] = "yarp" },
            ],
        },
        new RouteConfig
        {
            RouteId = "limited",
            ClusterId = "backends",
            Match = new RouteMatch { Path = "/limited/{**catch-all}" },
            RateLimiterPolicy = "per-client",
            Transforms = [new Dictionary<string, string> { ["PathRemovePrefix"] = "/limited" }],
        },
        new RouteConfig
        {
            RouteId = "admin",
            ClusterId = "backends",
            Match = new RouteMatch { Path = "/admin/{**catch-all}" },
            AuthorizationPolicy = "apikey",
            Transforms = [new Dictionary<string, string> { ["PathRemovePrefix"] = "/admin" }],
        },
    ];

    public static IReadOnlyList<ClusterConfig> Clusters(IEnumerable<string> backendAddresses) =>
    [
        new ClusterConfig
        {
            ClusterId = "backends",
            LoadBalancingPolicy = "RoundRobin",
            Destinations = backendAddresses
                .Select((address, i) => KeyValuePair.Create($"d{i}", new DestinationConfig { Address = address }))
                .ToDictionary(),
            HealthCheck = new HealthCheckConfig
            {
                Passive = new PassiveHealthCheckConfig
                {
                    Enabled = true,
                    Policy = "TransportFailureRate",
                    ReactivationPeriod = TimeSpan.FromSeconds(30),
                },
                Active = new ActiveHealthCheckConfig
                {
                    Enabled = true,
                    Interval = TimeSpan.FromSeconds(1),
                    Timeout = TimeSpan.FromSeconds(1),
                    Policy = "ConsecutiveFailures",
                    Path = "/health",
                },
            },
        },
    ];
}
