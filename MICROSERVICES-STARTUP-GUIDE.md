# 🚀 CyberLearnix LMS Microservices Startup Guide

## ✅ All Services Now Configured!

All your microservices are now properly configured and ready to run. Here's the complete setup:

## 🏗️ **Service Architecture Overview**

| Service | Port | Purpose | Database | Status |
|---------|------|---------|----------|--------|
| **eureka-server** | 8761 | Service Discovery | - | ✅ Core Infrastructure |
| **config-server** | 8888 | Configuration Management | - | ✅ Core Infrastructure |
| **api-gateway** | 8080 | API Gateway | - | ✅ Entry Point |
| **user-service** | 8081 | User Management | PostgreSQL | ✅ Ready |
| **course-service** | 8083 | Course Management | PostgreSQL | ✅ Ready |
| **instructor-service** | 8084 | Instructor Management | PostgreSQL | ✅ Ready |
| **order-service** | 8085 | Order Processing | PostgreSQL | ✅ Ready |
| **payment-service** | 8086 | Payment Processing | PostgreSQL | ✅ Ready |
| **student_service** | 8082 | Student Management | PostgreSQL | ✅ Ready |

## 🎯 **How to Start All Services**

### **Option 1: Start Services Individually (Recommended Order)**

```bash
# 1. Start Core Infrastructure First
.\gradlew :eureka-server:bootRun --no-daemon
# Wait for Eureka to start (check http://localhost:8761)

# 2. Start Configuration Server
.\gradlew :config-server:bootRun --no-daemon

# 3. Start Business Services
.\gradlew :user-service:bootRun --no-daemon
.\gradlew :course-service:bootRun --no-daemon
.\gradlew :instructor-service:bootRun --no-daemon
.\gradlew :student_service:bootRun --no-daemon
.\gradlew :order-service:bootRun --no-daemon
.\gradlew :payment-service:bootRun --no-daemon

# 4. Start API Gateway Last
.\gradlew :api-gateway:bootRun --no-daemon
```

### **Option 2: Start Multiple Services in Parallel**

Open multiple terminal windows and run:

**Terminal 1:**
```bash
.\gradlew :eureka-server:bootRun
```

**Terminal 2 (wait 30 seconds after Terminal 1):**
```bash
.\gradlew :config-server:bootRun
```

**Terminal 3:**
```bash
.\gradlew :user-service:bootRun
```

**Terminal 4:**
```bash
.\gradlew :course-service:bootRun
```

**Terminal 5:**
```bash
.\gradlew :instructor-service:bootRun
```

**Terminal 6:**
```bash
.\gradlew :order-service:bootRun
```

**Terminal 7:**
```bash
.\gradlew :payment-service:bootRun
```

**Terminal 8:**
```bash
.\gradlew :student_service:bootRun
```

**Terminal 9 (start last):**
```bash
.\gradlew :api-gateway:bootRun
```

## 🔧 **Prerequisites**

### **1. Database Setup**
Make sure PostgreSQL is running with:
- **Host**: localhost:5432
- **Database**: cyberlearnixdb
- **Username**: cyberlearnix
- **Password**: cyberlearnix123

### **2. Java Version**
- Services use Java 17 or Java 21 (automatically handled by Gradle)

### **3. Environment Variables (Optional)**
You can override default configurations with environment variables:
```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/cyberlearnixdb
SPRING_DATASOURCE_USERNAME=cyberlearnix
SPRING_DATASOURCE_PASSWORD=cyberlearnix123
EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://localhost:8761/eureka/
```

## 📍 **Service Health Checks**

Once all services are running, verify they're healthy:

| Service | Health Check URL |
|---------|------------------|
| Eureka Server | http://localhost:8761 |
| Config Server | http://localhost:8888/actuator/health |
| API Gateway | http://localhost:8080/actuator/health |
| User Service | http://localhost:8081/actuator/health |
| Course Service | http://localhost:8083/actuator/health |
| Instructor Service | http://localhost:8084/actuator/health |
| Order Service | http://localhost:8085/actuator/health |
| Payment Service | http://localhost:8086/actuator/health |
| Student Service | http://localhost:8082/actuator/health |

## 🌐 **API Access Through Gateway**

Once the API Gateway is running, access services through:
- **Base URL**: http://localhost:8080
- **User Service**: http://localhost:8080/api/users/**
- **Course Service**: http://localhost:8080/api/courses/**
- **Instructor Service**: http://localhost:8080/api/instructors/**
- **Student Service**: http://localhost:8080/api/students/**
- **Order Service**: http://localhost:8080/api/orders/**
- **Payment Service**: http://localhost:8080/api/payments/**

## 🚨 **Troubleshooting**

### **If a service fails to start:**

1. **Check if the port is already in use:**
   ```bash
   netstat -ano | findstr :8084  # Replace with the service port
   ```

2. **Check Eureka registration:**
   - Go to http://localhost:8761
   - Verify services are registered

3. **Check service logs:**
   - Services show detailed logs in the terminal
   - Look for database connection errors, port conflicts, etc.

4. **Database connection issues:**
   - Verify PostgreSQL is running
   - Check database credentials
   - Ensure database `cyberlearnixdb` exists

### **Build Issues:**

If you encounter build errors:
```bash
# Clean and rebuild all services
.\gradlew clean build --no-daemon

# Or clean individual service
.\gradlew :course-service:clean :course-service:build --no-daemon
```

## 🎉 **Success Indicators**

Your microservices ecosystem is working when:
- ✅ Eureka Dashboard shows all services registered
- ✅ All health check URLs return "UP" status  
- ✅ API Gateway can route requests to backend services
- ✅ Services can communicate with each other
- ✅ Database connections are established

## 🔄 **Development Workflow**

For development, typically start in this order:
1. `eureka-server` (always first)
2. `config-server` (configuration)
3. Core services: `user-service`, `course-service`
4. Business services: `instructor-service`, `student_service`, `order-service`, `payment-service`
5. `api-gateway` (always last)

**Happy coding! 🚀**