#!/bin/bash

# Simple script to build JAR files and push Docker images to registry

# Configuration
REGISTRY_URL=${1:-"your-registry-url"}
TAG=${2:-"latest"}

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

echo -e "${GREEN}🚀 Building JAR files and pushing to registry: $REGISTRY_URL${NC}"

# Check if registry URL is provided
if [[ "$REGISTRY_URL" == "your-registry-url" ]]; then
    echo -e "${RED}Error: Please provide registry URL${NC}"
    echo "Usage: $0 <registry-url> [tag]"
    echo "Example: $0 123456789.dkr.ecr.us-east-1.amazonaws.com latest"
    exit 1
fi

# Services with their build contexts
declare -A services=(
    ["user-service"]="user-service"
    ["student-service"]="student_service"
    ["instructor-service"]="instructor-service"
    ["order-service"]="order-service"
    ["eureka-server"]="eureka-server"
    ["config-server"]="config-server"
    ["api-gateway"]="api-gateway"
    ["frontend"]="userservice-frontend"
)

# Function to build and push service
build_and_push() {
    local service_name=$1
    local build_context=$2
    local image_name="cyberlms-$service_name"
    local full_tag="$REGISTRY_URL/$image_name:$TAG"
    
    echo -e "\n${CYAN}Processing $service_name...${NC}"
    
    # Build Docker image (which includes JAR building)
    echo -e "${YELLOW}Building Docker image for $service_name...${NC}"
    if docker build -t "$image_name" -f "$build_context/Dockerfile" .; then
        echo -e "${GREEN}✓ Built $image_name${NC}"
    else
        echo -e "${RED}✗ Failed to build $image_name${NC}"
        return 1
    fi
    
    # Tag for registry
    echo -e "${YELLOW}Tagging image...${NC}"
    if docker tag "$image_name" "$full_tag"; then
        echo -e "${GREEN}✓ Tagged as $full_tag${NC}"
    else
        echo -e "${RED}✗ Failed to tag $image_name${NC}"
        return 1
    fi
    
    # Push to registry
    echo -e "${YELLOW}Pushing to registry...${NC}"
    if docker push "$full_tag"; then
        echo -e "${GREEN}✓ Pushed $full_tag${NC}"
    else
        echo -e "${RED}✗ Failed to push $full_tag${NC}"
        return 1
    fi
}

# Build and push all services
echo -e "\n${CYAN}Building and pushing all services...${NC}"

for service in "${!services[@]}"; do
    build_and_push "$service" "${services[$service]}"
done

echo -e "\n${GREEN}🎉 All services built and pushed successfully!${NC}"

# Show pushed images
echo -e "\n${CYAN}Pushed images:${NC}"
for service in "${!services[@]}"; do
    echo "$REGISTRY_URL/cyberlms-$service:$TAG"
done

echo -e "\n${YELLOW}To deploy using docker-compose:${NC}"
echo "1. Update image URLs in docker-compose.prod.yml"
echo "2. Run: docker-compose -f docker-compose.prod.yml up -d"
