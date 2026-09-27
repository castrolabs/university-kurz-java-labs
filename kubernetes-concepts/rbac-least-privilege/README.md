# RBAC and Least Privilege

## Goal

Replace two overly generous grants with least-privilege RBAC, and prove it with `kubectl auth can-i` on a real Kubernetes cluster (k3s, started by Testcontainers, with RBAC enforced).

## Prerequisites

- Docker running locally (the tests start a `rancher/k3s` container)
- The article "RBAC and Least Privilege"

## Task

`src/main/resources/k8s/rbac.yaml` defines two ServiceAccounts in namespace `team-a`, described in the file header:

- `deployer` (CI): roll out Deployments in `team-a` and read Pods and their logs, nothing else.
- `viewer` (developers): read everything in `team-a` except Secrets, change nothing.

The tests ask the API server, with `kubectl auth can-i --as=system:serviceaccount:team-a:<name>`, what each identity may do: what must be allowed and, just as important, what must be denied (deleting Deployments, reading Secrets, `pods/exec`, creating RoleBindings, anything in `team-b`, cluster-wide resources).

You only edit `rbac.yaml`.

## Instructions

Run the tests first: every "must be allowed" check passes, and the "must not" checks fail.

- TODO-00: Replace the `cluster-admin` ClusterRoleBinding with a Role in `team-a` and a RoleBinding.
- TODO-01: Bind the right built-in ClusterRole to `viewer` instead of `edit`.

Run the tests until they all pass.

## Running the Lab

```bash
mvn -pl kubernetes-concepts/rbac-least-privilege test
```

## Bonus (Optional)

- TODO-02 (optional): Run `kubectl auth can-i --list -n team-a --as=system:serviceaccount:team-a:deployer` inside the cluster and compare it with the Role. Then give the deployer `create` on `pods` only and explain why that alone lets it read every Secret in the namespace.
