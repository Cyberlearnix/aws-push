# Complete Admin 403 Fix Script
param(
    [string]$Email = "cyberlearnixprivatelimited@gmail.com",
    [string]$BaseUrl = "http://localhost:8081"
)

Write-Host "🔧 Complete Admin 403 Debugging & Fix Script" -ForegroundColor Yellow
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
    
    Write-Host "✅ OTP sent successfully" -ForegroundColor Green
    Write-Host "Session ID: $($otpData.otpSessionId)" -ForegroundColor Cyan
    
    # Get OTP from user
    $otp = Read-Host "Enter the OTP from your email"
    
    # Step 2: Verify OTP
    Write-Host "`nStep 2: Verifying OTP and getting token..." -ForegroundColor Yellow
    $verifyBody = @{
        email = $Email
        otpSessionId = $otpData.otpSessionId
        otp = $otp
    } | ConvertTo-Json
    
    try {
        $verifyResponse = Invoke-WebRequest -Uri "$BaseUrl/api/email-auth/verify-otp" -Method POST -Body $verifyBody -Headers $headers
        $verifyData = $verifyResponse.Content | ConvertFrom-Json
        
        if ($verifyData.success -and $verifyData.accessToken) {
            Write-Host "✅ OTP verified successfully" -ForegroundColor Green
            $accessToken = $verifyData.accessToken
            
            # Display user information
            Write-Host "`n👤 User Information:" -ForegroundColor Yellow
            Write-Host "Email: $($verifyData.user.email)" -ForegroundColor White
            Write-Host "Role: $($verifyData.user.role)" -ForegroundColor White
            Write-Host "Is Active: $($verifyData.user.isActive)" -ForegroundColor White
            
            # Check role
            if ($verifyData.user.role -ne "ADMIN") {
                Write-Host "`n⚠️ PROBLEM IDENTIFIED: User role is '$($verifyData.user.role)', not 'ADMIN'" -ForegroundColor Red
                Write-Host "This is why you're getting 403 Forbidden!" -ForegroundColor Red
                
                Write-Host "`n🔧 SOLUTION OPTIONS:" -ForegroundColor Yellow
                Write-Host "1. Update this user's role to ADMIN in database" -ForegroundColor White
                Write-Host "2. Create a new admin user" -ForegroundColor White
                Write-Host "3. Use an existing admin user" -ForegroundColor White
                
                $choice = Read-Host "`nDo you want me to show you the SQL to fix this? (y/n)"
                if ($choice -eq "y" -or $choice -eq "Y") {
                    Write-Host "`n📝 SQL to fix the role:" -ForegroundColor Cyan
                    Write-Host "UPDATE user_entity SET role = 'ADMIN' WHERE email = '$Email';" -ForegroundColor Gray
                    Write-Host "`nRun this in your PostgreSQL database, then restart the application and try again." -ForegroundColor Yellow
                }
                return
            }
            
            Write-Host "✅ User has ADMIN role - proceeding with admin endpoint test" -ForegroundColor Green
            
            # Step 3: Test Admin Endpoint with detailed debugging
            Write-Host "`nStep 3: Testing admin endpoint..." -ForegroundColor Yellow
            $adminHeaders = @{ 
                "Authorization" = "Bearer $accessToken"
                "Content-Type" = "application/json"
            }
            
            Write-Host "Using Authorization header: Bearer $($accessToken.Substring(0, 50))..." -ForegroundColor Gray
            
            try {
                # Test the admin endpoint
                $adminResponse = Invoke-WebRequest -Uri "$BaseUrl/api/admin/all-users-details" -Method GET -Headers $adminHeaders
                Write-Host "✅ SUCCESS! Admin endpoint worked!" -ForegroundColor Green
                Write-Host "Status Code: $($adminResponse.StatusCode)" -ForegroundColor Cyan
                
                $adminData = $adminResponse.Content | ConvertFrom-Json
                Write-Host "Found $($adminData.count) users in the system" -ForegroundColor Cyan
                
                # Show first few users
                if ($adminData.users -and $adminData.users.Length -gt 0) {
                    Write-Host "`n📋 Sample users:" -ForegroundColor Yellow
                    $adminData.users | Select-Object -First 3 | ForEach-Object {
                        Write-Host "- $($_.email) ($($_.role)) - Active: $($_.isActive)" -ForegroundColor White
                    }
                }
                
            } catch {
                $statusCode = "Unknown"
                $errorBody = ""
                
                # Extract detailed error information
                if ($_.Exception.Response) {
                    $statusCode = [int]$_.Exception.Response.StatusCode
                    try {
                        $stream = $_.Exception.Response.GetResponseStream()
                        $reader = New-Object System.IO.StreamReader($stream)
                        $errorBody = $reader.ReadToEnd()
                        $reader.Close()
                        $stream.Close()
                    } catch { }
                }
                
                Write-Host "❌ Admin endpoint failed!" -ForegroundColor Red
                Write-Host "Status Code: $statusCode" -ForegroundColor Red
                Write-Host "Error Body: $errorBody" -ForegroundColor Red
                
                # Detailed diagnosis based on status code
                switch ($statusCode) {
                    401 {
                        Write-Host "`n🔍 401 Unauthorized - Token Issues:" -ForegroundColor Yellow
                        Write-Host "- Token might be expired" -ForegroundColor White
                        Write-Host "- Token might be malformed" -ForegroundColor White
                        Write-Host "- JWT secret mismatch" -ForegroundColor White
                        
                        # Try to decode token locally (basic check)
                        try {
                            $tokenParts = $accessToken.Split('.')
                            if ($tokenParts.Length -eq 3) {
                                Write-Host "- Token has 3 parts (looks valid structurally)" -ForegroundColor White
                            } else {
                                Write-Host "- Token does NOT have 3 parts (malformed)" -ForegroundColor Red
                            }
                        } catch {
                            Write-Host "- Could not parse token structure" -ForegroundColor Red
                        }
                    }
                    403 {
                        Write-Host "`n🔍 403 Forbidden - Authorization Issues:" -ForegroundColor Yellow
                        Write-Host "- Token is valid but user lacks ADMIN role" -ForegroundColor White
                        Write-Host "- Spring Security role mapping issue" -ForegroundColor White
                        Write-Host "- PreAuthorize annotation not working" -ForegroundColor White
                        
                        Write-Host "`n🔧 Debugging Steps:" -ForegroundColor Cyan
                        Write-Host "1. Check application logs for JWT filter messages" -ForegroundColor White
                        Write-Host "2. Verify role in database: SELECT email, role FROM user_entity WHERE email = '$Email';" -ForegroundColor Gray
                        Write-Host "3. Check if JWT token contains correct role claim" -ForegroundColor White
                        Write-Host "4. Verify Spring Security configuration" -ForegroundColor White
                    }
                    404 {
                        Write-Host "`n🔍 404 Not Found - Endpoint Issues:" -ForegroundColor Yellow
                        Write-Host "- Admin controller not mapped correctly" -ForegroundColor White
                        Write-Host "- Application not running on expected port" -ForegroundColor White
                        Write-Host "- URL mismatch" -ForegroundColor White
                    }
                    500 {
                        Write-Host "`n🔍 500 Internal Server Error:" -ForegroundColor Yellow
                        Write-Host "- Check application logs for exceptions" -ForegroundColor White
                        Write-Host "- Database connection issues" -ForegroundColor White
                        Write-Host "- Service layer errors" -ForegroundColor White
                    }
                }
                
                Write-Host "`n🔍 Next Steps:" -ForegroundColor Yellow
                Write-Host "1. Check the application console logs for JWT filter debug messages" -ForegroundColor White
                Write-Host "2. Look for any exceptions in the application logs" -ForegroundColor White
                Write-Host "3. Verify the user's role in the database" -ForegroundColor White
                Write-Host "4. Make sure Spring Security is properly configured" -ForegroundColor White
            }
            
        } else {
            Write-Host "❌ OTP verification failed or no access token received" -ForegroundColor Red
            Write-Host "Response: $($verifyResponse.Content)" -ForegroundColor Yellow
        }
        
    } catch {
        Write-Host "❌ OTP verification failed: $($_.Exception.Message)" -ForegroundColor Red
    }
    
} catch {
    Write-Host "❌ Failed to send OTP: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Make sure the application is running on $BaseUrl" -ForegroundColor Yellow
}

Write-Host "`n📋 Summary & Troubleshooting Guide:" -ForegroundColor Yellow
Write-Host ""
Write-Host "Common causes of 403 Forbidden on admin endpoints:" -ForegroundColor White
Write-Host "1. User doesn't have ADMIN role in database" -ForegroundColor Gray
Write-Host "2. JWT token doesn't contain role claim" -ForegroundColor Gray
Write-Host "3. Spring Security role mapping issue (ROLE_ prefix)" -ForegroundColor Gray
Write-Host "4. PreAuthorize annotation not working" -ForegroundColor Gray
Write-Host "5. JWT filter not processing admin endpoints" -ForegroundColor Gray
Write-Host ""
Write-Host "Quick database checks:" -ForegroundColor White
Write-Host "SELECT email, role, is_active FROM user_entity WHERE email = '$Email';" -ForegroundColor Gray
Write-Host "UPDATE user_entity SET role = 'ADMIN' WHERE email = '$Email';" -ForegroundColor Gray