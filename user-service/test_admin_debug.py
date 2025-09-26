#!/usr/bin/env python3
"""
Debug script to test admin login and token issues
"""

import requests
import json

BASE_URL = "http://localhost:9090"
ADMIN_EMAIL = "cyberlearnix@gmail.com"

def test_admin_debug():
    print("🔍 Debugging Admin API Issues...")
    
    # Step 1: Test if app is running
    print("\n1. Testing if application is running...")
    try:
        response = requests.get(f"{BASE_URL}/api/auth/logout", timeout=5)
        print(f"   Status: {response.status_code}")
        if response.status_code == 200:
            print("   ✅ Application is running")
        else:
            print("   ❌ Application not responding properly")
            return
    except Exception as e:
        print(f"   ❌ Application not running: {e}")
        return
    
    # Step 2: Send OTP to admin
    print("\n2. Sending OTP to admin email...")
    try:
        response = requests.post(f"{BASE_URL}/api/email-auth/send-otp", 
                               json={"email": ADMIN_EMAIL})
        print(f"   Status: {response.status_code}")
        print(f"   Response: {response.json()}")
        
        if response.status_code == 200:
            data = response.json()
            otp_session_id = data.get("otpSessionId")
            print(f"   ✅ OTP sent. Session ID: {otp_session_id}")
            
            # Step 3: Test admin endpoint without token (should get 401)
            print("\n3. Testing admin endpoint without token...")
            try:
                response = requests.get(f"{BASE_URL}/api/admin/all-users-details")
                print(f"   Status: {response.status_code}")
                if response.status_code == 401:
                    print("   ✅ Correctly requires authentication")
                else:
                    print(f"   ❌ Unexpected response: {response.text}")
            except Exception as e:
                print(f"   ❌ Error: {e}")
            
            # Step 4: Test with invalid token
            print("\n4. Testing admin endpoint with invalid token...")
            try:
                headers = {"Authorization": "Bearer invalid-token"}
                response = requests.get(f"{BASE_URL}/api/admin/all-users-details", headers=headers)
                print(f"   Status: {response.status_code}")
                print(f"   Response: {response.text}")
            except Exception as e:
                print(f"   ❌ Error: {e}")
                
        else:
            print("   ❌ Failed to send OTP")
    except Exception as e:
        print(f"   ❌ Error: {e}")

if __name__ == "__main__":
    test_admin_debug()
