# Service DNS and Cross-Namespace Access (Solution)

## Overview

`api` uses `http://db.shared:8080`, and `team-a` gets an `ExternalName` Service called `database` that points at `db.shared.svc.cluster.local`. Three tests call the database from both apps and check that nothing was duplicated.

## Key Concepts

- **A short name resolves only in the Pod's own namespace.** `db` becomes `db.team-a.svc.cluster.local` through the first search domain, which does not exist. `db.shared` works from any namespace through the `svc.cluster.local` search domain; the full `db.shared.svc.cluster.local` works everywhere and skips the search list.
- **`ExternalName` is a DNS alias.** It creates a CNAME record, with no selector, no endpoints and no ClusterIP, so the traffic goes straight to the real Service. It is the way to give an unchangeable name to something that lives elsewhere (another namespace, or a host outside the cluster).
- **ExternalName has limits.** The client still sends its own host name (`database:8080`) in HTTP `Host` headers and TLS SNI, which can break virtual hosts and certificate checks; and the port is not remapped.
- **`bad address` means the name did not resolve.** It is a DNS problem, not a connection problem, which narrows debugging to names and namespaces.

## Running the Solution

```bash
mvn -pl kubernetes-concepts/service-dns-and-cross-namespace-access-solution test
```
