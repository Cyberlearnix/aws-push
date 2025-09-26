#!/usr/bin/env python3
"""
Simple API test script for User Service
Run this after starting the Spring Boot application
"""

import requests
import json
import time

BASE_URL = "http://localhost:9090"

def test_api():
    print("🚀 Testing User Service APIs...")
    
    # Test 1: Send OTP
    print("\n1. Testing Send OTP...")
    try:
        response = requests.post(f"{BASE_URL}/api/email-auth/send-otp", 
                               json={"email": "test@example.com"})
        print(f"Status: {response.status_code}")
        print(f"Response: {response.json()}")
        
        if response.status_code == 200:
            otp_data = response.json()
            otp_session_id = otp_data.get("otpSessionId")
            print(f"✅ OTP sent successfully. Session ID: {otp_session_id}")
        else:
            print("❌ OTP send failed")
    except Exception as e:
        print(f"❌ Error: {e}")
    
    # Test 2: Health check - Get all users (should require auth)
    print("\n2. Testing Get All Users (without auth)...")
    try:
        response = requests.get(f"{BASE_URL}/api/admin/all-users-details")
        print(f"Status: {response.status_code}")
        if response.status_code == 401:
            print("✅ Correctly requires authentication")
        else:
            print(f"Response: {response.json()}")
    except Exception as e:
        print(f"❌ Error: {e}")
    
    # Test 3: Test refresh token endpoint
    print("\n3. Testing Refresh Token (with invalid token)...")
    try:
        response = requests.post(f"{BASE_URL}/api/auth/refresh-token", 
                               json={"refreshToken": "invalid_token"})
        print(f"Status: {response.status_code}")
        print(f"Response: {response.json()}")
        if response.status_code == 401:
            print("✅ Correctly rejects invalid refresh token")
    except Exception as e:
        print(f"❌ Error: {e}")
    
    # Test 4: Test logout
    print("\n4. Testing Logout...")
    try:
        response = requests.post(f"{BASE_URL}/api/auth/logout")
        print(f"Status: {response.status_code}")
        print(f"Response: {response.json()}")
        if response.status_code == 200:
            print("✅ Logout endpoint working")
    except Exception as e:
        print(f"❌ Error: {e}")
    
    # Test 5: Test forgot password
    print("\n5. Testing Forgot Password...")
    try:
        response = requests.post(f"{BASE_URL}/api/users/forgot-password", 
                               json={"email": "test@example.com"})
        print(f"Status: {response.status_code}")
        print(f"Response: {response.json()}")
        if response.status_code == 202:
            print("✅ Forgot password endpoint working")
    except Exception as e:
        print(f"❌ Error: {e}")
    
    print("\n🎉 Basic API tests completed!")
    print("\n📝 To test with real data:")
    print("1. Import the Postman collection: UserService_API_Collection.postman_collection.json")
    print("2. Set base_url variable to http://localhost:9090")
    print("3. Start with 'Send OTP' for admin email: cyberlearnix@gmail.com")
    print("4. Use the returned tokens in subsequent requests")

if __name__ == "__main__":
    test_api()
