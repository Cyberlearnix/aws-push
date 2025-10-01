#!/usr/bin/env bash
set -euo pipefail
TAG="${TAG:-latest}"
IMAGE_LOCAL="cyberlms-eureka-server:${TAG}"
DOCKERFILE="eureka-server/Dockerfile"

echo "Building ${IMAGE_LOCAL}"
docker build -t "${IMAGE_LOCAL}" -f "${DOCKERFILE}" .

if [[ -n "${REGISTRY:-}" ]]; then
  IMAGE_REMOTE="${REGISTRY}/cyberlms-eureka-server:${TAG}"
  docker tag "${IMAGE_LOCAL}" "${IMAGE_REMOTE}"
  echo "Tagged as ${IMAGE_REMOTE}"
fi
