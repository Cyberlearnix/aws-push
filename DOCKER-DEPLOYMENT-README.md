# Docker Deployment for CyberLMS Microservices

This document provides a quick reference for deploying the CyberLMS microservices using Docker containers.

## 📁 Project Structure

The following Docker files have been created for each service:

```
cyber-lms-microservices/
├── api-gateway/
│   ├── Dockerfile
│   └── .dockerignore
├── config-server/
│   ├── Dockerfile
│   └── .dockerignore
├── eureka-server/
│   ├── Dockerfile
│   └── .dockerignore
├── instructor-service/
│   ├── Dockerfile
│   └── .dockerignore
├── order-service/
│   ├── Dockerfile
│   └── .dockerignore
├── student_service/
│   ├── Dockerfile
│   └── .dockerignore
├── user-service/
│   ├── Dockerfile
│   └── .dockerignore
├── userservice-frontend/
│   ├── Dockerfile
│   ├── nginx.conf
│   └── .dockerignore
├── docker-compose.prod.yml
├── build-docker-images.ps1
├── deploy-to-aws.ps1
└── AWS-DEPLOYMENT-GUIDE.md
```

## 🚀 Quick Start

### Local Development

1. **Build all images:**
   ```powershell
   .\build-docker-images.ps1
   ```

2. **Run the complete application:**
   ```bash
   docker-compose -f docker-compose.prod.yml up -d
   ```

3. **Access the application:**
   - Frontend: http://localhost
   - API Gateway: http://localhost:8080
   - Eureka Server: http://localhost:8761

### AWS Deployment

1. **Configure AWS CLI:**
   ```bash
   aws configure
   ```

2. **Deploy to AWS:**
   ```powershell
   .\deploy-to-aws.ps1 -AWSRegion us-east-1 -ECRRepository <account-id>.dkr.ecr.us-east-1.amazonaws.com
   ```

3. **Follow the AWS Deployment Guide:**
   See `AWS-DEPLOYMENT-GUIDE.md` for detailed instructions.

## 🏗️ Service Architecture

| Service | Port | Technology | Database |
|---------|------|------------|----------|
| Frontend | 80 | React + Nginx | - |
| API Gateway | 8080 | Spring Cloud Gateway | - |
| Eureka Server | 8761 | Spring Cloud Netflix | - |
| Config Server | 8888 | Spring Cloud Config | - |
| User Service | 8081 | Spring Boot + JPA | PostgreSQL |
| Student Service | 8082 | Spring Boot + JPA | PostgreSQL |
| Instructor Service | 8083 | Spring Boot | - |
| Order Service | 8084 | Spring Boot + JPA | PostgreSQL |
| PostgreSQL | 5432 | Database | - |
| Redis | 6379 | Cache | - |

## 🔧 Configuration

### Database Configuration
Based on the existing configuration, the services use:
- **Database**: cyberlearnixdb
- **Username**: cyberlearnix
- **Password**: cyberlearnix123
- **Host**: localhost:5432 (or postgres:5432 in Docker)

### Environment Variables
Key environment variables for Docker deployment:
```yaml
SPRING_PROFILES_ACTIVE: docker
SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/cyberlearnixdb
SPRING_DATASOURCE_USERNAME: cyberlearnix
SPRING_DATASOURCE_PASSWORD: cyberlearnix123
SPRING_DATA_REDIS_HOST: redis
EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE: http://eureka-server:8761/eureka/
```

## 🐳 Docker Images

### Java Services
- **Base Image**: OpenJDK 17/21 (depending on service)
- **Build**: Multi-stage build with Gradle
- **Health Checks**: Actuator endpoints
- **Security**: Non-root user, minimal base image

### Frontend Service
- **Base Image**: Node.js 18 (build) + Nginx Alpine (runtime)
- **Build**: Multi-stage build with Vite
- **Features**: Gzip compression, security headers, API proxy

## 📊 Health Monitoring

All services include health checks:
- **Java Services**: `/actuator/health` endpoint
- **Frontend**: HTTP GET to root path
- **Database**: PostgreSQL connection check
- **Redis**: Redis ping command

## 🔒 Security Features

1. **Container Security**:
   - Non-root user execution
   - Minimal base images
   - Security headers in Nginx

2. **Network Security**:
   - Internal Docker network
   - Port exposure only where needed
   - Service-to-service communication

3. **Configuration Security**:
   - Environment variable injection
   - No hardcoded secrets in images

## 📈 Scaling Considerations

### Horizontal Scaling
Services that can be scaled horizontally:
- ✅ User Service
- ✅ Student Service  
- ✅ Instructor Service
- ✅ Order Service
- ✅ Frontend (multiple replicas behind load balancer)

### Vertical Scaling
Services that may need vertical scaling:
- 🔄 API Gateway (high traffic)
- 🔄 Eureka Server (service registry)
- 🔄 Database (PostgreSQL)

## 🛠️ Troubleshooting

### Common Issues

1. **Service Discovery Problems**:
   ```bash
   docker logs cyberlms-eureka-server
   ```

2. **Database Connection Issues**:
   ```bash
   docker logs cyberlms-postgres
   docker exec -it cyberlms-postgres psql -U cyberlearnix -d cyberlearnixdb
   ```

3. **Build Failures**:
   - Check Gradle wrapper permissions
   - Verify Java version compatibility
   - Review .dockerignore files

### Useful Commands

```bash
# View all containers
docker-compose -f docker-compose.prod.yml ps

# View logs for specific service
docker-compose -f docker-compose.prod.yml logs user-service

# Restart specific service
docker-compose -f docker-compose.prod.yml restart user-service

# Scale a service
docker-compose -f docker-compose.prod.yml up -d --scale user-service=3

# Clean up
docker-compose -f docker-compose.prod.yml down -v
```

## 📚 Additional Resources

- **AWS Deployment**: See `AWS-DEPLOYMENT-GUIDE.md`
- **Build Scripts**: Use `build-docker-images.ps1`
- **AWS Scripts**: Use `deploy-to-aws.ps1`
- **Original Setup**: See `MICROSERVICES-STARTUP-GUIDE.md`

## 🎯 Next Steps

1. **Production Deployment**:
   - Set up AWS infrastructure
   - Configure CI/CD pipeline
   - Implement monitoring and logging

2. **Performance Optimization**:
   - Configure JVM parameters
   - Implement caching strategies
   - Set up load balancing

3. **Security Hardening**:
   - Use AWS Secrets Manager
   - Implement SSL/TLS
   - Set up VPC and security groups

---

For detailed AWS deployment instructions, refer to `AWS-DEPLOYMENT-GUIDE.md`.
