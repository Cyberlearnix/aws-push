# AWS Deployment Guide for CyberLMS Microservices

This guide provides instructions for deploying the CyberLMS microservices application to AWS using Docker containers.

## Prerequisites

1. **AWS Account** with appropriate permissions
2. **Docker** installed locally
3. **AWS CLI** configured
4. **Docker Hub** or **AWS ECR** account for container registry

## Architecture Overview

The application consists of the following services:
- **Frontend**: React application (Port 80)
- **API Gateway**: Spring Cloud Gateway (Port 8080)
- **Eureka Server**: Service Discovery (Port 8761)
- **Config Server**: Configuration Management (Port 8888)
- **User Service**: User management (Port 8081)
- **Student Service**: Student operations (Port 8082)
- **Instructor Service**: Instructor operations (Port 8083)
- **Order Service**: Order processing (Port 8084)
- **PostgreSQL**: Database (Port 5432)
- **Redis**: Cache (Port 6379)

## Deployment Options

### Option 1: AWS ECS with Fargate (Recommended)

#### Step 1: Build and Push Images

```bash
# Build all images
docker-compose -f docker-compose.prod.yml build

# Tag images for ECR (replace with your ECR repository URIs)
docker tag cyberlms-frontend:latest <account-id>.dkr.ecr.<region>.amazonaws.com/cyberlms-frontend:latest
docker tag cyberlms-api-gateway:latest <account-id>.dkr.ecr.<region>.amazonaws.com/cyberlms-api-gateway:latest
docker tag cyberlms-eureka-server:latest <account-id>.dkr.ecr.<region>.amazonaws.com/cyberlms-eureka-server:latest
docker tag cyberlms-config-server:latest <account-id>.dkr.ecr.<region>.amazonaws.com/cyberlms-config-server:latest
docker tag cyberlms-user-service:latest <account-id>.dkr.ecr.<region>.amazonaws.com/cyberlms-user-service:latest
docker tag cyberlms-student-service:latest <account-id>.dkr.ecr.<region>.amazonaws.com/cyberlms-student-service:latest
docker tag cyberlms-instructor-service:latest <account-id>.dkr.ecr.<region>.amazonaws.com/cyberlms-instructor-service:latest
docker tag cyberlms-order-service:latest <account-id>.dkr.ecr.<region>.amazonaws.com/cyberlms-order-service:latest

# Push to ECR
aws ecr get-login-password --region <region> | docker login --username AWS --password-stdin <account-id>.dkr.ecr.<region>.amazonaws.com
docker push <account-id>.dkr.ecr.<region>.amazonaws.com/cyberlms-frontend:latest
# ... repeat for all services
```

#### Step 2: Create ECS Cluster

```bash
# Create ECS cluster
aws ecs create-cluster --cluster-name cyberlms-cluster --capacity-providers FARGATE
```

#### Step 3: Create RDS PostgreSQL Instance

```bash
# Create RDS subnet group
aws rds create-db-subnet-group \
    --db-subnet-group-name cyberlms-subnet-group \
    --db-subnet-group-description "Subnet group for CyberLMS" \
    --subnet-ids subnet-12345678 subnet-87654321

# Create RDS instance
aws rds create-db-instance \
    --db-instance-identifier cyberlms-postgres \
    --db-instance-class db.t3.micro \
    --engine postgres \
    --engine-version 15.4 \
    --master-username cyberlearnix \
    --master-user-password cyberlearnix123 \
    --allocated-storage 20 \
    --db-name cyberlearnixdb \
    --db-subnet-group-name cyberlms-subnet-group \
    --vpc-security-group-ids sg-12345678
```

#### Step 4: Create ElastiCache Redis

```bash
# Create ElastiCache subnet group
aws elasticache create-cache-subnet-group \
    --cache-subnet-group-name cyberlms-redis-subnet-group \
    --cache-subnet-group-description "Redis subnet group for CyberLMS" \
    --subnet-ids subnet-12345678 subnet-87654321

# Create Redis cluster
aws elasticache create-cache-cluster \
    --cache-cluster-id cyberlms-redis \
    --cache-node-type cache.t3.micro \
    --engine redis \
    --num-cache-nodes 1 \
    --cache-subnet-group-name cyberlms-redis-subnet-group \
    --security-group-ids sg-12345678
```

### Option 2: AWS EC2 with Docker Compose

#### Step 1: Launch EC2 Instance

1. Launch an EC2 instance (t3.large or larger recommended)
2. Install Docker and Docker Compose
3. Configure security groups to allow traffic on required ports

#### Step 2: Deploy Application

```bash
# Clone repository
git clone <your-repo-url>
cd cyber-lms-microservices

# Update environment variables in docker-compose.prod.yml
# Replace database and redis hosts with actual AWS service endpoints

# Deploy
docker-compose -f docker-compose.prod.yml up -d
```

### Option 3: AWS EKS (Kubernetes)

For production environments requiring high availability and scalability, consider deploying to EKS using Kubernetes manifests.

## Environment Configuration

### Required Environment Variables

Update the following environment variables in your deployment:

```yaml
# Database Configuration
SPRING_DATASOURCE_URL: jdbc:postgresql://<rds-endpoint>:5432/cyberlearnixdb
SPRING_DATASOURCE_USERNAME: cyberlearnix
SPRING_DATASOURCE_PASSWORD: cyberlearnix123

# Redis Configuration
SPRING_DATA_REDIS_HOST: <elasticache-endpoint>
SPRING_DATA_REDIS_PORT: 6379

# Service Discovery
EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE: http://<eureka-service-url>:8761/eureka/
```

## Security Considerations

1. **Use AWS Secrets Manager** for sensitive configuration
2. **Configure VPC** with private subnets for backend services
3. **Set up Application Load Balancer** for frontend and API gateway
4. **Enable SSL/TLS** certificates using AWS Certificate Manager
5. **Configure security groups** to restrict access between services
6. **Use IAM roles** for service authentication

## Monitoring and Logging

1. **CloudWatch Logs**: Configure log drivers for container logging
2. **CloudWatch Metrics**: Monitor application performance
3. **AWS X-Ray**: Distributed tracing for microservices
4. **Health Checks**: Ensure all services have proper health check endpoints

## Scaling Configuration

### Auto Scaling for ECS

```bash
# Create auto scaling target
aws application-autoscaling register-scalable-target \
    --service-namespace ecs \
    --resource-id service/cyberlms-cluster/user-service \
    --scalable-dimension ecs:service:DesiredCount \
    --min-capacity 1 \
    --max-capacity 10

# Create scaling policy
aws application-autoscaling put-scaling-policy \
    --policy-name cyberlms-scaling-policy \
    --service-namespace ecs \
    --resource-id service/cyberlms-cluster/user-service \
    --scalable-dimension ecs:service:DesiredCount \
    --policy-type TargetTrackingScaling \
    --target-tracking-scaling-policy-configuration file://scaling-policy.json
```

## Cost Optimization

1. **Use Spot Instances** for non-critical workloads
2. **Right-size instances** based on actual usage
3. **Implement auto-scaling** to handle traffic variations
4. **Use Reserved Instances** for predictable workloads
5. **Monitor costs** using AWS Cost Explorer

## Troubleshooting

### Common Issues

1. **Service Discovery**: Ensure Eureka server is running and accessible
2. **Database Connection**: Verify RDS security groups and connection strings
3. **Memory Issues**: Monitor container memory usage and adjust limits
4. **Network Connectivity**: Check VPC configuration and security groups

### Useful Commands

```bash
# Check ECS service status
aws ecs describe-services --cluster cyberlms-cluster --services user-service

# View container logs
aws logs get-log-events --log-group-name /ecs/cyberlms --log-stream-name <stream-name>

# Check RDS connectivity
aws rds describe-db-instances --db-instance-identifier cyberlms-postgres
```

## Backup and Recovery

1. **RDS Automated Backups**: Enable automated backups with appropriate retention
2. **Application Data**: Implement backup strategies for application-specific data
3. **Container Images**: Maintain versioned container images in ECR
4. **Configuration**: Store configuration in version control

## Next Steps

1. Set up CI/CD pipeline using AWS CodePipeline
2. Implement infrastructure as code using AWS CloudFormation or Terraform
3. Configure monitoring and alerting
4. Set up disaster recovery procedures
5. Implement security scanning for container images
