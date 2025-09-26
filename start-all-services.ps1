# PowerShell script to start all CyberLearnix LMS microservices
param(
    [switch]$Sequential,
    [switch]$Help
)

if ($Help) {
    Write-Host "CyberLearnix LMS Microservices Startup Script" -ForegroundColor Green
    Write-Host ""
    Write-Host "Usage:" -ForegroundColor Yellow
    Write-Host "  .\start-all-services.ps1          # Start all services in parallel"
    Write-Host "  .\start-all-services.ps1 -Sequential  # Start services one by one"
    Write-Host "  .\start-all-services.ps1 -Help       # Show this help"
    Write-Host ""
    Write-Host "Services will be started in the recommended order:" -ForegroundColor Cyan
    Write-Host "1. eureka-server (8761) - Service Discovery"
    Write-Host "2. config-server (8888) - Configuration"
    Write-Host "3. user-service (8081) - User Management"
    Write-Host "4. course-service (8083) - Course Management"
    Write-Host "5. instructor-service (8084) - Instructor Management"
    Write-Host "6. student_service (8082) - Student Management"
    Write-Host "7. order-service (8085) - Order Processing"
    Write-Host "8. payment-service (8086) - Payment Processing"
    Write-Host "9. api-gateway (8080) - API Gateway"
    Write-Host ""
    Write-Host "Prerequisites:" -ForegroundColor Yellow
    Write-Host "- PostgreSQL running on localhost:5432"
    Write-Host "- Database 'cyberlearnixdb' exists"
    Write-Host "- User 'cyberlearnix' with password 'cyberlearnix123'"
    exit 0
}

Write-Host "🚀 Starting CyberLearnix LMS Microservices..." -ForegroundColor Green
Write-Host ""

$services = @(
    @{Name="eureka-server"; Port=8761; Description="Service Discovery"}
    @{Name="config-server"; Port=8888; Description="Configuration Management"}
    @{Name="user-service"; Port=8081; Description="User Management"}
    @{Name="course-service"; Port=8083; Description="Course Management"}
    @{Name="instructor-service"; Port=8084; Description="Instructor Management"}
    @{Name="student_service"; Port=8082; Description="Student Management"}
    @{Name="order-service"; Port=8085; Description="Order Processing"}
    @{Name="payment-service"; Port=8086; Description="Payment Processing"}
    @{Name="api-gateway"; Port=8080; Description="API Gateway"}
)

if ($Sequential) {
    Write-Host "Starting services sequentially..." -ForegroundColor Yellow
    Write-Host ""
    
    foreach ($service in $services) {
        Write-Host "🔄 Starting $($service.Name) on port $($service.Port) - $($service.Description)" -ForegroundColor Cyan
        
        # Start the service in a new PowerShell window
        Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PWD'; .\gradlew :$($service.Name):bootRun"
        
        if ($service.Name -eq "eureka-server") {
            Write-Host "   ⏳ Waiting 30 seconds for Eureka Server to start..." -ForegroundColor Yellow
            Start-Sleep -Seconds 30
        } elseif ($service.Name -eq "config-server") {
            Write-Host "   ⏳ Waiting 15 seconds for Config Server to start..." -ForegroundColor Yellow
            Start-Sleep -Seconds 15
        } else {
            Write-Host "   ⏳ Waiting 10 seconds before starting next service..." -ForegroundColor Yellow
            Start-Sleep -Seconds 10
        }
    }
} else {
    Write-Host "Starting services in parallel..." -ForegroundColor Yellow
    Write-Host ""
    
    # Start Eureka Server first
    Write-Host "🔄 Starting eureka-server (Service Discovery) on port 8761" -ForegroundColor Cyan
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PWD'; .\gradlew :eureka-server:bootRun"
    
    Write-Host "   ⏳ Waiting 30 seconds for Eureka Server..." -ForegroundColor Yellow
    Start-Sleep -Seconds 30
    
    # Start Config Server
    Write-Host "🔄 Starting config-server (Configuration) on port 8888" -ForegroundColor Cyan
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PWD'; .\gradlew :config-server:bootRun"
    
    Write-Host "   ⏳ Waiting 15 seconds for Config Server..." -ForegroundColor Yellow
    Start-Sleep -Seconds 15
    
    # Start all business services in parallel
    $businessServices = $services | Where-Object { $_.Name -notin @("eureka-server", "config-server", "api-gateway") }
    
    foreach ($service in $businessServices) {
        Write-Host "🔄 Starting $($service.Name) - $($service.Description) on port $($service.Port)" -ForegroundColor Cyan
        Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PWD'; .\gradlew :$($service.Name):bootRun"
        Start-Sleep -Seconds 2  # Small delay between starts
    }
    
    Write-Host "   ⏳ Waiting 45 seconds for business services to start..." -ForegroundColor Yellow
    Start-Sleep -Seconds 45
    
    # Start API Gateway last
    Write-Host "🔄 Starting api-gateway (API Gateway) on port 8080" -ForegroundColor Cyan
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PWD'; .\gradlew :api-gateway:bootRun"
}

Write-Host ""
Write-Host "✅ All services are starting up!" -ForegroundColor Green
Write-Host ""
Write-Host "📍 Service URLs:" -ForegroundColor Yellow
Write-Host "   Eureka Dashboard: http://localhost:8761"
Write-Host "   API Gateway: http://localhost:8080"
Write-Host "   Health Checks: http://localhost:<port>/actuator/health"
Write-Host ""
Write-Host "🔍 To check service status:" -ForegroundColor Cyan
Write-Host "   Visit http://localhost:8761 to see registered services"
Write-Host ""
Write-Host "⚠️  Note: Services may take 1-2 minutes to fully start and register with Eureka" -ForegroundColor Yellow