# RBAC and Least Privilege (Solution)

## Overview

The deployer gets a namespaced Role (deployments: get, list, watch, patch, update; pods and pods/log: get, list, watch) bound with a RoleBinding; the viewer gets the built-in `view` ClusterRole through a RoleBinding in `team-a`. Five tests check allowed and denied actions with `kubectl auth can-i`.

## Key Concepts

- **"It works" is the wrong test for permissions.** With `cluster-admin`, every allowed-action test passed; only testing what must be **denied** shows the problem. Keep `auth can-i` checks like these as unit tests of your RBAC.
- **Role + RoleBinding are namespaced; ClusterRoleBinding is everywhere.** A ClusterRole bound with a RoleBinding (like `view` here) is limited to that namespace.
- **Built-in roles:** `view` reads almost everything except Secrets, `edit` also writes (including Secrets), `admin` adds RBAC inside the namespace, `cluster-admin` is everything. `edit` for "read-only developers" gave them `patch` and Secrets.
- **Subresources are separate resources.** `pods/log`, `pods/exec` and `pods/portforward` must be listed explicitly, which is how the deployer can read logs without getting a shell.
- **Some permissions are more powerful than they look** (TODO-02): `create pods` lets you mount any Secret of the namespace; `get`/`list` on Secrets returns their full contents.

## Running the Solution

```bash
mvn -pl kubernetes-concepts/rbac-least-privilege-solution test
```
