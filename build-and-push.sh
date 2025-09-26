#!/usr/bin/env bash
set -euo pipefail

# Single script to build and push all service images
# Usage examples:
#   REGISTRY=ghcr.io/your-org TAG=1.0 ./build-and-push.sh
#   REGISTRY=docker.io/your-namespace TAG=latest ./build-and-push.sh
# Notes:
#   - REGISTRY is REQUIRED
#   - TAG defaults to 'latest' if not provided

if [[ -z "${REGISTRY:-}" ]]; then
  echo "ERROR: REGISTRY is required (e.g., REGISTRY=ghcr.io/your-org)."
  exit 1
fi

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

echo "Building and pushing images to ${REGISTRY} with TAG=${TAG}"

for item in "${SERVICES[@]}"; do
  NAME="${item%%:*}"
  DOCKERFILE="${item#*:}"
  IMAGE_LOCAL="cyberlms-${NAME}:${TAG}"
  IMAGE_REMOTE="${REGISTRY}/cyberlms-${NAME}:${TAG}"

  echo "\n==> Building ${IMAGE_LOCAL} using ${DOCKERFILE}"
  docker build -t "${IMAGE_LOCAL}" -f "${DOCKERFILE}" .

  echo "Tagging ${IMAGE_LOCAL} as ${IMAGE_REMOTE}"
  docker tag "${IMAGE_LOCAL}" "${IMAGE_REMOTE}"

  echo "Pushing ${IMAGE_REMOTE}"
  docker push "${IMAGE_REMOTE}"

done

echo "\nAll images built and pushed successfully."
