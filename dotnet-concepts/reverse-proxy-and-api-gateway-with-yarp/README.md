# Reverse Proxy and API Gateway with YARP

## Goal

Put a YARP gateway in front of two backends and configure what a gateway is
for: path rewriting and header transforms, round-robin load balancing, passive
and active health checks, a rate limit per client and authentication at the edge.

## Prerequisites

- Minimal APIs and the ASP.NET Core middleware pipeline
- Basic `HttpClient` and HTTP status codes
- .NET 10 SDK

## Task

The tests start two real Kestrel servers on free ports (`TestBackend`) and the
gateway through `WebApplicationFactory<Program>`, passing the backend addresses
as configuration (`Backends:0`, `Backends:1`). The gateway is already wired in
`Program.cs`, and the policies `per-client` (rate limiter) and `apikey`
(authorization) already exist. What is missing is the proxy configuration in
`GatewayConfig.cs`: routes, one cluster and its health checks. Everything is
built with `RouteConfig` and `ClusterConfig` objects (`LoadFromMemory`), the
same model that `appsettings.json` binds to.

## Instructions

Complete the following TODOs in `src/GatewayLab/GatewayConfig.cs`:

- TODO-00: Route `api` to cluster `backends` for `/api/{**catch-all}`, with `PathRemovePrefix` set to `/api`.
- TODO-01: On that route, add the request header `X-Forwarded-By: gateway` and the response header `X-Gateway: yarp` as transforms.
- TODO-02: Use the `RoundRobin` load balancing policy in the cluster.
- TODO-03: Enable the passive health check with the `TransportFailureRate` policy and a 30 s reactivation period.
- TODO-04: Attach the `per-client` rate limiter policy to the `limited` route.
- TODO-05: Attach the `apikey` authorization policy to the `admin` route.
- TODO-06: Enable the active health check (`/health`, 1 s interval and timeout, `ConsecutiveFailures`).

Run the tests until they all pass.

## Running the Lab

```bash
dotnet test
```

## Bonus (Optional)

- TODO-07 (optional): Move the routes and the cluster into `appsettings.json` under `ReverseProxy` and use `LoadFromConfig`. What changes for the backend addresses the tests pass in?
- TODO-08 (optional): Add a `/slow` endpoint to `TestBackend` that waits 3 s, set `HttpRequest.ActivityTimeout` of the cluster to 1 s and call it through the gateway. Which status code does the client get?
