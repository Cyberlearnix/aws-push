# 🚀 CyberLearnix LMS Microservices Startup Guide

## ✅ All Services Updated - Direct Routing Architecture!

All your microservices have been updated to use direct routing through the API Gateway instead of Eureka service discovery. Here's the updated setup:

## 🏗️ **Updated Service Architecture Overview**

| Service | Port | Purpose | Database | Status |
|---------|------|---------|----------|--------|
| **api-gateway** | 8080 | API Gateway & Load Balancer | - | ✅ Entry Point |
| **user-service** | 8081 | User Management | PostgreSQL + Redis | ✅ Ready |
| **student-service** | 8082 | Student Management | PostgreSQL | ✅ Ready |
| **instructor-service** | 8083 | Instructor Management | PostgreSQL | ✅ Ready |
| **course-service** | 8085 | Course Management | PostgreSQL | ✅ Ready |
| **order-service** | 8084 | Order Processing | PostgreSQL | ✅ Ready |
| **payment-service** | 8086 | Payment Processing | - | ✅ Ready |

## 🎯 **How to Start All Services**

### **Option 1: Using Docker Compose (Recommended)**

```bash
# Start all services with one command
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down -v
```

### **Option 2: Start Services Individually**

```bash
# 1. Start Database and Cache
docker-compose up postgres redis -d

# 2. Start Services (any order)
./gradlew :user-service:bootRun --no-daemon &
./gradlew :student-service:bootRun --no-daemon &
./gradlew :instructor-service:bootRun --no-daemon &
./gradlew :course-service:bootRun --no-daemon &
./gradlew :order-service:bootRun --no-daemon &
./gradlew :payment-service:bootRun --no-daemon &

# 3. Start API Gateway Last
./gradlew :api-gateway:bootRun --no-daemon
```

### **Option 3: Start Multiple Services in Parallel**

Open multiple terminal windows and run:

**Terminal 1:**
```bash
./gradlew :user-service:bootRun
```

**Terminal 2:**
```bash
./gradlew :student-service:bootRun
```

**Terminal 3:**
```bash
./gradlew :instructor-service:bootRun
```

**Terminal 4:**
```bash
./gradlew :course-service:bootRun
```

**Terminal 5:**
```bash
./gradlew :order-service:bootRun
```

**Terminal 6:**
```bash
./gradlew :payment-service:bootRun
```

**Terminal 7 (start last):**
```bash
./gradlew :api-gateway:bootRun
```

## 🔧 **Prerequisites**

### **1. Database Setup**
Make sure PostgreSQL and Redis are running:
```bash
# Using Docker
docker-compose up postgres redis -d

# Or manually
# PostgreSQL: localhost:5432, database: cyberlearnixdb, user: cyberlearnix, pass: cyberlearnix123
# Redis: localhost:6379
```

### **2. Java Version**
- All services use **Java 21** (automatically handled by Gradle toolchain)

### **3. Environment Variables (Optional)**
You can override default configurations with environment variables:
```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/cyberlearnixdb
SPRING_DATASOURCE_USERNAME=cyberlearnix
SPRING_DATASOURCE_PASSWORD=cyberlearnix123
SPRING_DATA_REDIS_HOST=localhost
SPRING_DATA_REDIS_PORT=6379
```

## 📍 **Service Health Checks**

Once all services are running, verify they're healthy:

| Service | Health Check URL |
|---------|------------------|
| API Gateway | http://localhost:8080/actuator/health |
| User Service | http://localhost:8081/actuator/health |
| Student Service | http://localhost:8082/actuator/health |
| Instructor Service | http://localhost:8083/actuator/health |
| Course Service | http://localhost:8085/actuator/health |
| Order Service | http://localhost:8084/actuator/health |
| Payment Service | http://localhost:8086/actuator/health |

## 🌐 **API Access Through Gateway**

Once the API Gateway is running, access all services through:
- **Base URL**: http://localhost:8080/api/

**Available Routes:**
- **User Service**: http://localhost:8080/api/users/**
- **Email Auth**: http://localhost:8080/api/email-auth/**
- **Admin**: http://localhost:8080/api/admin/**
- **Student Service**: http://localhost:8080/api/students/**
- **Instructor Service**: http://localhost:8080/api/instructors/**
- **Course Service**: http://localhost:8080/api/courses/**
- **Order Service**: http://localhost:8080/api/orders/**
- **Payment Service**: http://localhost:8080/api/payments/**

## 🚨 **Troubleshooting**

### **If a service fails to start:**

1. **Check if the port is already in use:**
   ```bash
   netstat -ano | findstr :8084  # Replace with the service port
   ```

2. **Check service logs:**
   - Services show detailed logs in the terminal
   - Look for database connection errors, port conflicts, etc.

3. **Database connection issues:**
   - Verify PostgreSQL is running on port 5432
   - Check database credentials in application properties
   - Ensure database `cyberlearnixdb` exists

4. **Redis connection issues:**
   - Verify Redis is running on port 6379
   - Check if Redis is accessible

### **Build Issues:**

If you encounter build errors:
```bash
# Clean and rebuild all services
./gradlew clean build --no-daemon

# Or clean individual service
./gradlew :course-service:clean :course-service:build --no-daemon
```

## 🎉 **Success Indicators**

Your microservices ecosystem is working when:
- ✅ All health check URLs return "UP" status
- ✅ API Gateway can route requests to backend services
- ✅ Services can communicate with each other through direct URLs
- ✅ Database connections are established
- ✅ Redis caching is working (for user-service)

## 🔄 **Development Workflow**

For development, start services in any order (dependencies are minimal):

**Quick Development Setup:**
```bash
# Start infrastructure
docker-compose up postgres redis -d

# Start services in any order
./gradlew :user-service:bootRun &
./gradlew :student-service:bootRun &
./gradlew :instructor-service:bootRun &
./gradlew :course-service:bootRun &
./gradlew :order-service:bootRun &
./gradlew :payment-service:bootRun &

# Start API Gateway last
./gradlew :api-gateway:bootRun
```

## 📋 **Architecture Changes**

**What Changed:**
- ❌ **Removed**: Eureka Server (service discovery)
- ❌ **Removed**: Config Server (centralized configuration)
- ✅ **Added**: Direct service routing through API Gateway
- ✅ **Updated**: All services use Java 21
- ✅ **Simplified**: No service discovery complexity

**Benefits:**
- 🚀 **Faster startup** (no service discovery overhead)
- 🔧 **Simpler configuration** (direct URLs instead of service discovery)
- 🛠️ **Easier debugging** (clear service-to-service communication)
- 📦 **Reduced complexity** (fewer moving parts)

**Happy coding! 🚀**