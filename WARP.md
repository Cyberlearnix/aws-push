# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Architecture Overview

This is a microservices-based Learning Management System (LMS) built with Spring Boot and Spring Cloud. The system uses service discovery, API gateway patterns, and JWT-based authentication.

### Core Services Architecture
- **Eureka Server** (port 8761): Service registry and discovery
- **API Gateway** (port 8080): Routes requests to microservices with load balancing
- **User Service** (port 8081): User management, authentication, OTP-based registration
- **Student Service** (port 8081): Student operations, course enrollment, progress tracking
- **Config Server**: Centralized configuration (planned)

### Key Technologies
- Java 21 with Spring Boot 3.x
- Spring Cloud Gateway & Eureka for microservices
- PostgreSQL database
- JWT authentication with role-based access control
- Gradle build system

### Service Communication
Services communicate through:
- API Gateway routing with load balancing (`lb://service-name`)
- Eureka service discovery for dynamic service registration
- Feign clients for inter-service communication
- JWT token propagation for security

## Common Development Commands

### Build and Test
```bash
# Build entire project
./gradlew build

# Run tests for all services
./gradlew test

# Build specific service
cd <service-directory>
./gradlew build

# Run tests for specific service
cd <service-directory>
./gradlew test
```

### Service Startup (Required Order)
```bash
# 1. Start Eureka Server first
cd eureka-server
./gradlew bootRun

# 2. Start API Gateway (new terminal)
cd api-gateway
./gradlew bootRun

# 3. Start User Service (new terminal)
cd userservic
./gradlew bootRun

# 4. Start Student Service (new terminal) 
cd student_service
./gradlew bootRun
```

### Quick Service Health Check
```bash
# Check Eureka Dashboard
open http://localhost:8761

# Check service health endpoints
curl http://localhost:8080/actuator/health  # API Gateway
curl http://localhost:8081/actuator/health  # User/Student Service
```

### Database Setup
Services use PostgreSQL with the following default configuration:
- Database: `cyberlearnixdb`
- Username: `cyberlearnix` 
- Password: `cyberlearnix123`
- Port: 5432

Create the database:
```bash
createdb cyberlearnixdb
```

## API Testing

### User Service Authentication Flow
```bash
# 1. Send OTP for registration/login
curl -X POST http://localhost:8080/api/users/email-auth/send-otp \
  -H "Content-Type: application/json" \
  -d '{"email": "test@example.com"}'

# 2. Verify OTP and get JWT
curl -X POST http://localhost:8080/api/users/email-auth/verify-otp \
  -H "Content-Type: application/json" \
  -d '{"email": "test@example.com", "otpSessionId": "session-id", "otp": "123456"}'

# 3. Use JWT for authenticated requests
curl -H "Authorization: Bearer <jwt-token>" \
  http://localhost:8080/api/users/admin/all-users-details
```

### Student Service Testing
```bash
# Login to get JWT
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "testuser@example.com", "password": "test123"}'

# Get student dashboard
curl -H "Authorization: Bearer <jwt-token>" \
  http://localhost:8081/students/1/dashboard
```

### Key API Routes via Gateway
- User Service: `http://localhost:8080/api/users/**` → `userservic:8081`
- Student Service: `http://localhost:8080/api/students/**` → `student_service:8081`

## Service-Specific Notes

### User Service (`userservic/`)
- Handles OTP-based registration and password-based login
- JWT token generation and validation
- Role-based access control (STUDENT, INSTRUCTOR, ADMIN)
- Email integration for OTP delivery
- Admin APIs for user management

### Student Service (`student_service/`)
- Course enrollment and progress tracking
- Quiz and assignment management
- Certificate generation and verification
- Course reviews and communications
- Learning analytics dashboard

### API Gateway (`api-gateway/`)
- Routes requests to appropriate microservices
- Implements load balancing with Eureka
- CORS support for web clients
- Actuator endpoints for monitoring

### Eureka Server (`eureka-server/`)
- Service registry with web dashboard
- Health monitoring of registered services
- Self-preservation disabled for development

## Development Patterns

### Adding New Services
1. Include in root `settings.gradle`
2. Apply common plugins and dependencies from root `build.gradle`
3. Register with Eureka using `@EnableEurekaClient`
4. Add route configuration in API Gateway
5. Implement health check endpoints

### Inter-Service Communication
- Use `@FeignClient` with service names for inter-service calls
- Propagate JWT tokens in request headers
- Handle service discovery through Eureka
- Implement circuit breakers for resilience

### Security Implementation
- JWT-based authentication with configurable expiration
- Role-based authorization using Spring Security
- Public endpoints configured in SecurityConfig
- Data isolation per user/role

### Testing Strategy
- Use separate H2 database for tests
- Mock external services with `@MockBean`
- Integration tests with `@SpringBootTest`
- API testing with Postman collections provided