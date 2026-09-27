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

# One line per Pod, fields separated by "|" so that empty fields keep their place.
# (Pods with several containers would need every container checked; these have one.)
kubectl -n "$1" get pods -o jsonpath='{range .items[*]}{.metadata.name}|{.status.phase}|{.status.conditions[?(@.type=="PodScheduled")].reason}|{.status.conditions[?(@.type=="Ready")].status}|{.status.containerStatuses[0].state.waiting.reason}|{.status.containerStatuses[0].restartCount}{"\n"}{end}' |
while IFS='|' read -r name phase scheduled ready waiting restarts; do
  case "$phase" in
    Succeeded) category=succeeded ;;
    Failed) category=failed ;;
    *)
      if [ "$scheduled" = Unschedulable ]; then category=unschedulable
      elif [ "$waiting" = ErrImagePull ] || [ "$waiting" = ImagePullBackOff ]; then category=image-pull
      elif [ "${restarts:-0}" -gt 0 ] && [ "$ready" != True ]; then category=crash-loop
      elif [ "$ready" = True ]; then category=ready
      else category=not-ready
      fi ;;
  esac
  echo "$name $category"
done
