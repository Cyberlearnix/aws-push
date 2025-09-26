#!/bin/bash
# CyberLMS Microservices - Individual Service Restart Script
# This script restarts a specific service or multiple services

set -e  # Exit on any error

# Available services
AVAILABLE_SERVICES=("postgres" "redis" "config-server" "eureka-server" "user-service" "student-service" "instructor-service" "order-service" "api-gateway" "frontend")

# Default values
BUILD=false
VERBOSE=false
SERVICES=()

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

function print_step() {
    echo -e "${BLUE}🔄 $1${NC}"
}

function print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

function print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

function print_error() {
    echo -e "${RED}❌ $1${NC}"
}

function show_help() {
    echo "Usage: $0 [OPTIONS] SERVICE1 [SERVICE2 ...]"
    echo ""
    echo "Options:"
    echo "  --build            Rebuild the service before starting"
    echo "  --verbose          Show verbose output"
    echo "  -h, --help         Show this help message"
    echo ""
    echo "Available services:"
    printf "  %s\n" "${AVAILABLE_SERVICES[@]}"
    echo ""
    echo "Examples:"
    echo "  $0 user-service"
    echo "  $0 --build user-service student-service"
    echo "  $0 --verbose api-gateway"
}

function is_valid_service() {
    local service=$1
    for valid_service in "${AVAILABLE_SERVICES[@]}"; do
        if [[ "$service" == "$valid_service" ]]; then
            return 0
        fi
    done
    return 1
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --build)
            BUILD=true
            shift
            ;;
        --verbose)
            VERBOSE=true
            shift
            ;;
        -h|--help)
            show_help
            exit 0
            ;;
        -*)
            echo "Unknown option: $1"
            show_help
            exit 1
            ;;
        *)
            # This should be a service name
            if is_valid_service "$1"; then
                SERVICES+=("$1")
            else
                print_error "Invalid service: $1"
                echo "Available services: ${AVAILABLE_SERVICES[*]}"
                exit 1
            fi
            shift
            ;;
    esac
done

# Check if at least one service was specified
if [[ ${#SERVICES[@]} -eq 0 ]]; then
    print_error "No services specified"
    show_help
    exit 1
fi

# Check if Docker is running
print_step "Checking Docker status..."
if ! docker version >/dev/null 2>&1; then
    print_error "Docker is not running. Please start Docker and try again."
    exit 1
fi
print_success "Docker is running"

# Process each service
for service in "${SERVICES[@]}"; do
    print_step "Restarting service: $service"
    
    # Stop the service
    echo "  Stopping $service..."
    if ! docker-compose -f docker-compose.prod.yml stop "$service"; then
        print_error "Failed to stop service $service"
        continue
    fi
    
    # Remove the container
    echo "  Removing container for $service..."
    if ! docker-compose -f docker-compose.prod.yml rm -f "$service"; then
        print_error "Failed to remove container for service $service"
        continue
    fi
    
    # Start the service
    if [ "$BUILD" = true ]; then
        echo "  Building and starting $service..."
        if [ "$VERBOSE" = true ]; then
            docker-compose -f docker-compose.prod.yml up -d --build "$service"
        else
            docker-compose -f docker-compose.prod.yml up -d --build "$service" >/dev/null
        fi
    else
        echo "  Starting $service..."
        if [ "$VERBOSE" = true ]; then
            docker-compose -f docker-compose.prod.yml up -d "$service"
        else
            docker-compose -f docker-compose.prod.yml up -d "$service" >/dev/null
        fi
    fi
    
    if [ $? -eq 0 ]; then
        print_success "Service $service restarted successfully"
    else
        print_error "Failed to restart service $service"
        continue
    fi
done

# Wait for services to be healthy
print_step "Waiting for services to become healthy..."
MAX_WAIT_TIME=120  # 2 minutes
WAIT_TIME=0
CHECK_INTERVAL=5

while [ $WAIT_TIME -lt $MAX_WAIT_TIME ]; do
    sleep $CHECK_INTERVAL
    WAIT_TIME=$((WAIT_TIME + CHECK_INTERVAL))
    
    ALL_HEALTHY=true
    for service in "${SERVICES[@]}"; do
        STATUS=$(docker-compose -f docker-compose.prod.yml ps "$service" --format "{{.Status}}")
        if echo "$STATUS" | grep -E "(unhealthy|starting)" >/dev/null; then
            ALL_HEALTHY=false
            break
        fi
    done
    
    if [ "$ALL_HEALTHY" = true ]; then
        print_success "All specified services are healthy!"
        break
    fi
    
    if [ $WAIT_TIME -ge $MAX_WAIT_TIME ]; then
        print_warning "Timeout reached. Some services may still be starting up."
        break
    fi
    
    echo "⏳ Waiting for services to become healthy... ($WAIT_TIME/$MAX_WAIT_TIME seconds)"
done

# Show status of restarted services
print_step "Status of restarted services:"
for service in "${SERVICES[@]}"; do
    docker-compose -f docker-compose.prod.yml ps "$service"
done

print_success "🚀 Service restart completed!"

# Show usage examples
echo -e "\n${BLUE}📖 Usage Examples:${NC}"
echo "   Restart single service:     ./restart-service.sh user-service"
echo "   Restart multiple services:  ./restart-service.sh user-service student-service"
echo "   Restart with rebuild:       ./restart-service.sh --build api-gateway"
echo "   Restart with verbose:       ./restart-service.sh --verbose frontend"
