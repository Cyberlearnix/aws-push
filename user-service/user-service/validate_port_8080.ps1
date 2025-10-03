# Port 8080 Validation Script
Write-Host "Validating User Service on Port 8080" -ForegroundColor Yellow
Write-Host ""

$BASE_URL = "http://localhost:8080"

# Test 1: Health Check
Write-Host "1. Testing Health Check..." -ForegroundColor Cyan
try {
    $healthResponse = Invoke-WebRequest -Uri "$BASE_URL/actuator/health" -Method GET
    if ($healthResponse.StatusCode -eq 200) {
        Write-Host "✅ Health check successful on port 8080" -ForegroundColor Green
        $healthData = $healthResponse.Content | ConvertFrom-Json
        Write-Host "   Status: $($healthData.status)" -ForegroundColor White
    }
} catch {
    Write-Host "❌ Health check failed on port 8080" -ForegroundColor Red
    Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 2: Send OTP (Public endpoint)
Write-Host ""
Write-Host "2. Testing OTP endpoint..." -ForegroundColor Cyan
$headers = @{ "Content-Type" = "application/json" }
$otpBody = @{ email = "test@example.com" } | ConvertTo-Json

try {
    $otpResponse = Invoke-WebRequest -Uri "$BASE_URL/api/email-auth/send-otp" -Method POST -Body $otpBody -Headers $headers
    if ($otpResponse.StatusCode -eq 200) {
        Write-Host "✅ OTP endpoint working on port 8080" -ForegroundColor Green
        $otpData = $otpResponse.Content | ConvertFrom-Json
        Write-Host "   OTP Session created: $($otpData.otpSessionId -ne $null)" -ForegroundColor White
    }
} catch {
    $statusCode = if ($_.Exception.Response) { $_.Exception.Response.StatusCode } else { "Unknown" }
    Write-Host "❌ OTP endpoint failed on port 8080" -ForegroundColor Red
    Write-Host "   Status: $statusCode" -ForegroundColor Red
}

# Test 3: Admin endpoint (should require auth)
Write-Host ""
Write-Host "3. Testing Admin endpoint (should return 401/403)..." -ForegroundColor Cyan
try {
    $adminResponse = Invoke-WebRequest -Uri "$BASE_URL/api/admin/all-users-details" -Method GET
    Write-Host "⚠️ Admin endpoint accessible without auth - this might be an issue" -ForegroundColor Yellow
} catch {
    $statusCode = if ($_.Exception.Response) { [int]$_.Exception.Response.StatusCode } else { 0 }
    if ($statusCode -eq 401 -or $statusCode -eq 403) {
        Write-Host "✅ Admin endpoint properly secured on port 8080" -ForegroundColor Green
        Write-Host "   Status: $statusCode (expected for unauthenticated request)" -ForegroundColor White
    } else {
        Write-Host "❌ Unexpected status code: $statusCode" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "Port Configuration Summary:" -ForegroundColor Yellow
Write-Host "- User Service: http://localhost:8080" -ForegroundColor White
Write-Host "- Health Check: $BASE_URL/actuator/health" -ForegroundColor White
Write-Host "- API Base: $BASE_URL/api/" -ForegroundColor White
Write-Host ""
Write-Host "Next Steps:" -ForegroundColor Yellow
Write-Host "1. Restart your application to pick up the new port configuration" -ForegroundColor White
Write-Host "2. Update any API Gateway or load balancer configurations" -ForegroundColor White
Write-Host "3. Update Eureka configuration if needed" -ForegroundColor White
Write-Host "4. Test your admin authentication with: ./test_admin_simple.ps1" -ForegroundColor White