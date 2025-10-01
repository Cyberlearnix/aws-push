#!/usr/bin/env bash
set -euo pipefail

# Usage:
#   REGISTRY=myrepo TAG=1.0 ./push-all.sh
# Requires images to be already built and tagged as cyberlms-<service>:<TAG>
# Default TAG=latest

if [[ -z "${REGISTRY:-}" ]]; then
  echo "ERROR: REGISTRY is required (e.g., REGISTRY=ghcr.io/your-org)."
  exit 1
fi

TAG="${TAG:-latest}"

SERVICES=(
  "eureka-server"
  "config-server"
  "api-gateway"
  "user-service"
  "student-service"
  "instructor-service"
  "order-service"
  "course-service"
  "payment-service"
  "frontend"
)

echo "Pushing all images to ${REGISTRY} with TAG=${TAG}"

for NAME in "${SERVICES[@]}"; do
  IMAGE_LOCAL="cyberlms-${NAME}:${TAG}"
  IMAGE_REMOTE="${REGISTRY}/cyberlms-${NAME}:${TAG}"

  echo "\n==> Tagging ${IMAGE_LOCAL} as ${IMAGE_REMOTE}"
  docker tag "${IMAGE_LOCAL}" "${IMAGE_REMOTE}"

  echo "Pushing ${IMAGE_REMOTE}"
  docker push "${IMAGE_REMOTE}"

done

echo "\nAll images pushed successfully."
