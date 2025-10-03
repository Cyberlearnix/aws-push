# Simple Admin Test Script
param([string]$Otp, [string]$SessionId)

$BASE_URL = "http://localhost:8081"
$ADMIN_EMAIL = "cyberlearnixprivatelimited@gmail.com"

if (-not $Otp) {
    # Step 1: Send OTP
    Write-Host "Sending OTP to $ADMIN_EMAIL..." -ForegroundColor Yellow
    
    $body = "{`"email`": `"$ADMIN_EMAIL`"}"
    
    try {
        $response = Invoke-WebRequest -Uri "$BASE_URL/api/email-auth/send-otp" -Method POST -Body $body -ContentType "application/json"
        
        if ($response.StatusCode -eq 200) {
            $data = $response.Content | ConvertFrom-Json
            Write-Host "OTP sent! Session ID: $($data.otpSessionId)" -ForegroundColor Green
            Write-Host "Check your email and run:" -ForegroundColor Yellow
            Write-Host "  .\admin_test.ps1 -Otp YOUR_OTP -SessionId '$($data.otpSessionId)'" -ForegroundColor White
        }
    }
    catch {
        Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    }
}
else {
    # Step 2: Verify OTP and test admin endpoints
    Write-Host "Verifying OTP..." -ForegroundColor Yellow
    
    $body = "{`"email`": `"$ADMIN_EMAIL`", `"otpSessionId`": `"$SessionId`", `"otp`": `"$Otp`"}"
    
    try {
        $response = Invoke-WebRequest -Uri "$BASE_URL/api/email-auth/verify-otp" -Method POST -Body $body -ContentType "application/json"
        
        if ($response.StatusCode -eq 200) {
            $data = $response.Content | ConvertFrom-Json
            
            if ($data.success -and $data.accessToken) {
                Write-Host "Login successful!" -ForegroundColor Green
                $token = $data.accessToken
                
                # Test admin endpoint
                Write-Host "Testing admin endpoint..." -ForegroundColor Yellow
                $headers = @{ Authorization = "Bearer $token" }
                
                try {
                    $adminResponse = Invoke-WebRequest -Uri "$BASE_URL/api/admin/all-users-details" -Method GET -Headers $headers
                    Write-Host "Admin endpoint success! Status: $($adminResponse.StatusCode)" -ForegroundColor Green
                    
                    $adminData = $adminResponse.Content | ConvertFrom-Json
                    Write-Host "Found $($adminData.count) users" -ForegroundColor Cyan
                }
                catch {
                    $statusCode = if ($_.Exception.Response) { $_.Exception.Response.StatusCode } else { "Unknown" }
                    Write-Host "Admin endpoint failed! Status: $statusCode" -ForegroundColor Red
                    
                    if ($statusCode -eq "Forbidden") {
                        Write-Host "403 Forbidden - Role/permission issue!" -ForegroundColor Red
                        Write-Host "This usually means:" -ForegroundColor Yellow
                        Write-Host "  - User doesn't have ADMIN role" -ForegroundColor Yellow
                        Write-Host "  - JWT token missing role claim" -ForegroundColor Yellow
                        Write-Host "  - Spring Security config issue" -ForegroundColor Yellow
                    }
                }
            }
            else {
                Write-Host "Login failed or user doesn't exist" -ForegroundColor Red
                Write-Host "Response: $($response.Content)" -ForegroundColor Yellow
            }
        }
    }
    catch {
        Write-Host "OTP verification error: $($_.Exception.Message)" -ForegroundColor Red
    }
}