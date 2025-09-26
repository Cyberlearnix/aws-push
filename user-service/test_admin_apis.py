#!/usr/bin/env python3
"""
Test script for Admin APIs
This script tests the admin login flow and API access
"""

import requests
import json
import time

BASE_URL = "http://localhost:9090"
ADMIN_EMAIL = "cyberlearnix@gmail.com"

def test_admin_flow():
    print("🔐 Testing Admin API Flow...")
    
    # Step 1: Send OTP to admin email
    print("\n1. Sending OTP to admin email...")
    try:
        response = requests.post(f"{BASE_URL}/api/email-auth/send-otp", 
                               json={"email": ADMIN_EMAIL})
        print(f"Status: {response.status_code}")
        print(f"Response: {response.json()}")
        
        if response.status_code == 200:
            otp_data = response.json()
            otp_session_id = otp_data.get("otpSessionId")
            print(f"✅ OTP sent successfully. Session ID: {otp_session_id}")
            
            # Step 2: Verify OTP (you'll need to check email for actual OTP)
            print(f"\n2. Please check your email for the OTP and enter it below:")
            print(f"Session ID: {otp_session_id}")
            print("Then run: python test_admin_apis.py verify <OTP>")
            
        else:
            print("❌ OTP send failed")
    except Exception as e:
        print(f"❌ Error: {e}")

def test_admin_verify(otp):
    print(f"🔐 Testing Admin OTP Verification with OTP: {otp}")
    
    # Step 2: Verify OTP
    try:
        response = requests.post(f"{BASE_URL}/api/email-auth/verify-otp", 
                               json={
                                   "email": ADMIN_EMAIL,
                                   "otpSessionId": "{{otp_session_id}}",  # You need to replace this
                                   "otp": otp
                               })
        print(f"Status: {response.status_code}")
        print(f"Response: {response.json()}")
        
        if response.status_code == 200:
            data = response.json()
            if data.get("success") and data.get("userExists"):
                access_token = data.get("accessToken")
                print(f"✅ Admin login successful!")
                print(f"Access Token: {access_token[:50]}...")
                
                # Step 3: Test admin endpoints
                test_admin_endpoints(access_token)
            else:
                print("❌ Admin login failed")
        else:
            print("❌ OTP verification failed")
    except Exception as e:
        print(f"❌ Error: {e}")

def test_admin_endpoints(access_token):
    print("\n3. Testing Admin Endpoints...")
    
    headers = {"Authorization": f"Bearer {access_token}"}
    
    # Test get all users
    print("\n   Testing GET /api/admin/all-users-details...")
    try:
        response = requests.get(f"{BASE_URL}/api/admin/all-users-details", headers=headers)
        print(f"   Status: {response.status_code}")
        if response.status_code == 200:
            data = response.json()
            print(f"   ✅ Success! Found {data.get('count', 0)} users")
        else:
            print(f"   ❌ Failed: {response.text}")
    except Exception as e:
        print(f"   ❌ Error: {e}")
    
    # Test create user
    print("\n   Testing POST /api/admin/add-users...")
    try:
        user_data = {
            "fullName": "Test User",
            "email": "testuser@example.com",
            "phone": "1234567890",
            "password": "TestPassword123!",
            "role": "STUDENT"
        }
        response = requests.post(f"{BASE_URL}/api/admin/add-users", 
                               json=user_data, headers=headers)
        print(f"   Status: {response.status_code}")
        if response.status_code == 200:
            print(f"   ✅ User created successfully!")
        else:
            print(f"   ❌ Failed: {response.text}")
    except Exception as e:
        print(f"   ❌ Error: {e}")

def test_public_endpoints():
    print("\n🌐 Testing Public Endpoints...")
    
    # Test refresh token (should work without auth)
    print("\n   Testing POST /api/auth/refresh-token...")
    try:
        response = requests.post(f"{BASE_URL}/api/auth/refresh-token", 
                               json={"refreshToken": "invalid_token"})
        print(f"   Status: {response.status_code}")
        if response.status_code == 401:
            print(f"   ✅ Correctly rejects invalid refresh token")
        else:
            print(f"   Response: {response.json()}")
    except Exception as e:
        print(f"   ❌ Error: {e}")
    
    # Test logout
    print("\n   Testing POST /api/auth/logout...")
    try:
        response = requests.post(f"{BASE_URL}/api/auth/logout")
        print(f"   Status: {response.status_code}")
        if response.status_code == 200:
            print(f"   ✅ Logout endpoint working")
    except Exception as e:
        print(f"   ❌ Error: {e}")
    
    # Test forgot password
    print("\n   Testing POST /api/users/forgot-password...")
    try:
        response = requests.post(f"{BASE_URL}/api/users/forgot-password", 
                               json={"email": "test@example.com"})
        print(f"   Status: {response.status_code}")
        if response.status_code == 202:
            print(f"   ✅ Forgot password endpoint working")
    except Exception as e:
        print(f"   ❌ Error: {e}")

if __name__ == "__main__":
    import sys
    
    if len(sys.argv) > 1 and sys.argv[1] == "verify":
        if len(sys.argv) > 2:
            test_admin_verify(sys.argv[2])
        else:
            print("Usage: python test_admin_apis.py verify <OTP>")
    else:
        test_admin_flow()
        test_public_endpoints()
        
        print("\n📝 Instructions:")
        print("1. Check your email for the OTP")
        print("2. Run: python test_admin_apis.py verify <OTP>")
        print("3. The script will test admin endpoints with the access token")
