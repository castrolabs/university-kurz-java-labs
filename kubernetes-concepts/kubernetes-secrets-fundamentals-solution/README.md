# Kubernetes Secrets Fundamentals (Solution)

## Overview

`db-creds` uses `stringData: {password: s3cret}` and `immutable: true`, and the `reports` Role allows only `get` on `resourceNames: [report-creds]`. Three tests check the login, an in-place change, and the permissions.

## Key Concepts

- **base64 is an encoding, not encryption.** Anyone who can read the Secret can decode it (`base64 -d`). The newline from `echo s3cret | base64` (`czNjcmV0Cg==`) is a real part of the value, so the password became `s3cret\n` and the database rejected it. Use `stringData` with the plain value, `printf %s`, or `kubectl create secret generic --from-literal`.
- **`immutable: true`** makes the API server reject any change to `data`, protects against accidental edits, and lets the kubelet stop watching the Secret (less load on large clusters). A rotation becomes a new Secret (`db-creds-v2`) plus a rollout, which is also easier to roll back.
- **`list` and `watch` are as powerful as `get` on everything.** They return complete objects, values included, for every Secret in the namespace. For a workload that needs one Secret, grant `get` with `resourceNames` (which cannot restrict `list`).
- **Who else can read Secrets:** anyone who can create Pods in the namespace can mount any Secret there, so "create pods" is effectively "read secrets"; keep that in mind when handing out `edit`.

## Running the Solution

```bash
mvn -pl kubernetes-concepts/kubernetes-secrets-fundamentals-solution test
```
