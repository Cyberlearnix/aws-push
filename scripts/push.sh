#!/usr/bin/env bash
set -euo pipefail

# Simple, safe git push helper
# - Stages all changes
# - Commits with a message
# - Pushes to the current (or specified) branch
# - If remote is ahead, offers rebase or force-with-lease (if --force is provided)
#
# Usage:
#   scripts/push.sh -m "your commit message" [-b <branch>] [-r <remote>] [--force]
# Examples:
#   scripts/push.sh -m "feat: list users basic"
#   scripts/push.sh -m "fix: something" -b under-review
#   scripts/push.sh -m "wip" -r origin --force

REMOTE="origin"
BRANCH=""
MESSAGE=""
FORCE=false

while [[ $# -gt 0 ]]; do
  case "$1" in
    -m|--message)
      MESSAGE=${2-}
      shift 2
      ;;
    -b|--branch)
      BRANCH=${2-}
      shift 2
      ;;
    -r|--remote)
      REMOTE=${2-}
      shift 2
      ;;
    --force)
      FORCE=true
      shift 1
      ;;
    -h|--help)
      echo "Usage: $0 -m \"commit message\" [-b <branch>] [-r <remote>] [--force]"
      exit 0
      ;;
    *)
      echo "Unknown argument: $1" >&2
      exit 1
      ;;
  esac
done

if [[ -z "$MESSAGE" ]]; then
  echo "Error: commit message is required. Use -m \"message\"" >&2
  exit 1
fi

# Ensure we are inside a git repo
if ! git rev-parse --is-inside-work-tree >/dev/null 2>&1; then
  echo "Error: not inside a git repository" >&2
  exit 1
fi

# Determine branch if not provided
if [[ -z "$BRANCH" ]]; then
  BRANCH=$(git rev-parse --abbrev-ref HEAD)
fi

echo "Remote: $REMOTE"
echo "Branch: $BRANCH"

echo "Staging changes..."
git add -A

# Commit only if there is something staged
if ! git diff --cached --quiet; then
  echo "Committing..."
  git commit -m "$MESSAGE"
else
  echo "No staged changes to commit; proceeding to push."
fi

echo "Fetching $REMOTE..."
git fetch "$REMOTE" --prune

# Detect if upstream exists
set +e
UPSTREAM_REF=$(git rev-parse --abbrev-ref --symbolic-full-name @{u} 2>/dev/null)
UPSTREAM_STATUS=$?
set -e

PUSH_ARGS=("$REMOTE" "$BRANCH")

if [[ $UPSTREAM_STATUS -ne 0 ]]; then
  echo "No upstream set for $BRANCH. Will push with -u."
  PUSH_FLAGS=(-u)
else
  PUSH_FLAGS=()
fi

attempt_push() {
  echo "Pushing... git push ${PUSH_FLAGS[*]:-} ${PUSH_ARGS[*]}"
  if git push ${PUSH_FLAGS[*]:-} "${PUSH_ARGS[@]}"; then
    echo "Push succeeded."
    return 0
  fi
  return 1
}

if attempt_push; then
  exit 0
fi

# Handle non-fast-forward
echo "Initial push failed. Checking for non-fast-forward..."
if git rev-list --left-right --count "$BRANCH"..."$REMOTE/$BRANCH" >/dev/null 2>&1; then
  AHEAD_BEHIND=$(git rev-list --left-right --count "$BRANCH"..."$REMOTE/$BRANCH" | awk '{print $1" "$2}')
  echo "Divergence (local_ahead remote_ahead): $AHEAD_BEHIND"
fi

if [[ "$FORCE" == true ]]; then
  echo "Force-with-lease enabled; retrying push..."
  if git push --force-with-lease "${PUSH_ARGS[@]}"; then
    echo "Force push succeeded."
    exit 0
  else
    echo "Force push failed." >&2
    exit 1
  fi
else
  echo "Attempting rebase onto $REMOTE/$BRANCH..."
  git pull --rebase "$REMOTE" "$BRANCH"
  echo "Re-attempting push..."
  if attempt_push; then
    exit 0
  else
    echo "Push failed after rebase. Consider rerunning with --force if appropriate." >&2
    exit 1
  fi
fi
