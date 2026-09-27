# Kubeconfig, Contexts and Admin Access

## Goal

Merge the kubeconfig files of two clusters into one file with a context per cluster, although both files name everything `default`, and make the safe cluster the default. The tests start two real Kubernetes clusters (k3s, with Testcontainers) and use the merged file against both.

## Prerequisites

- Docker running locally (the tests start two `rancher/k3s` containers)
- The article "Kubeconfig, Contexts and Admin Access"

## Task

The test starts a `staging` and a `prod` cluster and copies their admin kubeconfigs to `/lab/staging.yaml` and `/lab/prod.yaml` on a machine that reaches both. As k3s writes them, both files call their cluster, user and context `default`. Then it runs your `src/main/resources/merge.sh`, which must write `/lab/merged.yaml`, and checks that:

- `--context staging` reaches the staging cluster and `--context prod` the prod cluster;
- the current context is `staging`;
- the file is self-contained (certificates embedded, no paths to other files).

You only edit `merge.sh`.

## Instructions

Run the tests first. The naive merge succeeds, and yet one cluster is gone.

- TODO-00: Rename the entries of each file before merging.
- TODO-01: Make `staging` the default context.

Run the tests until they all pass.

## Running the Lab

```bash
mvn -pl kubernetes-concepts/kubeconfig-contexts-and-admin-access test
```

## Bonus (Optional)

- TODO-02 (optional): Both files hold client certificates in `system:masters`. What can such a certificate do that no RBAC rule can take away, and what would you hand out instead to a colleague who only deploys to staging?
