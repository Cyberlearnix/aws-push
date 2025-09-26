#!/usr/bin/env bash
set -euo pipefail

if [[ -z "${REGISTRY:-}" ]]; then
  echo "ERROR: REGISTRY is required (e.g., REGISTRY=ghcr.io/your-org)."
  exit 1
fi
TAG="${TAG:-latest}"

IMAGE_LOCAL="cyberlms-course-service:${TAG}"
IMAGE_REMOTE="${REGISTRY}/cyberlms-course-service:${TAG}"

echo "Tagging ${IMAGE_LOCAL} as ${IMAGE_REMOTE}"
docker tag "${IMAGE_LOCAL}" "${IMAGE_REMOTE}"

echo "Pushing ${IMAGE_REMOTE}"
docker push "${IMAGE_REMOTE}"
