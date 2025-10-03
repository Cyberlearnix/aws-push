# Comprehensive Logging Test Script
param(
    [string]$Email = "cyberlearnixprivatelimited@gmail.com",
    [string]$BaseUrl = "http://localhost:8080"
)

Write-Host "🔍 Comprehensive Logging Test - User Service" -ForegroundColor Yellow
Write-Host "Email: $Email" -ForegroundColor Cyan
Write-Host "Base URL: $BaseUrl" -ForegroundColor Cyan
Write-Host ""
Write-Host "This script will test all major flows and generate comprehensive logs" -ForegroundColor White
Write-Host "Check your application console for detailed logging output" -ForegroundColor White
Write-Host ""

$headers = @{ "Content-Type" = "application/json" }
$testResults = @()

# Function to add test result
function Add-TestResult {
    param($TestName, $Success, $Details)
    $testResults += [PSCustomObject]@{
        Test = $TestName
        Success = $Success
        Details = $Details
    }
}

# Test 1: Health Check (should generate basic logs)
Write-Host "Test 1: Health Check" -ForegroundColor Cyan
try {
    $healthResponse = Invoke-WebRequest -Uri "$BaseUrl/actuator/health" -Method GET
    if ($healthResponse.StatusCode -eq 200) {
        Write-Host "✅ Health check passed" -ForegroundColor Green
        Add-TestResult "Health Check" $true "Status: 200"
    }
} catch {
    Write-Host "❌ Health check failed: $($_.Exception.Message)" -ForegroundColor Red
    Add-TestResult "Health Check" $false $_.Exception.Message
}

# Test 2: Send OTP (should generate EmailAuthService logs)
Write-Host ""
Write-Host "Test 2: Send OTP - EmailAuthService Logging" -ForegroundColor Cyan
$otpBody = @{ email = $Email } | ConvertTo-Json

try {
    $otpResponse = Invoke-WebRequest -Uri "$BaseUrl/api/email-auth/send-otp" -Method POST -Body $otpBody -Headers $headers
    if ($otpResponse.StatusCode -eq 200) {
        $otpData = $otpResponse.Content | ConvertFrom-Json
        Write-Host "✅ OTP sent successfully" -ForegroundColor Green
        Write-Host "   Session ID: $($otpData.otpSessionId)" -ForegroundColor Gray
        $sessionId = $otpData.otpSessionId
        Add-TestResult "Send OTP" $true "Session created: $sessionId"
        
        # Get OTP from user
        $otp = Read-Host "Enter the OTP from your email"
        
        # Test 3: Verify OTP (should generate comprehensive verification logs)
        Write-Host ""
        Write-Host "Test 3: Verify OTP - Authentication Flow Logging" -ForegroundColor Cyan
        $verifyBody = @{
            email = $Email
            otpSessionId = $sessionId
            otp = $otp
        } | ConvertTo-Json
        
        try {
            $verifyResponse = Invoke-WebRequest -Uri "$BaseUrl/api/email-auth/verify-otp" -Method POST -Body $verifyBody -Headers $headers
            if ($verifyResponse.StatusCode -eq 200) {
                $verifyData = $verifyResponse.Content | ConvertFrom-Json
                if ($verifyData.success -and $verifyData.accessToken) {
                    Write-Host "✅ OTP verification successful" -ForegroundColor Green
                    Write-Host "   User Role: $($verifyData.user.role)" -ForegroundColor Gray
                    Write-Host "   Token Length: $($verifyData.accessToken.Length)" -ForegroundColor Gray
                    $accessToken = $verifyData.accessToken
                    Add-TestResult "Verify OTP" $true "Role: $($verifyData.user.role)"
                    
                    # Test 4: Admin Endpoint (should generate JWT filter + Admin controller logs)
                    Write-Host ""
                    Write-Host "Test 4: Admin Endpoint - JWT Filter & Authorization Logging" -ForegroundColor Cyan
                    $adminHeaders = @{ 
                        "Authorization" = "Bearer $accessToken"
                        "Content-Type" = "application/json"
                        "User-Agent" = "PowerShell-LoggingTest/1.0"
                    }
                    
                    try {
                        Write-Host "   Making admin request (check logs for JWT filter processing)..." -ForegroundColor Gray
                        $adminResponse = Invoke-WebRequest -Uri "$BaseUrl/api/admin/all-users-details" -Method GET -Headers $adminHeaders
                        if ($adminResponse.StatusCode -eq 200) {
                            $adminData = $adminResponse.Content | ConvertFrom-Json
                            Write-Host "✅ Admin endpoint successful" -ForegroundColor Green
                            Write-Host "   Users found: $($adminData.count)" -ForegroundColor Gray
                            Add-TestResult "Admin Endpoint" $true "Users: $($adminData.count)"
                        }
                    } catch {
                        $statusCode = if ($_.Exception.Response) { [int]$_.Exception.Response.StatusCode } else { "Unknown" }
                        Write-Host "❌ Admin endpoint failed: Status $statusCode" -ForegroundColor Red
                        Add-TestResult "Admin Endpoint" $false "Status: $statusCode"
                    }
                    
                    # Test 5: Regular User Endpoint (should generate user service logs)
                    Write-Host ""
                    Write-Host "Test 5: User Profile Endpoint - UserService Logging" -ForegroundColor Cyan
                    try {
                        $userResponse = Invoke-WebRequest -Uri "$BaseUrl/api/users/$($verifyData.user.id)" -Method GET
                        if ($userResponse.StatusCode -eq 200) {
                            Write-Host "✅ User profile endpoint successful" -ForegroundColor Green
                            Add-TestResult "User Profile" $true "Profile retrieved"
                        }
                    } catch {
                        $statusCode = if ($_.Exception.Response) { [int]$_.Exception.Response.StatusCode } else { "Unknown" }
                        Write-Host "❌ User profile failed: Status $statusCode" -ForegroundColor Red
                        Add-TestResult "User Profile" $false "Status: $statusCode"
                    }
                    
                } else {
                    Write-Host "❌ OTP verification failed or no token received" -ForegroundColor Red
                    Add-TestResult "Verify OTP" $false "No access token"
                }
            }
        } catch {
            Write-Host "❌ OTP verification failed: $($_.Exception.Message)" -ForegroundColor Red
            Add-TestResult "Verify OTP" $false $_.Exception.Message
        }
    }
} catch {
    Write-Host "❌ Send OTP failed: $($_.Exception.Message)" -ForegroundColor Red
    Add-TestResult "Send OTP" $false $_.Exception.Message
}

# Test 6: Test invalid endpoint (should generate security/error logs)
Write-Host ""
Write-Host "Test 6: Invalid Endpoint - Error Logging" -ForegroundColor Cyan
try {
    $invalidResponse = Invoke-WebRequest -Uri "$BaseUrl/api/invalid/endpoint" -Method GET
} catch {
    $statusCode = if ($_.Exception.Response) { [int]$_.Exception.Response.StatusCode } else { "Unknown" }
    if ($statusCode -eq 404) {
        Write-Host "✅ Invalid endpoint correctly returned 404" -ForegroundColor Green
        Add-TestResult "Invalid Endpoint" $true "404 as expected"
    } else {
        Write-Host "⚠️ Invalid endpoint returned unexpected status: $statusCode" -ForegroundColor Yellow
        Add-TestResult "Invalid Endpoint" $false "Status: $statusCode"
    }
}

# Test 7: Unauthorized admin request (should generate JWT + security logs)
Write-Host ""
Write-Host "Test 7: Unauthorized Admin Request - Security Logging" -ForegroundColor Cyan
try {
    $unauthorizedResponse = Invoke-WebRequest -Uri "$BaseUrl/api/admin/all-users-details" -Method GET
} catch {
    $statusCode = if ($_.Exception.Response) { [int]$_.Exception.Response.StatusCode } else { "Unknown" }
    if ($statusCode -eq 401 -or $statusCode -eq 403) {
        Write-Host "✅ Unauthorized request correctly blocked" -ForegroundColor Green
        Add-TestResult "Unauthorized Admin" $true "Status: $statusCode (expected)"
    } else {
        Write-Host "⚠️ Unexpected status for unauthorized request: $statusCode" -ForegroundColor Yellow
        Add-TestResult "Unauthorized Admin" $false "Status: $statusCode"
    }
}

# Display summary
Write-Host ""
Write-Host "🔍 Logging Test Summary:" -ForegroundColor Yellow
Write-Host "========================" -ForegroundColor Yellow
$successCount = ($testResults | Where-Object { $_.Success -eq $true }).Count
$totalCount = $testResults.Count

foreach ($result in $testResults) {
    $status = if ($result.Success) { "✅ PASS" } else { "❌ FAIL" }
    $color = if ($result.Success) { "Green" } else { "Red" }
    Write-Host "$status $($result.Test) - $($result.Details)" -ForegroundColor $color
}

Write-Host ""
Write-Host "Results: $successCount/$totalCount tests passed" -ForegroundColor $(if ($successCount -eq $totalCount) { "Green" } else { "Yellow" })

Write-Host ""
Write-Host "📋 What to Look for in Application Logs:" -ForegroundColor Yellow
Write-Host "=========================================" -ForegroundColor Yellow
Write-Host "1. EmailAuthService logs with [requestId] prefixes" -ForegroundColor White
Write-Host "2. JWT Filter logs showing token processing steps" -ForegroundColor White
Write-Host "3. UserService logs with database operations" -ForegroundColor White
Write-Host "4. OtpCacheService logs with Redis operations" -ForegroundColor White
Write-Host "5. AdminController logs with authentication context" -ForegroundColor White
Write-Host "6. Spring Security logs for authorization decisions" -ForegroundColor White
Write-Host "7. Hibernate SQL logs for database queries" -ForegroundColor White
Write-Host "8. Request/response flow logs" -ForegroundColor White

Write-Host ""
Write-Host "📁 Log File Location: logs/user-service.log" -ForegroundColor Cyan
Write-Host "💡 For real-time log monitoring, use: Get-Content logs/user-service.log -Wait" -ForegroundColor Cyan