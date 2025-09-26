#!/usr/bin/env bash
set -euo pipefail
TAG="${TAG:-latest}"
IMAGE_LOCAL="cyberlms-user-service:${TAG}"
DOCKERFILE="user-service/Dockerfile"

echo "Building ${IMAGE_LOCAL}"
docker build -t "${IMAGE_LOCAL}" -f "${DOCKERFILE}" .

if [[ -n "${REGISTRY:-}" ]]; then
  IMAGE_REMOTE="${REGISTRY}/cyberlms-user-service:${TAG}"
  docker tag "${IMAGE_LOCAL}" "${IMAGE_REMOTE}"
  echo "Tagged as ${IMAGE_REMOTE}"
fi
