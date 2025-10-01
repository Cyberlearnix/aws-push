#!/usr/bin/env bash
set -euo pipefail

# Usage:
#   REGISTRY=myrepo TAG=1.0 ./build-all.sh
# If REGISTRY is unset, images will be built locally as cyberlms-<service>:<TAG>
# Default TAG=latest

TAG="${TAG:-latest}"

SERVICES=(
  "eureka-server:eureka-server/Dockerfile"
  "config-server:config-server/Dockerfile"
  "api-gateway:api-gateway/Dockerfile"
  "user-service:user-service/Dockerfile"
  "student-service:student_service/Dockerfile"
  "instructor-service:instructor-service/Dockerfile"
  "order-service:order-service/Dockerfile"
  "course-service:course-service/Dockerfile"
  "payment-service:payment-service/Dockerfile"
  "frontend:userservice-frontend/Dockerfile"
)

echo "Building all images with TAG=${TAG}"

for item in "${SERVICES[@]}"; do
  NAME="${item%%:*}"
  DOCKERFILE="${item#*:}"
  IMAGE_LOCAL="cyberlms-${NAME}:${TAG}"

  echo "\n==> Building ${IMAGE_LOCAL} using ${DOCKERFILE}"
  docker build -t "${IMAGE_LOCAL}" -f "${DOCKERFILE}" .

  if [[ -n "${REGISTRY:-}" ]]; then
    IMAGE_REMOTE="${REGISTRY}/cyberlms-${NAME}:${TAG}"
    echo "Tagging ${IMAGE_LOCAL} as ${IMAGE_REMOTE}"
    docker tag "${IMAGE_LOCAL}" "${IMAGE_REMOTE}"
  fi

done

echo "\nAll images built successfully."
