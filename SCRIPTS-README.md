# CyberLMS Microservices - Management Scripts

This directory contains several scripts to help you manage the CyberLMS microservices architecture efficiently.

## 📋 Available Scripts

### 🚀 Full System Management

#### `pull-and-restart-all.ps1` (PowerShell)
Complete system restart with git pull and Docker rebuild.

```powershell
# Basic usage - pull latest code and restart all services
.\pull-and-restart-all.ps1

# Skip git pull (useful for testing local changes)
.\pull-and-restart-all.ps1 -SkipGitPull

# Skip Docker build (faster restart)
.\pull-and-restart-all.ps1 -SkipBuild

# Show verbose output
.\pull-and-restart-all.ps1 -Verbose

# Combine options
.\pull-and-restart-all.ps1 -SkipGitPull -Verbose
```

#### `pull-and-restart-all.sh` (Bash)
Same functionality as PowerShell version for Linux/Mac users.

```bash
# Basic usage
./pull-and-restart-all.sh

# Skip git pull
./pull-and-restart-all.sh --skip-git-pull

# Skip Docker build
./pull-and-restart-all.sh --skip-build

# Show verbose output
./pull-and-restart-all.sh --verbose

# Show help
./pull-and-restart-all.sh --help
```

### 🔧 Individual Service Management

#### `restart-service.ps1` (PowerShell)
Restart specific services without affecting others.

```powershell
# Restart single service
.\restart-service.ps1 -Services user-service

# Restart multiple services
.\restart-service.ps1 -Services user-service,student-service,api-gateway

# Restart with rebuild
.\restart-service.ps1 -Services frontend -Build

# Restart with verbose output
.\restart-service.ps1 -Services eureka-server -Verbose

# Combine options
.\restart-service.ps1 -Services user-service,api-gateway -Build -Verbose
```

#### `restart-service.sh` (Bash)
Same functionality as PowerShell version for Linux/Mac users.

```bash
# Restart single service
./restart-service.sh user-service

# Restart multiple services
./restart-service.sh user-service student-service api-gateway

# Restart with rebuild
./restart-service.sh --build frontend

# Restart with verbose output
./restart-service.sh --verbose eureka-server

# Show help
./restart-service.sh --help
```

### 📊 Monitoring and Utilities

#### `service-manager.ps1` (PowerShell)
Quick reference and monitoring utilities.

```powershell
# Show help and available commands
.\service-manager.ps1

# Show all service status
.\service-manager.ps1 status

# Show health check status
.\service-manager.ps1 health

# Show all service URLs
.\service-manager.ps1 urls

# Follow logs for all services
.\service-manager.ps1 logs
```

## 🏗️ Service Architecture

### Core Services
- **Frontend** (Port 80) - React application
- **API Gateway** (Port 8080) - Routes requests to microservices
- **User Service** (Port 8081) - User management
- **Student Service** (Port 8082) - Student-specific functionality
- **Instructor Service** (Port 8083) - Instructor-specific functionality
- **Order Service** (Port 8084) - Order processing

### Infrastructure Services
- **Config Server** (Port 8888) - Centralized configuration
- **Eureka Server** (Port 8761) - Service discovery and registration
- **PostgreSQL** (Port 5432) - Primary database
- **Redis** (Port 6379) - Caching layer

## 🌐 Service URLs

### User Interfaces
- **Main Application**: http://localhost
- **Eureka Dashboard**: http://localhost:8761

### API Endpoints
- **API Gateway**: http://localhost:8080
- **User Service**: http://localhost:8081
- **Student Service**: http://localhost:8082
- **Instructor Service**: http://localhost:8083
- **Order Service**: http://localhost:8084
- **Config Server**: http://localhost:8888

### Health Checks
All services expose health endpoints at `/actuator/health`:
- Example: http://localhost:8081/actuator/health

## 🔄 Common Workflows

### 1. Daily Development Workflow
```powershell
# Pull latest changes and restart everything
.\pull-and-restart-all.ps1
```

### 2. Testing Local Changes
```powershell
# Restart without pulling (keeps your local changes)
.\pull-and-restart-all.ps1 -SkipGitPull
```

### 3. Quick Service Restart
```powershell
# Restart just the service you're working on
.\restart-service.ps1 -Services user-service -Build
```

### 4. Debugging Issues
```powershell
# Check service health
.\service-manager.ps1 health

# View logs
.\service-manager.ps1 logs

# Check individual service status
docker-compose -f docker-compose.prod.yml ps user-service
```

### 5. Performance Testing
```powershell
# Restart without rebuild for faster iteration
.\pull-and-restart-all.ps1 -SkipBuild
```

## 🛠️ Troubleshooting

### Common Issues

1. **Docker not running**
   - Start Docker Desktop
   - Wait for it to fully initialize

2. **Port conflicts**
   - Check if other applications are using the same ports
   - Stop conflicting services or change ports in docker-compose.prod.yml

3. **Services not healthy**
   - Wait longer (some services take time to start)
   - Check logs: `docker-compose -f docker-compose.prod.yml logs [service-name]`

4. **Git pull fails**
   - Check your git configuration
   - Ensure you have proper access to the repository
   - Use `-SkipGitPull` flag to bypass

### Manual Commands

If scripts fail, you can use these manual Docker commands:

```bash
# Stop all services
docker-compose -f docker-compose.prod.yml down

# Start all services
docker-compose -f docker-compose.prod.yml up -d

# Rebuild and start
docker-compose -f docker-compose.prod.yml up -d --build

# View logs
docker-compose -f docker-compose.prod.yml logs -f

# Check status
docker-compose -f docker-compose.prod.yml ps
```

## 📝 Notes

- Scripts automatically wait for services to become healthy
- Use `-Verbose` flag to see detailed output during operations
- All scripts include colored output for better readability
- PowerShell scripts work on Windows, Linux, and Mac (with PowerShell Core)
- Bash scripts work on Linux and Mac (and Windows with WSL/Git Bash)

## 🔒 Security Notes

- Scripts use `docker-compose.prod.yml` for production-like environment
- Database credentials are configured in the compose file
- Ensure proper network security when deploying to production
- Consider using Docker secrets for sensitive data in production

## 📞 Support

If you encounter issues with these scripts:
1. Check the troubleshooting section above
2. Verify Docker is running and accessible
3. Ensure all required files (docker-compose.prod.yml) exist
4. Check system requirements and dependencies
