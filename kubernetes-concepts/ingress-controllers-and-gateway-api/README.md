# Ingress Controllers and the Gateway API

## Goal

Route two hostnames to two Services through the cluster's ingress controller: one with an Ingress, one with a Gateway API `HTTPRoute`. The tests run on a real Kubernetes cluster (k3s, started by Testcontainers) with its bundled Traefik, and send real HTTP requests with different `Host` headers.

## Prerequisites

- Docker running locally (the tests start a `rancher/k3s` container, which installs Traefik and pulls `busybox`)
- The article "Ingress Controllers and the Gateway API"

## Task

The test starts k3s with Traefik and its Gateway API provider enabled (`src/test/resources/k8s/traefik-config.yaml`, given), deploys `shop` and `blog` in the namespace `web` (`apps.yaml`, given), and applies your `src/main/resources/k8s/routes.yaml`. Through Traefik, it checks that:

- `shop.lab/` and `shop.lab/cart/` answer with the shop's pages;
- `blog.lab/` answers with the blog, routed by an `HTTPRoute` (not an Ingress);
- any other host gets a 404;
- the platform's `traefik-gateway` in `kube-system` is unchanged.

You only edit `routes.yaml`.

## Instructions

Run the tests first. The failure message shows the HTTPRoute's status conditions.

- TODO-00: Make every page of the shop reachable.
- TODO-01: Make the blog route work without touching `kube-system`.

Run the tests until they all pass.

## Running the Lab

```bash
mvn -pl kubernetes-concepts/ingress-controllers-and-gateway-api test
```

## Bonus (Optional)

- TODO-02 (optional): Instead of a Gateway per team, the platform team could let routes from `web` attach to `traefik-gateway`. Which field of the Gateway would they change, and what would they give up?
