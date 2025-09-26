# PowerShell script to stop all CyberLearnix LMS microservices
param(
    [switch]$Force,
    [switch]$Help
)

if ($Help) {
    Write-Host "CyberLearnix LMS Microservices Stop Script" -ForegroundColor Green
    Write-Host ""
    Write-Host "Usage:" -ForegroundColor Yellow
    Write-Host "  .\stop-all-services.ps1        # Gracefully stop services"
    Write-Host "  .\stop-all-services.ps1 -Force   # Force kill all services"
    Write-Host "  .\stop-all-services.ps1 -Help    # Show this help"
    Write-Host ""
    Write-Host "This script will stop Java processes using these ports:" -ForegroundColor Cyan
    Write-Host "  8761 - eureka-server (Service Discovery)"
    Write-Host "  8888 - config-server (Configuration)"
    Write-Host "  8080 - api-gateway (API Gateway)"
    Write-Host "  8081 - user-service (User Management)"
    Write-Host "  8082 - student_service (Student Management)"
    Write-Host "  8083 - course-service (Course Management)"
    Write-Host "  8084 - instructor-service (Instructor Management)"
    Write-Host "  8085 - order-service (Order Processing)"
    Write-Host "  8086 - payment-service (Payment Processing)"
    exit 0
}

Write-Host "🛑 Stopping CyberLearnix LMS Microservices..." -ForegroundColor Red
Write-Host ""

# Define the ports used by our microservices
$servicePorts = @(8761, 8888, 8080, 8081, 8082, 8083, 8084, 8085, 8086)
$serviceNames = @{
    8761 = "eureka-server"
    8888 = "config-server"
    8080 = "api-gateway"
    8081 = "user-service"
    8082 = "student_service"
    8083 = "course-service"
    8084 = "instructor-service"
    8085 = "order-service"
    8086 = "payment-service"
}

$stoppedCount = 0

foreach ($port in $servicePorts) {
    Write-Host "🔍 Checking port $port ($($serviceNames[$port]))..." -ForegroundColor Cyan
    
    # Get processes using this port
    $connections = netstat -ano | Select-String ":$port\s"
    
    if ($connections) {
        foreach ($connection in $connections) {
            # Extract PID from netstat output
            $fields = $connection -split '\s+' | Where-Object { $_ -ne '' }
            if ($fields.Count -ge 5) {
                $pid = $fields[4]
                
                # Get process details
                $process = Get-Process -Id $pid -ErrorAction SilentlyContinue
                
                if ($process -and $process.ProcessName -eq "java") {
                    Write-Host "   📍 Found Java process: PID $pid using port $port" -ForegroundColor Yellow
                    
                    if ($Force) {
                        Write-Host "   💀 Force killing process $pid..." -ForegroundColor Red
                        Stop-Process -Id $pid -Force -ErrorAction SilentlyContinue
                    } else {
                        Write-Host "   🛑 Gracefully stopping process $pid..." -ForegroundColor Yellow
                        Stop-Process -Id $pid -ErrorAction SilentlyContinue
                    }
                    
                    Start-Sleep -Seconds 2
                    
                    # Check if process is still running
                    $stillRunning = Get-Process -Id $pid -ErrorAction SilentlyContinue
                    if (-not $stillRunning) {
                        Write-Host "   ✅ Successfully stopped $($serviceNames[$port])" -ForegroundColor Green
                        $stoppedCount++
                    } else {
                        Write-Host "   ⚠️  Process $pid still running, you may need to use -Force" -ForegroundColor Yellow
                    }
                }
            }
        }
    } else {
        Write-Host "   ✅ Port $port is free" -ForegroundColor Green
    }
}

Write-Host ""
if ($stoppedCount -gt 0) {
    Write-Host "✅ Stopped $stoppedCount microservice(s)" -ForegroundColor Green
} else {
    Write-Host "ℹ️  No running microservices found" -ForegroundColor Blue
}

Write-Host ""
Write-Host "🔍 Final port status:" -ForegroundColor Cyan
foreach ($port in $servicePorts) {
    $inUse = netstat -ano | Select-String ":$port\s" -Quiet
    if ($inUse) {
        Write-Host "   ❌ Port $port ($($serviceNames[$port])) - Still in use" -ForegroundColor Red
    } else {
        Write-Host "   ✅ Port $port ($($serviceNames[$port])) - Free" -ForegroundColor Green
    }
}

Write-Host ""
Write-Host "💡 You can now start your services safely!" -ForegroundColor Green