# User Service API Endpoints Documentation

## 🔧 **Fixed Issues:**
- ✅ Removed duplicate registration endpoints
- ✅ Removed duplicate admin endpoints  
- ✅ Fixed admin security using proper Spring Security annotations
- ✅ Cleaned up controller structure

---

## 📋 **Complete API List**

### **1. Email Authentication APIs** (`/api/email-auth`)
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/send-otp` | Send OTP to email | ❌ No |
| POST | `/verify-otp` | Verify OTP and get tokens | ❌ No |
| POST | `/register` | Register new user (after OTP) | ✅ Temp Token |

### **2. Authentication APIs** (`/api/auth`)
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/login-password` | Login with email & password | ❌ No |
| POST | `/refresh-token` | Generate new access token | ❌ No |
| POST | `/logout` | Invalidate session/token | ❌ No |

### **3. User APIs** (`/api/users`)
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| PUT | `/update` | Update user profile | ✅ User Token |
| DELETE | `/delete` | Delete own account (with OTP) | ✅ User Token |
| DELETE | `/{id}` | Admin delete any user | ✅ Admin Token |
| POST | `/forgot-password` | Request password reset | ❌ No |
| POST | `/reset-password` | Reset password with OTP | ❌ No |
| POST | `/upload-photo` | Upload/change profile picture | ✅ User Token |
| GET | `/{id}` | Get user profile by ID | ❌ No (Public) |

### **4. Admin APIs** (`/api/admin`) - **All require ADMIN role**
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/all-users-details` | Get all users | ✅ Admin Token |
| GET | `/users/{id}` | Get specific user | ✅ Admin Token |
| POST | `/add-users` | Create user manually | ✅ Admin Token |
| PUT | `/users/{id}` | Update user details | ✅ Admin Token |
| DELETE | `/users/{id}` | Delete user | ✅ Admin Token |
| POST | `/users/{id}/deactivate` | Suspend user | ✅ Admin Token |
| POST | `/users/{id}/activate` | Reactivate user | ✅ Admin Token |

---

## 🔐 **Security Implementation**

### **Public Endpoints** (No authentication required):
- All `/api/email-auth/**` endpoints
- `/api/auth/login-password`
- `/api/auth/refresh-token` 
- `/api/auth/logout`
- `/api/users/forgot-password`
- `/api/users/reset-password`
- `/api/users/{id}` (get user by ID)

### **User Authentication Required**:
- `/api/users/update`
- `/api/users/delete`
- `/api/users/upload-photo`

### **Admin Role Required**:
- All `/api/admin/**` endpoints

---

## 🚀 **How to Test**

### **1. Admin Login Flow:**
```bash
# Step 1: Send OTP to admin email
POST /api/email-auth/send-otp
{
  "email": "cyberlearnix@gmail.com"
}

# Step 2: Verify OTP (check email for code)
POST /api/email-auth/verify-otp
{
  "email": "cyberlearnix@gmail.com",
  "otpSessionId": "session-id-from-step-1",
  "otp": "123456"
}

# Step 3: Use access token for admin endpoints
GET /api/admin/all-users-details
Authorization: Bearer <access-token>
```

### **2. User Registration Flow:**
```bash
# Step 1: Send OTP
POST /api/email-auth/send-otp
{
  "email": "user@example.com"
}

# Step 2: Verify OTP
POST /api/email-auth/verify-otp
{
  "email": "user@example.com", 
  "otpSessionId": "session-id",
  "otp": "123456"
}

# Step 3: Register with temp token
POST /api/email-auth/register
Authorization: Bearer <temp-token>
{
  "fullName": "John Doe",
  "phone": "1234567890",
  "password": "Password123!"
}
```

### **3. Password Login:**
```bash
POST /api/auth/login-password
{
  "email": "user@example.com",
  "password": "Password123!"
}
```

---

## 📝 **Request/Response Examples**

### **Send OTP Response:**
```json
{
  "success": true,
  "message": "OTP sent successfully",
  "otpSessionId": "uuid-here",
  "expirySeconds": 300,
  "deliveryMethod": "EMAIL",
  "maskedContact": "u***@example.com"
}
```

### **Verify OTP Response (Existing User):**
```json
{
  "success": true,
  "message": "Login successful",
  "userExists": true,
  "accessToken": "jwt-token-here",
  "refreshToken": "refresh-token-here",
  "user": {
    "id": "uuid",
    "fullName": "John Doe",
    "email": "user@example.com",
    "role": "STUDENT"
  }
}
```

### **Admin Get All Users Response:**
```json
{
  "success": true,
  "count": 5,
  "users": [
    {
      "id": "uuid",
      "fullName": "John Doe",
      "email": "user@example.com",
      "role": "STUDENT",
      "isActive": true
    }
  ]
}
```

---

## ⚠️ **Important Notes**

1. **No Duplicate Endpoints**: Removed duplicate registration and admin endpoints
2. **Proper Security**: Admin endpoints use `@PreAuthorize("hasRole('ROLE_ADMIN')")`
3. **Consistent Responses**: All endpoints return consistent JSON structure
4. **OTP Flow**: Registration requires OTP verification first
5. **Token Management**: Use refresh tokens to get new access tokens
6. **Public Access**: User profile by ID is publicly accessible

---

## 🧪 **Testing with Postman**

1. Import `UserService_API_Collection.postman_collection.json`
2. Set `base_url` variable to `http://localhost:9090`
3. Follow the admin login flow to get admin tokens
4. Test all endpoints with proper authentication

---

## 🔧 **Configuration Files Updated**

- `SecurityConfig.java` - Updated security rules
- `JwtAuthFilter.java` - Updated public endpoint patterns
- `AuthController.java` - Removed duplicates, cleaned up
- `AdminController.java` - Fixed security annotations
- `UserController.java` - Added new user endpoints
