# Kustomize: Bases, Overlays and Components (Solution)

## Overview

prod lists the base under `resources` and the metrics Component under `components`, and patches the Deployment by its base name (`web`) for replicas and `LOG_LEVEL`. dev uses the base plus one `LOG_LEVEL` patch; its copy of the Deployment is gone. Three tests render both overlays after a base change.

## Key Concepts

- **Resources vs components.** `resources` takes files or directories with `kind: Kustomization`; a directory with `kind: Component` there fails with `expected kind != 'Component'`. `components` applies a reusable set of changes (patches, resources, generators) to whatever the including overlay has built so far, so each overlay opts in.
- **Patches use the original names.** In the overlay that sets `namePrefix: prod-`, a patch still targets `web`; `prod-web` gives `no matches for Id Deployment.v1.apps/prod-web`.
- **Strategic merge patches merge lists by key.** Containers merge by `name`, env entries by `name`, so a patch listing only `LOG_LEVEL` changes that one variable and keeps everything else.
- **Copies drift.** An overlay with its own copy of a manifest renders correctly today and misses every later change to the base (here, the new image). An overlay should contain only differences.
- **Render before applying.** `kubectl kustomize <dir>` (or `kubectl diff -k`) shows exactly what would be applied, and is what a CI check should run for every overlay.

## Running the Solution

```bash
mvn -pl kubernetes-concepts/kustomize-bases-overlays-and-components-solution test
```
