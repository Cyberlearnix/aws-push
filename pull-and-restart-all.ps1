#!/usr/bin/env pwsh
# CyberLMS Microservices - Pull and Restart All Services
# This script pulls the latest code from git and restarts all Docker services

param(
    [switch]$SkipGitPull,
    [switch]$SkipBuild,
    [switch]$Verbose
)

# Colors for output
$Red = "`e[31m"
$Green = "`e[32m"
$Yellow = "`e[33m"
$Blue = "`e[34m"
$Reset = "`e[0m"

function Write-ColorOutput {
    param([string]$Message, [string]$Color = $Reset)
    Write-Host "$Color$Message$Reset"
}

function Write-Step {
    param([string]$Message)
    Write-ColorOutput "🔄 $Message" $Blue
}

function Write-Success {
    param([string]$Message)
    Write-ColorOutput "✅ $Message" $Green
}

function Write-Warning {
    param([string]$Message)
    Write-ColorOutput "⚠️  $Message" $Yellow
}

function Write-Error {
    param([string]$Message)
    Write-ColorOutput "❌ $Message" $Red
}

# Check if Docker is running
Write-Step "Checking Docker status..."
try {
    docker version | Out-Null
    Write-Success "Docker is running"
} catch {
    Write-Error "Docker is not running. Please start Docker Desktop and try again."
    exit 1
}

# Git pull latest changes
if (-not $SkipGitPull) {
    Write-Step "Pulling latest changes from git..."
    try {
        git pull origin main
        if ($LASTEXITCODE -eq 0) {
            Write-Success "Git pull completed successfully"
        } else {
            Write-Warning "Git pull completed with warnings"
        }
    } catch {
        Write-Error "Failed to pull from git: $_"
        exit 1
    }
} else {
    Write-Warning "Skipping git pull as requested"
}

# Stop all services
Write-Step "Stopping all services..."
try {
    docker-compose -f docker-compose.prod.yml down
    Write-Success "All services stopped"
} catch {
    Write-Error "Failed to stop services: $_"
    exit 1
}

# Remove unused Docker resources
Write-Step "Cleaning up Docker resources..."
try {
    docker system prune -f
    Write-Success "Docker cleanup completed"
} catch {
    Write-Warning "Docker cleanup failed, continuing anyway"
}

# Build and start services
if (-not $SkipBuild) {
    Write-Step "Building and starting all services..."
    try {
        if ($Verbose) {
            docker-compose -f docker-compose.prod.yml up -d --build
        } else {
            docker-compose -f docker-compose.prod.yml up -d --build | Out-Null
        }
        
        if ($LASTEXITCODE -eq 0) {
            Write-Success "All services built and started successfully"
        } else {
            Write-Error "Failed to build and start services"
            exit 1
        }
    } catch {
        Write-Error "Failed to build and start services: $_"
        exit 1
    }
} else {
    Write-Step "Starting services without rebuild..."
    try {
        if ($Verbose) {
            docker-compose -f docker-compose.prod.yml up -d
        } else {
            docker-compose -f docker-compose.prod.yml up -d | Out-Null
        }
        
        if ($LASTEXITCODE -eq 0) {
            Write-Success "All services started successfully"
        } else {
            Write-Error "Failed to start services"
            exit 1
        }
    } catch {
        Write-Error "Failed to start services: $_"
        exit 1
    }
}

# Wait for services to be healthy
Write-Step "Waiting for services to become healthy..."
$maxWaitTime = 300 # 5 minutes
$waitTime = 0
$checkInterval = 10

do {
    Start-Sleep $checkInterval
    $waitTime += $checkInterval
    
    $unhealthyServices = docker-compose -f docker-compose.prod.yml ps --format "table {{.Name}}\t{{.Status}}" | Where-Object { $_ -match "unhealthy|starting" }
    
    if ($unhealthyServices.Count -eq 0) {
        Write-Success "All services are healthy!"
        break
    }
    
    if ($waitTime -ge $maxWaitTime) {
        Write-Warning "Timeout reached. Some services may still be starting up."
        break
    }
    
    Write-Host "⏳ Waiting for services to become healthy... ($waitTime/$maxWaitTime seconds)"
} while ($true)

# Show final status
Write-Step "Final service status:"
docker-compose -f docker-compose.prod.yml ps

Write-Success "🚀 Pull and restart completed!"
Write-ColorOutput "📊 Access the application at: http://localhost" $Green
Write-ColorOutput "🔍 Monitor services with: docker-compose -f docker-compose.prod.yml logs -f" $Blue

# Show service URLs
Write-ColorOutput "`n🌐 Service URLs:" $Blue
Write-Host "   Frontend:           http://localhost"
Write-Host "   API Gateway:        http://localhost:8080"
Write-Host "   User Service:       http://localhost:8081"
Write-Host "   Student Service:    http://localhost:8082"
Write-Host "   Instructor Service: http://localhost:8083"
Write-Host "   Order Service:      http://localhost:8084"
Write-Host "   Eureka Server:      http://localhost:8761"
Write-Host "   Config Server:      http://localhost:8888"
