# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Project Overview

This is a Spring Boot microservice called `user-service` that handles user authentication, registration, and management for the CyberLearnix LMS platform. It's part of a larger microservices architecture using Eureka service discovery.

## Common Development Commands

### Build & Run
```bash
# Build the project
./gradlew build

# Run tests
./gradlew test

# Run the application
./gradlew bootRun

# Build Docker image
docker build -t user-service .

# Run with Docker
docker run -p 8080:8080 user-service
```

### Development Testing
```bash
# Quick API test (Python script)
python test_apis.py

# Admin functionality test (PowerShell)
./simple_test.ps1

# Run with specific profile
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### Database Operations
```bash
# Apply Flyway migrations manually
./gradlew flywayMigrate

# Get migration info
./gradlew flywayInfo

# Clean database (dev only)
./gradlew flywayClean
```

## Architecture & Structure

### Core Architecture
- **Framework**: Spring Boot 3.2.4 with Spring Security
- **Database**: PostgreSQL with JPA/Hibernate and Flyway migrations
- **Cache**: Redis for OTP and session management
- **Service Discovery**: Eureka Client
- **Authentication**: JWT tokens with refresh token support
- **Email**: SMTP-based OTP delivery with HTML templates

### Key Components

#### Authentication Flow
- **OTP-based registration/login**: Users receive OTP via email for verification
- **Password-based login**: Traditional email/password authentication
- **JWT tokens**: Access tokens (1 hour) + refresh tokens for session management
- **Role-based security**: ADMIN role for management endpoints

#### Service Layer
- `EmailAuthService`: Handles OTP generation, validation, and email-based auth
- `AuthService`: Traditional password authentication and token management
- `UserService`: User CRUD operations and profile management
- `EmailService`: Email sending with professional HTML templates
- `OtpCacheService`: Redis-based OTP caching with expiration

#### Security Configuration
- Public endpoints: OTP auth, password login, forgot password
- User endpoints: Profile updates, account deletion (require user token)
- Admin endpoints: User management (require ADMIN role)
- JWT filter chain with proper exception handling

### Database Schema
- **UserEntity**: Core user data (id, email, fullName, phone, role, password, isActive)
- **OtpSession**: Temporary OTP session management (cached in Redis)
- Uses Flyway for schema migrations (see `src/main/resources/db/migration/`)

### Inter-service Communication
- **Feign clients**: Communicates with `instructor-service` for validation
- **Eureka registration**: Registers as `user-service` on port 8080
- **Health checks**: Actuator endpoints for monitoring

## Configuration

### Environment Variables
The service supports environment variable overrides:
- `SPRING_DATASOURCE_URL`: Database connection string
- `SPRING_DATASOURCE_USERNAME`: Database username
- `SPRING_DATASOURCE_PASSWORD`: Database password
- `EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE`: Eureka server URL

### Profiles
- Default: Uses `application.properties` and `application.yml`
- Test: Uses `application-test.yml` with in-memory configurations

### Key Configuration Files
- `application.properties`: Main configuration (legacy format)
- `application.yml`: YAML configuration (preferred format)
- `SecurityConfig.java`: Spring Security setup
- `WebConfig.java`: CORS and web configuration

## API Structure

### Authentication Endpoints (`/api/email-auth`, `/api/auth`)
- OTP-based registration and login flow
- Password-based authentication
- Token refresh and logout
- All well-documented in `API_ENDPOINTS_DOCUMENTATION.md`

### User Endpoints (`/api/users`)
- Profile management (update, delete, photo upload)
- Password reset functionality
- Public user profile access

### Admin Endpoints (`/api/admin`)
- Complete user management CRUD operations
- User activation/deactivation
- Bulk user operations
- Requires ADMIN role authentication

## Testing Strategy

### Unit Tests
- Located in `src/test/java/com/userservice/userservice/`
- Run with: `./gradlew test`
- Uses JUnit 5 with Spring Boot Test

### Integration Testing
- Python script (`test_apis.py`) for basic API validation
- PowerShell script (`simple_test.ps1`) for admin workflow testing
- Postman collection available: `UserService_API_Collection.postman_collection.json`

### Development Testing Flow
1. Start the application (`./gradlew bootRun`)
2. Ensure PostgreSQL and Redis are running
3. Run `python test_apis.py` for basic health checks
4. Use Postman collection for comprehensive API testing
5. Use `simple_test.ps1` for admin authentication testing

## Dependencies & Infrastructure

### Required Services
- **PostgreSQL**: Primary database (port 5432)
- **Redis**: OTP and session caching (port 6379)
- **SMTP Server**: Email delivery (configured for Gmail)
- **Eureka Server**: Service discovery (port 8761)

### Key Dependencies
- Spring Boot starters: web, data-jpa, security, validation, mail, actuator
- Spring Cloud: eureka-client, openfeign, loadbalancer
- Database: PostgreSQL driver, Flyway
- Security: JJWT for JWT handling
- Utility: Lombok for code generation

## Email Templates

Professional HTML and plain text email templates are located in `src/main/resources/templates/`:
- `otp-email-template.html`: OTP verification emails
- `password-reset-email-template.html`: Password reset emails
- Templates support variable substitution (`{{OTP_CODE}}`)
- Fallback to plain text for compatibility

## Docker Support

- `Dockerfile` provided for containerization
- Multi-stage build with OpenJDK 17
- Health checks via `/actuator/health`
- Exposes port 8080
- Includes curl for health check functionality

## Port Configuration

- **Application**: 8080 (configurable via `server.port`)
- **Database**: 5432 (PostgreSQL)
- **Redis**: 6379
- **Eureka**: 8761
- Note: All services now run on port 8080
