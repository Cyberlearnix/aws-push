#!/bin/bash

# Bash script to build all Docker images for CyberLMS Microservices

echo "🚀 Building CyberLMS Microservices Docker Images..."

# Set error handling
set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Function to build Docker image
build_docker_image() {
    local service_name=$1
    local dockerfile_path=$2
    local build_context=${3:-.}
    
    echo -e "${YELLOW}Building $service_name...${NC}"
    
    if docker build -t "cyberlms-$service_name" -f "$dockerfile_path" "$build_context"; then
        echo -e "${GREEN}✓ Successfully built cyberlms-$service_name${NC}"
    else
        echo -e "${RED}✗ Failed to build cyberlms-$service_name${NC}"
        exit 1
    fi
}

# Build all services
echo -e "\n${CYAN}Building individual services...${NC}"

# Infrastructure services
build_docker_image "eureka-server" "eureka-server/Dockerfile"
build_docker_image "config-server" "config-server/Dockerfile"
build_docker_image "api-gateway" "api-gateway/Dockerfile"

# Business services
build_docker_image "user-service" "user-service/Dockerfile"
build_docker_image "student-service" "student_service/Dockerfile"
build_docker_image "instructor-service" "instructor-service/Dockerfile"
build_docker_image "order-service" "order-service/Dockerfile"

# Frontend
build_docker_image "frontend" "userservice-frontend/Dockerfile"

echo -e "\n${GREEN}🎉 All Docker images built successfully!${NC}"

# List all built images
echo -e "\n${CYAN}Built images:${NC}"
docker images | grep "cyberlms-"

echo -e "\n${YELLOW}To run the complete application:${NC}"
echo "docker-compose -f docker-compose.prod.yml up -d"

echo -e "\n${YELLOW}To push images to registry (update with your registry URL):${NC}"
echo "docker tag cyberlms-frontend:latest <your-registry>/cyberlms-frontend:latest"
echo "docker push <your-registry>/cyberlms-frontend:latest"
echo "# Repeat for all services..."
