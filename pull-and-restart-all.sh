#!/bin/bash
# CyberLMS Microservices - Pull and Restart All Services
# This script pulls the latest code from git and restarts all Docker services

set -e  # Exit on any error

# Default values
SKIP_GIT_PULL=false
SKIP_BUILD=false
VERBOSE=false

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --skip-git-pull)
            SKIP_GIT_PULL=true
            shift
            ;;
        --skip-build)
            SKIP_BUILD=true
            shift
            ;;
        --verbose)
            VERBOSE=true
            shift
            ;;
        -h|--help)
            echo "Usage: $0 [OPTIONS]"
            echo "Options:"
            echo "  --skip-git-pull    Skip git pull step"
            echo "  --skip-build       Skip Docker build step"
            echo "  --verbose          Show verbose output"
            echo "  -h, --help         Show this help message"
            exit 0
            ;;
        *)
            echo "Unknown option: $1"
            exit 1
            ;;
    esac
done

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

# Check if Docker is running
print_step "Checking Docker status..."
if ! docker version >/dev/null 2>&1; then
    print_error "Docker is not running. Please start Docker and try again."
    exit 1
fi
print_success "Docker is running"

# Git pull latest changes
if [ "$SKIP_GIT_PULL" = false ]; then
    print_step "Pulling latest changes from git..."
    if git pull origin main; then
        print_success "Git pull completed successfully"
    else
        print_warning "Git pull completed with warnings"
    fi
else
    print_warning "Skipping git pull as requested"
fi

# Stop all services
print_step "Stopping all services..."
if docker-compose -f docker-compose.prod.yml down; then
    print_success "All services stopped"
else
    print_error "Failed to stop services"
    exit 1
fi

# Remove unused Docker resources
print_step "Cleaning up Docker resources..."
if docker system prune -f >/dev/null 2>&1; then
    print_success "Docker cleanup completed"
else
    print_warning "Docker cleanup failed, continuing anyway"
fi

# Build and start services
if [ "$SKIP_BUILD" = false ]; then
    print_step "Building and starting all services..."
    if [ "$VERBOSE" = true ]; then
        docker-compose -f docker-compose.prod.yml up -d --build
    else
        docker-compose -f docker-compose.prod.yml up -d --build >/dev/null
    fi
    
    if [ $? -eq 0 ]; then
        print_success "All services built and started successfully"
    else
        print_error "Failed to build and start services"
        exit 1
    fi
else
    print_step "Starting services without rebuild..."
    if [ "$VERBOSE" = true ]; then
        docker-compose -f docker-compose.prod.yml up -d
    else
        docker-compose -f docker-compose.prod.yml up -d >/dev/null
    fi
    
    if [ $? -eq 0 ]; then
        print_success "All services started successfully"
    else
        print_error "Failed to start services"
        exit 1
    fi
fi

# Wait for services to be healthy
print_step "Waiting for services to become healthy..."
MAX_WAIT_TIME=300  # 5 minutes
WAIT_TIME=0
CHECK_INTERVAL=10

while [ $WAIT_TIME -lt $MAX_WAIT_TIME ]; do
    sleep $CHECK_INTERVAL
    WAIT_TIME=$((WAIT_TIME + CHECK_INTERVAL))
    
    # Check if any services are unhealthy or starting
    UNHEALTHY_COUNT=$(docker-compose -f docker-compose.prod.yml ps --format "table {{.Name}}\t{{.Status}}" | grep -E "(unhealthy|starting)" | wc -l)
    
    if [ $UNHEALTHY_COUNT -eq 0 ]; then
        print_success "All services are healthy!"
        break
    fi
    
    if [ $WAIT_TIME -ge $MAX_WAIT_TIME ]; then
        print_warning "Timeout reached. Some services may still be starting up."
        break
    fi
    
    echo "⏳ Waiting for services to become healthy... ($WAIT_TIME/$MAX_WAIT_TIME seconds)"
done

# Show final status
print_step "Final service status:"
docker-compose -f docker-compose.prod.yml ps

print_success "🚀 Pull and restart completed!"
echo -e "${GREEN}📊 Access the application at: http://localhost${NC}"
echo -e "${BLUE}🔍 Monitor services with: docker-compose -f docker-compose.prod.yml logs -f${NC}"

# Show service URLs
echo -e "\n${BLUE}🌐 Service URLs:${NC}"
echo "   Frontend:           http://localhost"
echo "   API Gateway:        http://localhost:8080"
echo "   User Service:       http://localhost:8081"
echo "   Student Service:    http://localhost:8082"
echo "   Instructor Service: http://localhost:8083"
echo "   Order Service:      http://localhost:8084"
echo "   Eureka Server:      http://localhost:8761"
echo "   Config Server:      http://localhost:8888"
