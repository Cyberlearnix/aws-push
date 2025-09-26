# PowerShell script to build all Docker images for CyberLMS Microservices

Write-Host "Building CyberLMS Microservices Docker Images..." -ForegroundColor Green

# Set error action preference
$ErrorActionPreference = "Stop"

# Function to build Docker image
function Build-DockerImage {
    param(
        [string]$ServiceName,
        [string]$DockerfilePath,
        [string]$BuildContext = "."
    )
    
    Write-Host "Building $ServiceName..." -ForegroundColor Yellow
    
    try {
        docker build -t "cyberlms-$ServiceName" -f $DockerfilePath $BuildContext
        Write-Host "✓ Successfully built cyberlms-$ServiceName" -ForegroundColor Green
    }
    catch {
        Write-Host "✗ Failed to build cyberlms-$ServiceName" -ForegroundColor Red
        Write-Host $_.Exception.Message -ForegroundColor Red
        exit 1
    }
}

# Build all services
Write-Host "`nBuilding individual services..." -ForegroundColor Cyan

# Infrastructure services
Build-DockerImage "eureka-server" "eureka-server/Dockerfile"
Build-DockerImage "config-server" "config-server/Dockerfile"
Build-DockerImage "api-gateway" "api-gateway/Dockerfile"

# Business services
Build-DockerImage "user-service" "user-service/Dockerfile"
Build-DockerImage "student-service" "student_service/Dockerfile"
Build-DockerImage "instructor-service" "instructor-service/Dockerfile"
Build-DockerImage "order-service" "order-service/Dockerfile"

# Frontend
Build-DockerImage "frontend" "userservice-frontend/Dockerfile"

Write-Host "`n🎉 All Docker images built successfully!" -ForegroundColor Green

# List all built images
Write-Host "`nBuilt images:" -ForegroundColor Cyan
docker images | Select-String "cyberlms-"

Write-Host "`nTo run the complete application:" -ForegroundColor Yellow
Write-Host "docker-compose -f docker-compose.prod.yml up -d" -ForegroundColor White

Write-Host "`nTo push images to registry (update with your registry URL):" -ForegroundColor Yellow
Write-Host "docker tag cyberlms-frontend:latest <your-registry>/cyberlms-frontend:latest" -ForegroundColor White
Write-Host "docker push <your-registry>/cyberlms-frontend:latest" -ForegroundColor White
Write-Host "# Repeat for all services..." -ForegroundColor Gray
