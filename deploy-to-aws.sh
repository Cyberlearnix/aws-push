#!/bin/bash

# Bash script to deploy CyberLMS to AWS

# Default values
ECS_CLUSTER="cyberlms-cluster"
ENVIRONMENT="production"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
MAGENTA='\033[0;35m'
NC='\033[0m' # No Color

# Function to display usage
usage() {
    echo "Usage: $0 -r <aws-region> -e <ecr-repository> [-c <ecs-cluster>] [-env <environment>]"
    echo "  -r, --region       AWS Region (required)"
    echo "  -e, --ecr          ECR Repository URL (required)"
    echo "  -c, --cluster      ECS Cluster name (default: cyberlms-cluster)"
    echo "  -env, --environment Environment (default: production)"
    echo "  -h, --help         Show this help message"
    exit 1
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -r|--region)
            AWS_REGION="$2"
            shift 2
            ;;
        -e|--ecr)
            ECR_REPOSITORY="$2"
            shift 2
            ;;
        -c|--cluster)
            ECS_CLUSTER="$2"
            shift 2
            ;;
        -env|--environment)
            ENVIRONMENT="$2"
            shift 2
            ;;
        -h|--help)
            usage
            ;;
        *)
            echo "Unknown option $1"
            usage
            ;;
    esac
done

# Check required parameters
if [[ -z "$AWS_REGION" || -z "$ECR_REPOSITORY" ]]; then
    echo -e "${RED}Error: AWS Region and ECR Repository are required${NC}"
    usage
fi

echo -e "${GREEN}Deploying CyberLMS to AWS...${NC}"
echo -e "${CYAN}Region: $AWS_REGION${NC}"
echo -e "${CYAN}ECR Repository: $ECR_REPOSITORY${NC}"
echo -e "${CYAN}ECS Cluster: $ECS_CLUSTER${NC}"

# Set error handling
set -e

# Services to deploy
services=("eureka-server" "config-server" "api-gateway" "user-service" "student-service" "instructor-service" "order-service" "frontend")

# Function to check if AWS CLI is configured
check_aws_configuration() {
    echo -e "${YELLOW}Checking AWS configuration...${NC}"
    
    if aws sts get-caller-identity >/dev/null 2>&1; then
        echo -e "${GREEN}✓ AWS CLI is configured${NC}"
    else
        echo -e "${RED}✗ AWS CLI is not configured. Please run 'aws configure'${NC}"
        exit 1
    fi
}

# Function to login to ECR
connect_ecr() {
    echo -e "${YELLOW}Logging into ECR...${NC}"
    
    if aws ecr get-login-password --region "$AWS_REGION" | docker login --username AWS --password-stdin "$ECR_REPOSITORY"; then
        echo -e "${GREEN}✓ Successfully logged into ECR${NC}"
    else
        echo -e "${RED}✗ Failed to login to ECR${NC}"
        exit 1
    fi
}

# Function to create ECR repositories if they don't exist
create_ecr_repositories() {
    echo -e "${YELLOW}Creating ECR repositories...${NC}"
    
    for service in "${services[@]}"; do
        repo_name="cyberlms-$service"
        
        if aws ecr describe-repositories --repository-names "$repo_name" --region "$AWS_REGION" >/dev/null 2>&1; then
            echo -e "${GREEN}✓ Repository $repo_name already exists${NC}"
        else
            if aws ecr create-repository --repository-name "$repo_name" --region "$AWS_REGION" >/dev/null 2>&1; then
                echo -e "${GREEN}✓ Created repository $repo_name${NC}"
            else
                echo -e "${RED}✗ Failed to create repository $repo_name${NC}"
            fi
        fi
    done
}

# Function to build and push Docker images
push_docker_images() {
    echo -e "${YELLOW}Building and pushing Docker images...${NC}"
    
    for service in "${services[@]}"; do
        image_name="cyberlms-$service"
        remote_tag="$ECR_REPOSITORY/$image_name:latest"
        
        echo -e "${CYAN}Processing $service...${NC}"
        
        # Build image
        if [[ "$service" == "student-service" ]]; then
            dockerfile_path="student_service/Dockerfile"
        elif [[ "$service" == "frontend" ]]; then
            dockerfile_path="userservice-frontend/Dockerfile"
        else
            dockerfile_path="$service/Dockerfile"
        fi
        
        if docker build -t "$image_name" -f "$dockerfile_path" .; then
            echo -e "  ${GREEN}✓ Built $image_name${NC}"
        else
            echo -e "  ${RED}✗ Failed to build $image_name${NC}"
            continue
        fi
        
        # Tag image
        if docker tag "$image_name" "$remote_tag"; then
            echo -e "  ${GREEN}✓ Tagged $image_name as $remote_tag${NC}"
        else
            echo -e "  ${RED}✗ Failed to tag $image_name${NC}"
            continue
        fi
        
        # Push image
        if docker push "$remote_tag"; then
            echo -e "  ${GREEN}✓ Pushed $remote_tag${NC}"
        else
            echo -e "  ${RED}✗ Failed to push $remote_tag${NC}"
        fi
    done
}

# Function to create ECS cluster
create_ecs_cluster() {
    echo -e "${YELLOW}Creating ECS cluster...${NC}"
    
    if aws ecs describe-clusters --clusters "$ECS_CLUSTER" --region "$AWS_REGION" >/dev/null 2>&1; then
        echo -e "${GREEN}✓ ECS cluster $ECS_CLUSTER already exists${NC}"
    else
        if aws ecs create-cluster --cluster-name "$ECS_CLUSTER" --capacity-providers FARGATE --region "$AWS_REGION" >/dev/null 2>&1; then
            echo -e "${GREEN}✓ Created ECS cluster $ECS_CLUSTER${NC}"
        else
            echo -e "${RED}✗ Failed to create ECS cluster $ECS_CLUSTER${NC}"
        fi
    fi
}

# Main deployment process
main() {
    echo -e "\n${MAGENTA}=== Step 1: Checking AWS Configuration ===${NC}"
    check_aws_configuration
    
    echo -e "\n${MAGENTA}=== Step 2: Connecting to ECR ===${NC}"
    connect_ecr
    
    echo -e "\n${MAGENTA}=== Step 3: Creating ECR Repositories ===${NC}"
    create_ecr_repositories
    
    echo -e "\n${MAGENTA}=== Step 4: Building and Pushing Images ===${NC}"
    push_docker_images
    
    echo -e "\n${MAGENTA}=== Step 5: Creating ECS Cluster ===${NC}"
    create_ecs_cluster
    
    echo -e "\n${GREEN}🎉 Deployment completed successfully!${NC}"
    
    echo -e "\n${YELLOW}Next steps:${NC}"
    echo -e "${NC}1. Create RDS PostgreSQL instance${NC}"
    echo -e "${NC}2. Create ElastiCache Redis cluster${NC}"
    echo -e "${NC}3. Create ECS task definitions and services${NC}"
    echo -e "${NC}4. Configure Application Load Balancer${NC}"
    echo -e "${NC}5. Set up Route 53 for DNS${NC}"
    
    echo -e "\n${CYAN}Refer to AWS-DEPLOYMENT-GUIDE.md for detailed instructions.${NC}"
}

# Run main function
main
