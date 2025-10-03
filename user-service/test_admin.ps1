# Test Admin API Flow
param(
    [string]$Action = "send-otp",
    [string]$Otp = "",
    [string]$SessionId = ""
)

$BASE_URL = "http://localhost:8081"
$ADMIN_EMAIL = "cyberlearnix@gmail.com"

function Test-AdminOtpSend {
    Write-Host "🔐 Sending OTP to admin email..." -ForegroundColor Yellow
    
    $body = @{
        email = $ADMIN_EMAIL
    } | ConvertTo-Json
    
    try {
        $response = Invoke-WebRequest -Uri "$BASE_URL/api/email-auth/send-otp" -Method POST -Body $body -ContentType "application/json"
        Write-Host "Status: $($response.StatusCode)" -ForegroundColor Green
        
        if ($response.StatusCode -eq 200) {
            $data = $response.Content | ConvertFrom-Json
            Write-Host "✅ OTP sent successfully!" -ForegroundColor Green
            Write-Host "Session ID: $($data.otpSessionId)" -ForegroundColor Cyan
            Write-Host "Masked Contact: $($data.maskedContact)" -ForegroundColor Cyan
            Write-Host ""
            Write-Host "Next step:" -ForegroundColor Yellow
            Write-Host "  .\test_admin.ps1 -Action verify-otp -Otp YOUR_OTP -SessionId '$($data.otpSessionId)'" -ForegroundColor White
            return $data.otpSessionId
        }
    }
    catch {
        Write-Host "❌ Error sending OTP: $($_.Exception.Message)" -ForegroundColor Red
    }
}

function Test-AdminOtpVerify {
    param($Otp, $SessionId)
    
    Write-Host "🔐 Verifying OTP..." -ForegroundColor Yellow
    
    $body = @{
        email = $ADMIN_EMAIL
        otpSessionId = $SessionId
        otp = $Otp
    } | ConvertTo-Json
    
    try {
        $response = Invoke-WebRequest -Uri "$BASE_URL/api/email-auth/verify-otp" -Method POST -Body $body -ContentType "application/json"
        Write-Host "Status: $($response.StatusCode)" -ForegroundColor Green
        
        if ($response.StatusCode -eq 200) {
            $data = $response.Content | ConvertFrom-Json
            Write-Host "Response: $($response.Content)" -ForegroundColor Cyan
            
            if ($data.success -and $data.userExists -and $data.accessToken) {
                Write-Host "✅ Admin login successful!" -ForegroundColor Green
                Write-Host "Access Token: $($data.accessToken.Substring(0, [Math]::Min(50, $data.accessToken.Length)))..." -ForegroundColor Cyan
                
                # Test admin endpoints
                Test-AdminEndpoints -AccessToken $data.accessToken
            }
            else {
                Write-Host "❌ Login failed or user doesn't exist" -ForegroundColor Red
                Write-Host "User exists: $($data.userExists)" -ForegroundColor Yellow
                Write-Host "Has access token: $($data.accessToken -ne $null)" -ForegroundColor Yellow
            }
        }
    }
    catch {
        Write-Host "❌ Error verifying OTP: $($_.Exception.Message)" -ForegroundColor Red
        if ($_.Exception.Response) {
            $errorContent = $_.Exception.Response.GetResponseStream()
            Write-Host "Error details: $errorContent" -ForegroundColor Red
        }
    }
}

function Test-AdminEndpoints {
    param($AccessToken)
    
    Write-Host "`n🔍 Testing Admin Endpoints..." -ForegroundColor Yellow
    
    $headers = @{
        Authorization = "Bearer $AccessToken"
    }
    
    # Test debug endpoint first (if available after rebuild)
    Write-Host "`n   Testing debug endpoint..." -ForegroundColor White
    try {
        $response = Invoke-WebRequest -Uri "$BASE_URL/api/admin/debug/auth" -Method GET -Headers $headers
        Write-Host "   Debug Status: $($response.StatusCode)" -ForegroundColor Green
        Write-Host "   Debug Response: $($response.Content)" -ForegroundColor Cyan
    }
    catch {
        Write-Host "   Debug endpoint not available (needs rebuild)" -ForegroundColor Yellow
    }
    
    # Test get all users
    Write-Host "`n   Testing GET /api/admin/all-users-details..." -ForegroundColor White
    try {
        $response = Invoke-WebRequest -Uri "$BASE_URL/api/admin/all-users-details" -Method GET -Headers $headers
        Write-Host "   Status: $($response.StatusCode)" -ForegroundColor Green
        
        if ($response.StatusCode -eq 200) {
            $data = $response.Content | ConvertFrom-Json
            Write-Host "   ✅ Success! Found $($data.count) users" -ForegroundColor Green
        }
    }
    catch {
        $statusCode = if ($_.Exception.Response) { $_.Exception.Response.StatusCode } else { "Unknown" }
        Write-Host "   ❌ Failed with status: $statusCode" -ForegroundColor Red
        
        if ($statusCode -eq "Forbidden") {
            Write-Host "   🚨 403 Forbidden - This indicates a role/permission issue!" -ForegroundColor Red
            Write-Host "   Possible causes:" -ForegroundColor Yellow
            Write-Host "     1. User doesn't have ADMIN role in database" -ForegroundColor Yellow
            Write-Host "     2. JWT token doesn't contain correct role claim" -ForegroundColor Yellow
            Write-Host "     3. Spring Security authority mapping issue" -ForegroundColor Yellow
        }
    }
}

function Test-WithoutAuth {
    Write-Host "`n🔍 Testing endpoint without authentication..." -ForegroundColor Yellow
    
    try {
        $response = Invoke-WebRequest -Uri "$BASE_URL/api/admin/all-users-details" -Method GET -ErrorAction Stop
    }
    catch {
        $statusCode = if ($_.Exception.Response) { $_.Exception.Response.StatusCode } else { "Unknown" }
        Write-Host "   Status without auth: $statusCode" -ForegroundColor Cyan
        
        if ($statusCode -eq "Unauthorized") {
            Write-Host "   ✅ Correctly requires authentication" -ForegroundColor Green
        }
    }
}

# Main execution
Write-Host "🚀 Admin API Test Script" -ForegroundColor Magenta
Write-Host "=========================" -ForegroundColor Magenta

switch ($Action.ToLower()) {
    "send-otp" {
        Test-WithoutAuth
        Test-AdminOtpSend
    }
    "verify-otp" {
        if (-not $Otp -or -not $SessionId) {
            Write-Host "❌ Please provide both OTP and SessionId" -ForegroundColor Red
            Write-Host "Usage: .\test_admin.ps1 -Action verify-otp -Otp YOUR_OTP -SessionId 'session-id-here'" -ForegroundColor Yellow
            exit 1
        }
        Test-AdminOtpVerify -Otp $Otp -SessionId $SessionId
    }
    default {
        Write-Host "Usage:" -ForegroundColor Yellow
        Write-Host "  .\test_admin.ps1 -Action send-otp" -ForegroundColor White
        Write-Host "  .\test_admin.ps1 -Action verify-otp -Otp YOUR_OTP -SessionId 'session-id'" -ForegroundColor White
    }
}