# Simple Admin Test Script
param(
    [string]$Email = "cyberlearnixprivatelimited@gmail.com",
    [string]$BaseUrl = "http://localhost:8080"
)

Write-Host "Admin 403 Debug Script" -ForegroundColor Yellow
Write-Host "Email: $Email" -ForegroundColor Cyan
Write-Host "Base URL: $BaseUrl" -ForegroundColor Cyan
Write-Host ""

$headers = @{ "Content-Type" = "application/json" }

# Step 1: Send OTP
Write-Host "Step 1: Sending OTP..." -ForegroundColor Yellow
$otpBody = @{ email = $Email } | ConvertTo-Json

try {
    $otpResponse = Invoke-WebRequest -Uri "$BaseUrl/api/email-auth/send-otp" -Method POST -Body $otpBody -Headers $headers
    $otpData = $otpResponse.Content | ConvertFrom-Json
    
    Write-Host "OTP sent successfully" -ForegroundColor Green
    Write-Host "Session ID: $($otpData.otpSessionId)" -ForegroundColor Cyan
    
    # Get OTP from user
    $otp = Read-Host "Enter the OTP from your email"
    
    # Step 2: Verify OTP
    Write-Host ""
    Write-Host "Step 2: Verifying OTP..." -ForegroundColor Yellow
    $verifyBody = @{
        email = $Email
        otpSessionId = $otpData.otpSessionId
        otp = $otp
    } | ConvertTo-Json
    
    try {
        $verifyResponse = Invoke-WebRequest -Uri "$BaseUrl/api/email-auth/verify-otp" -Method POST -Body $verifyBody -Headers $headers
        $verifyData = $verifyResponse.Content | ConvertFrom-Json
        
        if ($verifyData.success -and $verifyData.accessToken) {
            Write-Host "OTP verified successfully" -ForegroundColor Green
            $accessToken = $verifyData.accessToken
            
            # Display user information
            Write-Host ""
            Write-Host "User Information:" -ForegroundColor Yellow
            Write-Host "Email: $($verifyData.user.email)" -ForegroundColor White
            Write-Host "Role: $($verifyData.user.role)" -ForegroundColor White
            Write-Host "Is Active: $($verifyData.user.isActive)" -ForegroundColor White
            
            # Check role
            if ($verifyData.user.role -ne "ADMIN") {
                Write-Host ""
                Write-Host "PROBLEM: User role is '$($verifyData.user.role)', not 'ADMIN'" -ForegroundColor Red
                Write-Host "This is why you are getting 403 Forbidden!" -ForegroundColor Red
                Write-Host ""
                Write-Host "SQL to fix this:" -ForegroundColor Cyan
                Write-Host "UPDATE user_entity SET role = 'ADMIN' WHERE email = '$Email';" -ForegroundColor Gray
                Write-Host ""
                Write-Host "Run this SQL in your PostgreSQL database and restart the application" -ForegroundColor Yellow
                return
            }
            
            Write-Host "User has ADMIN role - testing admin endpoint" -ForegroundColor Green
            
            # Step 3: Test Admin Endpoint
            Write-Host ""
            Write-Host "Step 3: Testing admin endpoint..." -ForegroundColor Yellow
            $adminHeaders = @{ 
                "Authorization" = "Bearer $accessToken"
                "Content-Type" = "application/json"
            }
            
            try {
                $adminResponse = Invoke-WebRequest -Uri "$BaseUrl/api/admin/all-users-details" -Method GET -Headers $adminHeaders
                Write-Host "SUCCESS! Admin endpoint worked!" -ForegroundColor Green
                Write-Host "Status Code: $($adminResponse.StatusCode)" -ForegroundColor Cyan
                
                $adminData = $adminResponse.Content | ConvertFrom-Json
                Write-Host "Found $($adminData.count) users in the system" -ForegroundColor Cyan
                
            } catch {
                $statusCode = [int]$_.Exception.Response.StatusCode
                Write-Host "Admin endpoint failed!" -ForegroundColor Red
                Write-Host "Status Code: $statusCode" -ForegroundColor Red
                
                if ($statusCode -eq 403) {
                    Write-Host ""
                    Write-Host "403 Forbidden means:" -ForegroundColor Yellow
                    Write-Host "1. Token is valid but user lacks ADMIN role" -ForegroundColor White
                    Write-Host "2. Check database: SELECT email, role FROM user_entity WHERE email = '$Email';" -ForegroundColor Gray
                    Write-Host "3. Update role: UPDATE user_entity SET role = 'ADMIN' WHERE email = '$Email';" -ForegroundColor Gray
                }
            }
            
        } else {
            Write-Host "OTP verification failed" -ForegroundColor Red
        }
        
    } catch {
        Write-Host "OTP verification error: $($_.Exception.Message)" -ForegroundColor Red
    }
    
} catch {
    Write-Host "Failed to send OTP: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Make sure the application is running on $BaseUrl" -ForegroundColor Yellow
}