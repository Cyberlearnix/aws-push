#!/bin/bash

# Script to build JAR files for all Java services

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

echo -e "${GREEN}🔨 Building JAR files for CyberLMS services...${NC}"

# Set error handling
set -e

# Function to build JAR for a service
build_jar() {
    local service_name=$1
    local service_path=$2
    
    echo -e "\n${CYAN}Building $service_name...${NC}"
    
    # Check if service directory exists
    if [[ ! -d "$service_path" ]]; then
        echo -e "${RED}✗ Service directory $service_path not found${NC}"
        return 1
    fi
    
    # Build using Gradle
    if [[ -f "$service_path/gradlew" ]]; then
        # Service has its own Gradle wrapper
        cd "$service_path"
        echo -e "${YELLOW}Using local Gradle wrapper...${NC}"
        if ./gradlew build -x test; then
            echo -e "${GREEN}✓ Built $service_name JAR${NC}"
        else
            echo -e "${RED}✗ Failed to build $service_name JAR${NC}"
            cd ..
            return 1
        fi
        cd ..
    else
        # Use root Gradle wrapper
        echo -e "${YELLOW}Using root Gradle wrapper...${NC}"
        if ./gradlew ":$service_name:build" -x test; then
            echo -e "${GREEN}✓ Built $service_name JAR${NC}"
        else
            echo -e "${RED}✗ Failed to build $service_name JAR${NC}"
            return 1
        fi
    fi
    
    # Show JAR location
    jar_file=$(find "$service_path/build/libs" -name "*.jar" 2>/dev/null | head -1)
    if [[ -n "$jar_file" ]]; then
        echo -e "${GREEN}  JAR location: $jar_file${NC}"
    fi
}

# Check if Gradle wrapper exists
if [[ ! -f "./gradlew" ]]; then
    echo -e "${RED}✗ Gradle wrapper not found in root directory${NC}"
    exit 1
fi

# Make Gradle wrapper executable
chmod +x ./gradlew

# Java services to build
declare -A java_services=(
    ["user-service"]="user-service"
    ["student_service"]="student_service"
    ["instructor-service"]="instructor-service"
    ["order-service"]="order-service"
    ["eureka-server"]="eureka-server"
    ["config-server"]="config-server"
    ["api-gateway"]="api-gateway"
)

echo -e "\n${CYAN}Building JAR files for Java services...${NC}"

# Build each service
for service in "${!java_services[@]}"; do
    build_jar "$service" "${java_services[$service]}"
done

echo -e "\n${GREEN}🎉 All JAR files built successfully!${NC}"

# List all built JARs
echo -e "\n${CYAN}Built JAR files:${NC}"
for service in "${!java_services[@]}"; do
    service_path="${java_services[$service]}"
    jar_files=$(find "$service_path/build/libs" -name "*.jar" 2>/dev/null || true)
    if [[ -n "$jar_files" ]]; then
        echo -e "${GREEN}$service:${NC}"
        echo "$jar_files" | sed 's/^/  /'
    fi
done

echo -e "\n${YELLOW}To run a specific service:${NC}"
echo "java -jar <service-path>/build/libs/<service-name>.jar"

echo -e "\n${YELLOW}To build Docker images with these JARs:${NC}"
echo "./build-docker-images.sh"
