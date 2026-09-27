# Volumes and Mount Conflicts (Solution)

## Overview

`app.yaml` disables the ServiceAccount token automount, and `web.yaml` mounts the single `extra.conf` key with `subPath`. Five tests verify both Pods against a real k3s cluster.

## Key Concepts

- **The failing mount in `app` is the ServiceAccount token, not the Secret.** On Debian and Ubuntu images `/var/run` is a symlink to `/run`. The runtime mounts the read-only Secret at `/run/secrets`, then has to create `/var/run/secrets/kubernetes.io/serviceaccount` for the token, which now lies inside a read-only filesystem: `StartError`, exit code 128, empty logs. `automountServiceAccountToken: false` removes the second mount, and a credential the app never needed. Mounting the Secret at another path (`/etc/secrets`) would also work.
- **A volume mounted on a directory replaces the whole directory.** The ConfigMap at `/etc/nginx` left only `extra.conf` there, so `nginx.conf` was gone and nginx exited. Mounting at `/etc/nginx/conf.d` would start nginx but hide `default.conf`, which is why `defaultSiteStillWorks` exists.
- **`subPath` mounts one file and leaves the directory alone.** The price is that subPath content is a snapshot: when the ConfigMap changes, full-directory mounts are updated by the kubelet after about a minute, subPath mounts never are (TODO-02).
- **"Ready" is not "healthy" without a readiness probe.** nginx is Ready for a fraction of a second before it crashes, which is why the tests check the restart count, not only the Ready condition.

## Running the Solution

```bash
mvn -pl kubernetes-concepts/volumes-and-mount-conflicts-solution test
```
