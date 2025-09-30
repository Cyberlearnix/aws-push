#!/usr/bin/env bash
set -euo pipefail

# Build (Gradle) + Git push in a single script
#
# Usage:
#   scripts/build_and_push.sh -m "commit message" [-b <branch>] [-r <remote>] [--skip-tests] [--force]
# Examples:
#   scripts/build_and_push.sh -m "feat: users basic list"
#   scripts/build_and_push.sh -m "fix: X" -b under-review --skip-tests
#   scripts/build_and_push.sh -m "wip" -r origin --force

REMOTE="origin"
BRANCH=""
MESSAGE=""
SKIP_TESTS=false
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
    --skip-tests)
      SKIP_TESTS=true
      shift 1
      ;;
    --force)
      FORCE=true
      shift 1
      ;;
    -h|--help)
      echo "Usage: $0 -m \"commit message\" [-b <branch>] [-r <remote>] [--skip-tests] [--force]"
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

# Ensure inside a git repo
if ! git rev-parse --is-inside-work-tree >/dev/null 2>&1; then
  echo "Error: not inside a git repository" >&2
  exit 1
fi

# Determine branch if not provided
if [[ -z "$BRANCH" ]]; then
  BRANCH=$(git rev-parse --abbrev-ref HEAD)
fi

echo "[Step 1/3] Building project with Gradle..."
if [[ ! -x "./gradlew" ]]; then
  chmod +x ./gradlew || true
fi

if [[ "$SKIP_TESTS" == true ]]; then
  ./gradlew clean build -x test
else
  ./gradlew clean build
fi

echo "[Step 2/3] Staging and committing changes..."
git add -A
if ! git diff --cached --quiet; then
  git commit -m "$MESSAGE"
else
  echo "No staged changes to commit; will push existing commits."
fi

echo "[Step 3/3] Pushing to $REMOTE $BRANCH..."
git fetch "$REMOTE" --prune

# Detect upstream
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
  echo "Running: git push ${PUSH_FLAGS[*]:-} ${PUSH_ARGS[*]}"
  if git push ${PUSH_FLAGS[*]:-} "${PUSH_ARGS[@]}"; then
    echo "Push succeeded."
    return 0
  fi
  return 1
}

if attempt_push; then
  exit 0
fi

echo "Initial push failed. Attempting to auto-resolve..."
if [[ "$FORCE" == true ]]; then
  echo "Force-with-lease enabled; retrying push..."
  git push --force-with-lease "${PUSH_ARGS[@]}"
  echo "Force push succeeded."
  exit 0
else
  echo "Rebasing onto $REMOTE/$BRANCH..."
  git pull --rebase "$REMOTE" "$BRANCH"
  echo "Retrying push..."
  if attempt_push; then
    exit 0
  else
    echo "Push failed even after rebase. Re-run with --force if you intend to overwrite remote (force-with-lease)." >&2
    exit 1
  fi
fi
