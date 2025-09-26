#!/usr/bin/env pwsh
# CyberLMS Microservices - Individual Service Restart Script
# This script restarts a specific service or multiple services

param(
    [Parameter(Mandatory=$true)]
    [string[]]$Services,
    [switch]$Build,
    [switch]$Verbose
)

# Available services
$AvailableServices = @(
    "postgres",
    "redis", 
    "config-server",
    "eureka-server",
    "user-service",
    "student-service", 
    "instructor-service",
    "order-service",
    "api-gateway",
    "frontend"
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

# Validate services
foreach ($service in $Services) {
    if ($service -notin $AvailableServices) {
        Write-Error "Invalid service: $service"
        Write-Host "Available services: $($AvailableServices -join ', ')"
        exit 1
    }
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

# Process each service
foreach ($service in $Services) {
    Write-Step "Restarting service: $service"
    
    try {
        # Stop the service
        Write-Host "  Stopping $service..."
        docker-compose -f docker-compose.prod.yml stop $service
        
        # Remove the container
        Write-Host "  Removing container for $service..."
        docker-compose -f docker-compose.prod.yml rm -f $service
        
        # Start the service
        if ($Build) {
            Write-Host "  Building and starting $service..."
            if ($Verbose) {
                docker-compose -f docker-compose.prod.yml up -d --build $service
            } else {
                docker-compose -f docker-compose.prod.yml up -d --build $service | Out-Null
            }
        } else {
            Write-Host "  Starting $service..."
            if ($Verbose) {
                docker-compose -f docker-compose.prod.yml up -d $service
            } else {
                docker-compose -f docker-compose.prod.yml up -d $service | Out-Null
            }
        }
        
        Write-Success "Service $service restarted successfully"
        
    } catch {
        Write-Error "Failed to restart service $service: $_"
        continue
    }
}

# Wait for services to be healthy
Write-Step "Waiting for services to become healthy..."
$maxWaitTime = 120 # 2 minutes
$waitTime = 0
$checkInterval = 5

do {
    Start-Sleep $checkInterval
    $waitTime += $checkInterval
    
    $allHealthy = $true
    foreach ($service in $Services) {
        $status = docker-compose -f docker-compose.prod.yml ps $service --format "{{.Status}}"
        if ($status -match "unhealthy|starting") {
            $allHealthy = $false
            break
        }
    }
    
    if ($allHealthy) {
        Write-Success "All specified services are healthy!"
        break
    }
    
    if ($waitTime -ge $maxWaitTime) {
        Write-Warning "Timeout reached. Some services may still be starting up."
        break
    }
    
    Write-Host "⏳ Waiting for services to become healthy... ($waitTime/$maxWaitTime seconds)"
} while ($true)

# Show status of restarted services
Write-Step "Status of restarted services:"
foreach ($service in $Services) {
    $status = docker-compose -f docker-compose.prod.yml ps $service
    Write-Host $status
}

Write-Success "🚀 Service restart completed!"

# Show usage examples
Write-ColorOutput "`n📖 Usage Examples:" $Blue
Write-Host "   Restart single service:     .\restart-service.ps1 -Services user-service"
Write-Host "   Restart multiple services:  .\restart-service.ps1 -Services user-service,student-service"
Write-Host "   Restart with rebuild:       .\restart-service.ps1 -Services api-gateway -Build"
Write-Host "   Restart with verbose:       .\restart-service.ps1 -Services frontend -Verbose"
