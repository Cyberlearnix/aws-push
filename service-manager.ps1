#!/usr/bin/env pwsh
# CyberLMS Microservices - Service Manager
# Quick reference and utility script for managing all services

param(
    [Parameter(Position=0)]
    [ValidateSet("status", "logs", "health", "urls", "help")]
    [string]$Action = "help"
)

# Colors for output
$Red = "`e[31m"
$Green = "`e[32m"
$Yellow = "`e[33m"
$Blue = "`e[34m"
$Cyan = "`e[36m"
$Reset = "`e[0m"

function Write-ColorOutput {
    param([string]$Message, [string]$Color = $Reset)
    Write-Host "$Color$Message$Reset"
}

function Show-Help {
    Write-ColorOutput "🚀 CyberLMS Microservices Manager" $Cyan
    Write-ColorOutput "=================================" $Cyan
    Write-Host ""
    Write-ColorOutput "📋 Available Scripts:" $Blue
    Write-Host "   .\pull-and-restart-all.ps1    - Pull latest code and restart all services"
    Write-Host "   .\pull-and-restart-all.sh     - Pull latest code and restart all services (bash)"
    Write-Host "   .\restart-service.ps1         - Restart specific service(s)"
    Write-Host "   .\restart-service.sh          - Restart specific service(s) (bash)"
    Write-Host "   .\service-manager.ps1         - This utility script"
    Write-Host ""
    Write-ColorOutput "🔧 Quick Actions:" $Blue
    Write-Host "   .\service-manager.ps1 status  - Show all service status"
    Write-Host "   .\service-manager.ps1 logs    - Show logs for all services"
    Write-Host "   .\service-manager.ps1 health  - Show health status"
    Write-Host "   .\service-manager.ps1 urls    - Show all service URLs"
    Write-Host ""
    Write-ColorOutput "📖 Usage Examples:" $Blue
    Write-Host "   # Full restart with latest code"
    Write-Host "   .\pull-and-restart-all.ps1"
    Write-Host ""
    Write-Host "   # Restart specific service"
    Write-Host "   .\restart-service.ps1 -Services user-service"
    Write-Host ""
    Write-Host "   # Restart multiple services with rebuild"
    Write-Host "   .\restart-service.ps1 -Services user-service,api-gateway -Build"
    Write-Host ""
    Write-Host "   # Skip git pull (useful for testing local changes)"
    Write-Host "   .\pull-and-restart-all.ps1 -SkipGitPull"
    Write-Host ""
    Write-ColorOutput "🌐 Service Architecture:" $Blue
    Write-Host "   Frontend (React)     → API Gateway → Microservices"
    Write-Host "   Port 80              → Port 8080   → Ports 8081-8084"
    Write-Host ""
    Write-Host "   Infrastructure:"
    Write-Host "   - Config Server (8888) - Centralized configuration"
    Write-Host "   - Eureka Server (8761) - Service discovery"
    Write-Host "   - PostgreSQL (5432)    - Database"
    Write-Host "   - Redis (6379)         - Cache"
}

function Show-Status {
    Write-ColorOutput "📊 Service Status:" $Blue
    docker-compose -f docker-compose.prod.yml ps
}

function Show-Logs {
    Write-ColorOutput "📝 Following logs for all services (Ctrl+C to stop):" $Blue
    docker-compose -f docker-compose.prod.yml logs -f
}

function Show-Health {
    Write-ColorOutput "🏥 Health Check Status:" $Blue
    $services = @("config-server", "eureka-server", "user-service", "student-service", "instructor-service", "order-service", "api-gateway", "frontend")
    
    foreach ($service in $services) {
        $status = docker-compose -f docker-compose.prod.yml ps $service --format "{{.Status}}"
        if ($status -match "healthy") {
            Write-ColorOutput "✅ $service - Healthy" $Green
        } elseif ($status -match "unhealthy") {
            Write-ColorOutput "❌ $service - Unhealthy" $Red
        } elseif ($status -match "starting") {
            Write-ColorOutput "⏳ $service - Starting" $Yellow
        } else {
            Write-ColorOutput "⚠️  $service - $status" $Yellow
        }
    }
}

function Show-URLs {
    Write-ColorOutput "🌐 Service URLs:" $Blue
    Write-Host ""
    Write-ColorOutput "🖥️  User Interfaces:" $Cyan
    Write-Host "   Frontend Application:  http://localhost"
    Write-Host "   Eureka Dashboard:      http://localhost:8761"
    Write-Host ""
    Write-ColorOutput "🔌 API Endpoints:" $Cyan
    Write-Host "   API Gateway:           http://localhost:8080"
    Write-Host "   User Service:          http://localhost:8081"
    Write-Host "   Student Service:       http://localhost:8082"
    Write-Host "   Instructor Service:    http://localhost:8083"
    Write-Host "   Order Service:         http://localhost:8084"
    Write-Host "   Config Server:         http://localhost:8888"
    Write-Host ""
    Write-ColorOutput "🗄️  Infrastructure:" $Cyan
    Write-Host "   PostgreSQL:            localhost:5432"
    Write-Host "   Redis:                 localhost:6379"
    Write-Host ""
    Write-ColorOutput "🔍 Health Endpoints:" $Cyan
    Write-Host "   All services have:     /actuator/health"
    Write-Host "   Example:               http://localhost:8081/actuator/health"
}

# Execute the requested action
switch ($Action) {
    "status" { Show-Status }
    "logs" { Show-Logs }
    "health" { Show-Health }
    "urls" { Show-URLs }
    "help" { Show-Help }
    default { Show-Help }
}
