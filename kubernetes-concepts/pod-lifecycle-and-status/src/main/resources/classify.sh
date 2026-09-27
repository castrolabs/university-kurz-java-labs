#!/bin/sh
# Usage: classify.sh <namespace>
# Prints one line per Pod: "<name> <category>", where category is one of:
#   ready          running, and its Ready condition is True
#   not-ready      running, but not Ready (and never restarted)
#   unschedulable  no node can take it
#   image-pull     its image cannot be pulled
#   crash-loop     its container keeps exiting and being restarted
#   succeeded      finished, every container exited 0
#   failed         finished, and a container failed
set -eu

# TODO-00: The STATUS column is a summary made for humans. It hides some of these cases and changes
#          from one second to the next for others. Classify from the Pod's status fields instead
#          (phase, conditions, container states), e.g. with -o jsonpath.
kubectl -n "$1" get pods --no-headers | while read -r name ready status restarts age; do
  case "$status" in
    Running) echo "$name ready" ;;
    Pending) echo "$name unschedulable" ;;
    ImagePullBackOff) echo "$name image-pull" ;;
    CrashLoopBackOff) echo "$name crash-loop" ;;
    Completed) echo "$name succeeded" ;;
    Error) echo "$name failed" ;;
    *) echo "$name unknown" ;;
  esac
done
