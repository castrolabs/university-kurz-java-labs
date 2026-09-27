# Kubernetes Secrets Fundamentals

## Goal

Get three Secret basics right: the value you store is exactly the password (no stray newline), a Secret that must not change in place is immutable, and access to one Secret does not open all of them. The tests run on a real Kubernetes cluster (k3s, started by Testcontainers).

## Prerequisites

- Docker running locally (the tests start a `rancher/k3s` container and pull `busybox` inside it)
- The article "Kubernetes Secrets Fundamentals"

## Task

The test deploys (`src/test/resources/k8s/shop.yaml`, given) a "database" in `shop` that accepts user `app` with password `s3cret`, an `api` that logs in with the password from Secret `db-creds`, a Secret `report-creds`, and a ServiceAccount `reports`. Then it applies your `src/main/resources/k8s/secrets.yaml` and checks that:

- the `api` becomes ready (the database accepts its password);
- `db-creds` cannot be modified in place;
- `reports` can `get` `report-creds`, but cannot read `db-creds` or list Secrets.

You only edit `secrets.yaml`.

## Instructions

Run the tests first. The failure message shows the stored value in base64.

- TODO-00: Fix the password value.
- TODO-01: Make `db-creds` immutable.
- TODO-02: Narrow the `reports` Role to its own Secret.

Run the tests until they all pass.

## Running the Lab

```bash
mvn -pl kubernetes-concepts/kubernetes-secrets-fundamentals test
```

## Bonus (Optional)

- TODO-03 (optional): With `db-creds` immutable, rotate the password: what do you create, what do you change in the Deployment, and what happens to the old Secret?
