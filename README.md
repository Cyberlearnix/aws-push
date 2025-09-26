# CyberLearnX LMS Microservices

This is a microservices-based Learning Management System (LMS) built with Spring Boot, Spring Cloud Gateway, and Eureka Service Discovery.

## Project Structure

- `api-gateway`: API Gateway service that routes requests to appropriate microservices
- `user-service`: User management service (first microservice)
- `eureka-server`: Service registry and discovery server
- `config-server`: (To be implemented) Centralized configuration server

## Prerequisites

- Java 21
- Gradle 8.0+
- H2 Database (embedded)
- (Optional) PostgreSQL for production

## Getting Started

1. **Start Eureka Server**
   ```bash
   cd eureka-server
   ./gradlew bootRun
   ```
   Access Eureka Dashboard at: http://localhost:8761

2. **Start API Gateway**
   ```bash
   cd api-gateway
   ./gradlew bootRun
   ```

3. **Start User Service**
   ```bash
   cd user-service
   ./gradlew bootRun
   ```

## Services Overview

### Eureka Server
- Port: 8761
- URL: http://localhost:8761

### API Gateway
- Port: 8080
- Routes:
  - User Service: /api/users/**

### User Service
- Port: 8081
- H2 Console: http://localhost:8081/h2-console
- JDBC URL: jdbc:h2:mem:userdb
- Username: sa
- Password: password
- API Documentation: http://localhost:8081/swagger-ui.html

## API Endpoints

### User Service
- GET /api/users - Get all users
- GET /api/users/{id} - Get user by ID
- POST /api/users - Create a new user
- PUT /api/users/{id} - Update a user
- DELETE /api/users/{id} - Delete a user

## Development

### Building the project
```bash
./gradlew build
```

### Running tests
```bash
./gradlew test
```

## Next Steps

1. Implement authentication and authorization with Spring Security and JWT
2. Add more microservices (courses, enrollments, etc.)
3. Implement centralized configuration with Spring Cloud Config
4. Add API documentation with OpenAPI/Swagger
5. Add monitoring with Spring Boot Actuator and Micrometer
6. Implement distributed tracing with Spring Cloud Sleuth and Zipkin

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
