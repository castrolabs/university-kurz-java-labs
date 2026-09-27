# Deploying from a CI Pipeline

## Goal

Write the deploy step of a pipeline so that the job fails when a release does not come up, the previous release keeps running, and the pipeline's credentials can do nothing but deploy this app. The tests run it with a ServiceAccount token against a real Kubernetes cluster (k3s, started by Testcontainers).

## Prerequisites

- Docker running locally (the tests start a `rancher/k3s` container and pull `busybox` inside it)
- The article "Deploying from a CI Pipeline"

## Task

The test applies your `src/main/resources/k8s/ci-rbac.yaml` (the pipeline's ServiceAccount `ci-deployer` and its permissions), builds a kubeconfig holding only that ServiceAccount's token, and runs your `src/main/resources/deploy.sh` with it twice, as two pipeline runs:

1. `IMAGE=busybox:1.37`: must succeed, with 2 available replicas;
2. `IMAGE=busybox:0.0-does-not-exist`: must fail the job, and leave `busybox:1.37` in the Deployment.

It also checks that the pipeline can patch Deployments in `shop`, but cannot read Secrets, deploy to other namespaces or create cluster role bindings.

## Instructions

Run the tests first. The broken release reports success.

- TODO-00: Make `deploy.sh` wait for the rollout, roll back and fail when it does not complete.
- TODO-01: Replace `cluster-admin` with the permissions `deploy.sh` actually uses.

Run the tests until they all pass.

## Running the Lab

```bash
mvn -pl kubernetes-concepts/deploying-from-ci-pipeline test
```

## Bonus (Optional)

- TODO-02 (optional): The token in the test lives one hour. How would a real pipeline get short-lived credentials without storing a token at all (look up OIDC federation for your CI system), and what does it change if the CI system is compromised?
