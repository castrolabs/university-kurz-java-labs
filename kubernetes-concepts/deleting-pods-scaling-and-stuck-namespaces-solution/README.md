# Deleting Pods, Scaling and Stuck Namespaces (Solution)

## Overview

`ops.sh` scales `web` to 0 and then removes the finalizers from every object left in `old-team`, found by listing every namespaced resource type. Three tests check the script, the stopped app, and that the recreated namespace is empty.

## Key Concepts

- **Deleting a Pod that has an owner restarts it.** The ReplicaSet sees fewer Pods than it wants and creates new ones. To stop an app, change what the owner wants: `kubectl scale --replicas=0`, which keeps the Deployment and its history.
- **A namespace waits for its contents.** Deleting it deletes every object inside, and an object with a finalizer stays until its controller removes that finalizer. If the controller is gone, nothing ever will. The namespace condition `NamespaceFinalizersRemaining` names the finalizer and the resource type.
- **Remove the finalizer from the object, not from the namespace.** Force-finalizing the namespace makes it disappear while its objects stay in etcd, orphaned. Create a namespace with the same name and they come back, as the last test shows.
- **A finalizer usually stands for real cleanup** (a cloud load balancer, a volume, a DNS record). Before removing one by hand, check whether its controller is just down, and whether the external resource must be deleted manually.

## Running the Solution

```bash
mvn -pl kubernetes-concepts/deleting-pods-scaling-and-stuck-namespaces-solution test
```
