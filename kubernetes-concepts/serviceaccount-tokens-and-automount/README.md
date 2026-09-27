# ServiceAccount Tokens and Automount

## Goal

Remove an API credential from a Pod that never uses it, and give the one Pod that does call the API its own identity with a single permission. The tests call the real Kubernetes API from inside the Pods (k3s, started by Testcontainers).

## Prerequisites

- Docker running locally (the tests start a `rancher/k3s` container and pull `busybox` and `curlimages/curl` inside it)
- The articles "ServiceAccount Tokens and Automount" and "RBAC and Least Privilege"

## Task

`src/main/resources/k8s/app.yaml` runs two Pods in `team-a`: `web`, which never calls the API, and `watcher`, which lists ConfigMaps through the API. Someone made `watcher` work by giving the namespace's **default** ServiceAccount the `edit` role, so every Pod in the namespace now carries a token that can read and change almost everything, Secrets included.

The tests check, from inside the Pods: that `web` has no token at all, that `watcher` can list ConfigMaps (HTTP 200) but not Secrets (HTTP 403), that it runs as `config-watcher`, and that the default ServiceAccount has no permissions left.

You only edit `app.yaml`.

## Instructions

- TODO-00: Stop Kubernetes from mounting a token into `web`.
- TODO-01: Replace the `default-edit` binding with a dedicated ServiceAccount, a Role and a RoleBinding for the watcher.

Run the tests until they all pass.

## Running the Lab

```bash
mvn -pl kubernetes-concepts/serviceaccount-tokens-and-automount test
```

## Bonus (Optional)

- TODO-02 (optional): Decode the watcher's token (`cut -d. -f2 token | base64 -d`) and read its claims: `aud`, `exp` and the bound Pod name. What happens to the token when the Pod is deleted?
