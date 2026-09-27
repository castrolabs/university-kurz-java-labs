# kubectl exec, debug and port-forward

## Goal

Inspect a running container that has no shell and no tools, and reach it from outside the cluster network. You write a small script; the tests run it on the node of a real Kubernetes cluster (k3s, started by Testcontainers), where `kubectl` works but the cluster DNS does not.

## Prerequisites

- Docker running locally (the tests start a `rancher/k3s` container, which pulls `cgr.dev/chainguard/nginx` and `busybox`)
- The article "kubectl exec, debug and port-forward"

## Task

The test deploys `web` (`src/test/resources/k8s/web.yaml`, given): nginx from a minimal image with no shell, no `cat` and no package manager. Then it runs your `src/main/resources/inspect.sh` on the node. The script must print two lines:

1. the `worker_processes` line of `/etc/nginx/nginx.conf` **as the running container sees it**;
2. the `Server` header that `web` sends back.

The test also checks that no `kubectl port-forward` process is left running. You only edit `inspect.sh`, and you may not change the Deployment.

## Instructions

Run the tests first.

- TODO-00: Read the file from the running container. `kubectl exec` can only run programs that exist in the image.
- TODO-01: Fetch the header. The node is not inside the cluster network's DNS, so `http://web:8080` does not resolve there.

Run the tests until they all pass.

## Running the Lab

```bash
mvn -pl kubernetes-concepts/kubectl-exec-and-port-forward test
```

## Bonus (Optional)

- TODO-02 (optional): Run `kubectl get pod -l app=web -o yaml` after the tests. What did your debug containers leave behind in the Pod, and how do you get rid of them?
