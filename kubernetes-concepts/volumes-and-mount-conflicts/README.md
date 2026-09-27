# Volumes and Mount Conflicts

## Goal

Fix two Pods that are broken by their own `volumeMounts`: one never starts (`StartError`), the other starts and crashes because a ConfigMap hid the image's configuration. The tests run against a real Kubernetes cluster (k3s, started by Testcontainers).

## Prerequisites

- Docker running locally (the tests start a `rancher/k3s` container and pull `ubuntu`, `nginx` and their layers inside it)
- The article "Volumes and Mount Conflicts"

## Task

The test creates the namespace, the Secret `app-secrets` (key `API_KEY`) and the ConfigMap `nginx-extra` (key `extra.conf`, a server block on port 8081), then applies:

- `src/main/resources/k8s/app.yaml`: an Ubuntu container that reads `/run/secrets/API_KEY`.
- `src/main/resources/k8s/web.yaml`: nginx that should serve the extra server block on 8081 **and** keep its default site on port 80.

You only edit those two files.

## Instructions

Run the tests once before changing anything. `readOnlySecretAtRunSecretsBreaksTheTokenMountOnUbuntu` passes and shows the exact runtime error of the "obvious" Pod. Read it carefully: the failing mount is not the Secret.

Complete the following TODOs:

- TODO-00 (`app.yaml`): Keep the Secret at `/run/secrets` and make the Pod start. The app never calls the Kubernetes API.
- TODO-01 (`web.yaml`): Mount only `extra.conf` into `/etc/nginx/conf.d/`, next to the image's `default.conf`.

Run the tests until they all pass.

## Running the Lab

From the project root:

```bash
mvn -pl kubernetes-concepts/volumes-and-mount-conflicts test
```

The first run pulls the k3s image (about 250 MB). With the starter files the run is slow (the tests wait for Pods that never become Ready); once fixed it takes around 30 seconds.

## Bonus (Optional)

- TODO-02 (optional): Update the ConfigMap (`kubectl -n example edit configmap nginx-extra`, change the response text) and wait two minutes. Does `wget -qO- 127.0.0.1:8081` inside the Pod change? Compare with a full-directory mount of the same ConfigMap at `/etc/extra`.
