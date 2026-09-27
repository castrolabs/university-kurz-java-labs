# Service DNS and Cross-Namespace Access

## Goal

Reach a Service in another namespace by name, and give an application that cannot be changed a local alias for it. The tests make real requests through the cluster DNS on a real Kubernetes cluster (k3s, started by Testcontainers).

## Prerequisites

- Docker running locally (the tests start a `rancher/k3s` container and pull `busybox` inside it)
- The article "Service DNS and Cross-Namespace Access"

## Task

The test deploys a database Service `db` in the namespace `shared` (`src/test/resources/k8s/shared.yaml`, given), then applies your `src/main/resources/k8s/team-a.yaml`, which runs two apps in `team-a`:

- `api` calls the URL in its `DB_URL` variable;
- `legacy` always calls `http://database:8080`, and you cannot change that.

Both must get `db ok` back from the one database in `shared`. You only edit `team-a.yaml`.

## Instructions

Run the tests first. Both apps fail with `wget: bad address`, which is how an NXDOMAIN looks from inside a container.

- TODO-00: Fix `DB_URL` for `api`.
- TODO-01: Make `database` resolve in `team-a` for `legacy`. The tests reject a second database and a Service with its own ClusterIP.

Run the tests until they all pass.

## Running the Lab

```bash
mvn -pl kubernetes-concepts/service-dns-and-cross-namespace-access test
```

## Bonus (Optional)

- TODO-02 (optional): Run `cat /etc/resolv.conf` in the `api` Pod. Which search domains make `db.shared` work, and how many DNS queries does `ndots:5` cause for a name like `example.com` before the real one?
