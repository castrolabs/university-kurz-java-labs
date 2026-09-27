# Services: ClusterIP and NodePort (Solution)

## Overview

`shop` selects `app: shop, tier: web` and targets the named port `http`; `shop-public` uses node port 30080. Three tests check the EndpointSlice, the ClusterIP and the NodePort.

## Key Concepts

- **A Service selects Pods by label, not by name.** A selector that matches nothing is accepted without any warning; the only sign is an empty EndpointSlice. Checking it is the first step whenever a Service does not answer.
- **An empty Service and a wrong `targetPort` look the same from a client:** `Connection refused`. The EndpointSlice tells them apart: no addresses means the selector, addresses present means the port.
- **Name the container port and target the name.** `targetPort: http` follows the container if it moves from 8080 to another port, and the Service `port` (80) can stay what clients expect.
- **NodePorts come from a fixed range** (30000-32767 by default), and the API server rejects anything outside it. Leave `nodePort` empty to get a free one, and set it only when something outside the cluster must know it.

## Running the Solution

```bash
mvn -pl kubernetes-concepts/services-clusterip-and-nodeport-solution test
```
