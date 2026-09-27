# NetworkPolicies and Default Deny

## Goal

Lock down a shared namespace so that only one kind of Pod in one other namespace can reach its database, and keep a strict egress policy without breaking DNS. The tests open real connections between Pods on a real Kubernetes cluster (k3s, started by Testcontainers, whose built-in controller enforces NetworkPolicies).

## Prerequisites

- Docker running locally (the tests start a `rancher/k3s` container and pull `busybox` inside it)
- The article "NetworkPolicies and Default Deny"

## Task

The test deploys (from `src/test/resources/k8s/workloads.yaml`, given):

- in `shared`: `db` and `cache` (HTTP on 8080), plus a Pod `impostor` labelled `app=api`;
- in `team-a`: a Pod `api` (`app=api`);
- in `team-b`: a Pod `client`.

Then it applies your `src/main/resources/k8s/policies.yaml` and checks, with real HTTP requests, that `team-a/api` reaches `db.shared` by name, and that nothing else reaches `db` or `cache`. You only edit `policies.yaml`.

## Instructions

Run the tests first. Four things are wrong at once: `team-a/api` cannot even resolve names, `team-b` reaches `cache`, and `shared/impostor` reaches both services.

- TODO-00: Add a default deny for incoming traffic in `shared`.
- TODO-01: Fix the selectors of `allow-api-to-db` so they mean "Pods labelled `app=api` **in** `team-a`".
- TODO-02: Allow `team-a`'s `api` Pods to query DNS and to reach `db` on port 8080.

Run the tests until they all pass.

## Running the Lab

```bash
mvn -pl kubernetes-concepts/network-policies-default-deny test
```

## Bonus (Optional)

- TODO-03 (optional): Remove the DNS rule from your solution and run `nslookup db.shared` from `team-a/api`. What error does the application see, and why does it look like a DNS problem rather than a network policy problem?
