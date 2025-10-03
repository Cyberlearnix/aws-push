# Debug 403 Forbidden Error Script
param([string]$Email = "cyberlearnix@gmail.com")

$BASE_URL = "http://localhost:8081"
$HEADERS = @{ "Content-Type" = "application/json" }

Write-Host "🔍 Debugging 403 Forbidden Error for Admin Access" -ForegroundColor Yellow
Write-Host "Email: $Email" -ForegroundColor Cyan
Write-Host "Base URL: $BASE_URL" -ForegroundColor Cyan
Write-Host ""

# Step 1: Send OTP
Write-Host "Step 1: Sending OTP..." -ForegroundColor Yellow
$otpBody = @{ email = $Email } | ConvertTo-Json

try {
    $otpResponse = Invoke-WebRequest -Uri "$BASE_URL/api/email-auth/send-otp" -Method POST -Body $otpBody -Headers $HEADERS
    $otpData = $otpResponse.Content | ConvertFrom-Json
    
    if ($otpResponse.StatusCode -eq 200) {
        Write-Host "✅ OTP sent successfully" -ForegroundColor Green
        Write-Host "Session ID: $($otpData.otpSessionId)" -ForegroundColor Cyan
        
        # Prompt for OTP
        $otp = Read-Host "Enter the OTP from your email"
        
        # Step 2: Verify OTP
        Write-Host "`nStep 2: Verifying OTP..." -ForegroundColor Yellow
        $verifyBody = @{
            email = $Email
            otpSessionId = $otpData.otpSessionId
            otp = $otp
        } | ConvertTo-Json
        
        try {
            $verifyResponse = Invoke-WebRequest -Uri "$BASE_URL/api/email-auth/verify-otp" -Method POST -Body $verifyBody -Headers $HEADERS
            $verifyData = $verifyResponse.Content | ConvertFrom-Json
            
            if ($verifyResponse.StatusCode -eq 200 -and $verifyData.success) {
                Write-Host "✅ OTP verified successfully" -ForegroundColor Green
                Write-Host "User exists: $($verifyData.userExists)" -ForegroundColor Cyan
                
                if ($verifyData.accessToken) {
                    $accessToken = $verifyData.accessToken
                    Write-Host "Access token received (first 50 chars): $($accessToken.Substring(0, [Math]::Min(50, $accessToken.Length)))..." -ForegroundColor Cyan
                    
                    # Debug: Display user info
                    if ($verifyData.user) {
                        Write-Host "`n👤 User Information:" -ForegroundColor Yellow
                        Write-Host "ID: $($verifyData.user.id)" -ForegroundColor White
                        Write-Host "Full Name: $($verifyData.user.fullName)" -ForegroundColor White
                        Write-Host "Email: $($verifyData.user.email)" -ForegroundColor White
                        Write-Host "Role: $($verifyData.user.role)" -ForegroundColor White
                        Write-Host "Is Active: $($verifyData.user.isActive)" -ForegroundColor White
                        
                        # Check if role is ADMIN
                        if ($verifyData.user.role -ne "ADMIN") {
                            Write-Host "⚠️ WARNING: User role is '$($verifyData.user.role)', not 'ADMIN'" -ForegroundColor Red
                            Write-Host "This explains the 403 error - user doesn't have admin privileges" -ForegroundColor Red
                        } else {
                            Write-Host "✅ User has ADMIN role" -ForegroundColor Green
                        }
                    }
                    
                    # Step 3: Test Admin Endpoint
                    Write-Host "`nStep 3: Testing admin endpoint with token..." -ForegroundColor Yellow
                    $adminHeaders = @{ 
                        "Authorization" = "Bearer $accessToken"
                        "Content-Type" = "application/json" 
                    }
                    
                    try {
                        $adminResponse = Invoke-WebRequest -Uri "$BASE_URL/api/admin/all-users-details" -Method GET -Headers $adminHeaders
                        Write-Host "✅ Admin endpoint success! Status: $($adminResponse.StatusCode)" -ForegroundColor Green
                        
                        $adminData = $adminResponse.Content | ConvertFrom-Json
                        Write-Host "Found $($adminData.count) users in system" -ForegroundColor Cyan
                    }
                    catch {
                        $statusCode = if ($_.Exception.Response) { $_.Exception.Response.StatusCode } else { "Unknown" }
                        $errorBody = ""
                        
                        if ($_.Exception.Response) {
                            try {
                                $stream = $_.Exception.Response.GetResponseStream()
                                $reader = New-Object System.IO.StreamReader($stream)
                                $errorBody = $reader.ReadToEnd()
                                $reader.Close()
                                $stream.Close()
                            } catch { }
                        }
                        
                        Write-Host "❌ Admin endpoint failed! Status: $statusCode" -ForegroundColor Red
                        if ($errorBody) {
                            Write-Host "Error response: $errorBody" -ForegroundColor Red
                        }
                        
                        # Specific 403 diagnosis
                        if ($statusCode -eq "Forbidden") {
                            Write-Host "`n🔍 403 Forbidden Diagnosis:" -ForegroundColor Yellow
                            Write-Host "1. Token is valid (authenticated)" -ForegroundColor White
                            Write-Host "2. User lacks ADMIN role (authorization failed)" -ForegroundColor White
                            Write-Host "3. Check if cyberlearnix@gmail.com has role=ADMIN in database" -ForegroundColor White
                            Write-Host "4. Verify JWT token contains correct role claim" -ForegroundColor White
                        }
                    }
                }
            } else {
                Write-Host "❌ OTP verification failed" -ForegroundColor Red
                Write-Host "Response: $($verifyResponse.Content)" -ForegroundColor Yellow
            }
        }
        catch {
            Write-Host "❌ OTP verification error: $($_.Exception.Message)" -ForegroundColor Red
        }
    } else {
        Write-Host "❌ Failed to send OTP. Status: $($otpResponse.StatusCode)" -ForegroundColor Red
    }
}
catch {
    Write-Host "❌ Failed to send OTP: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Make sure the user service is running on port 8081" -ForegroundColor Yellow
}

Write-Host "`n🔧 Potential Solutions:" -ForegroundColor Yellow
Write-Host "1. Update user role to ADMIN in database:" -ForegroundColor White
Write-Host "   UPDATE user_entity SET role = 'ADMIN' WHERE email = 'cyberlearnix@gmail.com';" -ForegroundColor Gray
Write-Host ""
Write-Host "2. Create admin user if doesn't exist:" -ForegroundColor White
Write-Host "   Use /api/admin/add-users endpoint or direct DB insert" -ForegroundColor Gray
Write-Host ""
Write-Host "3. Check JWT token generation includes role claim" -ForegroundColor White
Write-Host ""
Write-Host "4. Verify Spring Security role mapping (ROLE_ prefix)" -ForegroundColor White