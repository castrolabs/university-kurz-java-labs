# Ingress Controllers and the Gateway API (Solution)

## Overview

The Ingress uses `pathType: Prefix`, and the namespace `web` gets its own `Gateway` (class `traefik`, listener on port 8000) that the `HTTPRoute` attaches to. Four tests send requests through Traefik and check the platform Gateway.

## Key Concepts

- **`Exact` vs `Prefix`.** `pathType: Exact` with `path: /` matches only `/`; every other page is a 404 from the controller, not from the app. `Prefix` matches whole path segments (`/` covers everything, `/cart` covers `/cart` and `/cart/...` but not `/carts`).
- **An Ingress needs a controller, and the class picks it.** k3s marks Traefik as the default `IngressClass`, so `ingressClassName` can be omitted there; on clusters with several controllers, set it.
- **Routes attach to Gateways only if the Gateway allows it.** The Gateway's listener decides which namespaces may attach (`allowedRoutes.namespaces.from: Same | All | Selector`). The route stays `Accepted=False` with reason `NotAllowedByListeners`, and Traefik returns 404, as if the route did not exist. Always read the route's `status.parents`.
- **Gateway API splits ownership.** The platform owns `GatewayClass` and shared `Gateway`s, application teams own `HTTPRoute`s (or their own Gateways, as here). With Ingress, one object mixes both concerns.
- **With Traefik, a listener's port is an entrypoint.** Port 8000 is Traefik's `web` entrypoint, which its Service publishes on port 80; a listener on a port with no entrypoint is not programmed.

## Running the Solution

```bash
mvn -pl kubernetes-concepts/ingress-controllers-and-gateway-api-solution test
```
