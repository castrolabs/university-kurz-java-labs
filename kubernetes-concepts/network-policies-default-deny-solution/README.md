# NetworkPolicies and Default Deny (Solution)

## Overview

`shared` gets a default deny for ingress, `allow-api-to-db` combines both selectors in one list item, and `team-a` gets an egress policy for its `api` Pods that allows DNS and `db`. Four tests make real HTTP requests between Pods on k3s.

## Key Concepts

- **A policy only restricts the Pods it selects.** `cache` was selected by nothing, so it was reachable from the whole cluster even though `db` was protected. A `podSelector: {}` default deny closes every Pod in the namespace, and allow rules open exactly what is needed.
- **AND vs OR is one dash.** Two items under `from` (`- namespaceSelector` and `- podSelector`) mean "any Pod in `team-a`, **or** any Pod labelled `app=api` in this namespace". One item with both keys means "Pods labelled `app=api` **in** `team-a`". The `impostor` Pod proves the difference.
- **Egress deny also blocks DNS.** Every name looks unknown, so applications report resolution errors, not network errors. Allow UDP and TCP 53 to the `kube-dns` Pods in `kube-system` before anything else.
- **Both sides must agree.** The connection from `team-a/api` to `db` needs the egress allow in `team-a` and the ingress allow in `shared`.
- **Allow rules may take a few seconds to be programmed**, which is why the positive test retries.

## Running the Solution

```bash
mvn -pl kubernetes-concepts/network-policies-default-deny-solution test
```
