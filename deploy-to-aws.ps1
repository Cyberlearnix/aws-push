# PowerShell script to deploy CyberLMS to AWS

param(
    [Parameter(Mandatory=$true)]
    [string]$AWSRegion,
    
    [Parameter(Mandatory=$true)]
    [string]$ECRRepository,
    
    [Parameter(Mandatory=$false)]
    [string]$ECSCluster = "cyberlms-cluster",
    
    [Parameter(Mandatory=$false)]
    [string]$Environment = "production"
)

Write-Host "Deploying CyberLMS to AWS..." -ForegroundColor Green
Write-Host "Region: $AWSRegion" -ForegroundColor Cyan
Write-Host "ECR Repository: $ECRRepository" -ForegroundColor Cyan
Write-Host "ECS Cluster: $ECSCluster" -ForegroundColor Cyan

# Set error action preference
$ErrorActionPreference = "Stop"

# Services to deploy
$services = @(
    "eureka-server",
    "config-server", 
    "api-gateway",
    "user-service",
    "student-service",
    "instructor-service",
    "order-service",
    "frontend"
)

# Function to check if AWS CLI is configured
function Test-AWSConfiguration {
    try {
        aws sts get-caller-identity | Out-Null
        Write-Host "✓ AWS CLI is configured" -ForegroundColor Green
    }
    catch {
        Write-Host "✗ AWS CLI is not configured. Please run 'aws configure'" -ForegroundColor Red
        exit 1
    }
}

# Function to login to ECR
function Connect-ECR {
    Write-Host "Logging into ECR..." -ForegroundColor Yellow
    
    try {
        $loginCommand = aws ecr get-login-password --region $AWSRegion
        $loginCommand | docker login --username AWS --password-stdin "$ECRRepository"
        Write-Host "✓ Successfully logged into ECR" -ForegroundColor Green
    }
    catch {
        Write-Host "✗ Failed to login to ECR" -ForegroundColor Red
        Write-Host $_.Exception.Message -ForegroundColor Red
        exit 1
    }
}

# Function to create ECR repositories if they don't exist
function New-ECRRepositories {
    Write-Host "Creating ECR repositories..." -ForegroundColor Yellow
    
    foreach ($service in $services) {
        $repoName = "cyberlms-$service"
        
        try {
            aws ecr describe-repositories --repository-names $repoName --region $AWSRegion | Out-Null
            Write-Host "✓ Repository $repoName already exists" -ForegroundColor Green
        }
        catch {
            try {
                aws ecr create-repository --repository-name $repoName --region $AWSRegion | Out-Null
                Write-Host "✓ Created repository $repoName" -ForegroundColor Green
            }
            catch {
                Write-Host "✗ Failed to create repository $repoName" -ForegroundColor Red
                Write-Host $_.Exception.Message -ForegroundColor Red
            }
        }
    }
}

# Function to build and push Docker images
function Push-DockerImages {
    Write-Host "Building and pushing Docker images..." -ForegroundColor Yellow
    
    foreach ($service in $services) {
        $imageName = "cyberlms-$service"
        $remoteTag = "$ECRRepository/$imageName`:latest"
        
        Write-Host "Processing $service..." -ForegroundColor Cyan
        
        # Build image
        try {
            if ($service -eq "student-service") {
                docker build -t $imageName -f "student_service/Dockerfile" .
            }
            elseif ($service -eq "frontend") {
                docker build -t $imageName -f "userservice-frontend/Dockerfile" .
            }
            else {
                docker build -t $imageName -f "$service/Dockerfile" .
            }
            Write-Host "  ✓ Built $imageName" -ForegroundColor Green
        }
        catch {
            Write-Host "  ✗ Failed to build $imageName" -ForegroundColor Red
            continue
        }
        
        # Tag image
        try {
            docker tag $imageName $remoteTag
            Write-Host "  ✓ Tagged $imageName as $remoteTag" -ForegroundColor Green
        }
        catch {
            Write-Host "  ✗ Failed to tag $imageName" -ForegroundColor Red
            continue
        }
        
        # Push image
        try {
            docker push $remoteTag
            Write-Host "  ✓ Pushed $remoteTag" -ForegroundColor Green
        }
        catch {
            Write-Host "  ✗ Failed to push $remoteTag" -ForegroundColor Red
        }
    }
}

# Function to create ECS cluster
function New-ECSCluster {
    Write-Host "Creating ECS cluster..." -ForegroundColor Yellow
    
    try {
        aws ecs describe-clusters --clusters $ECSCluster --region $AWSRegion | Out-Null
        Write-Host "✓ ECS cluster $ECSCluster already exists" -ForegroundColor Green
    }
    catch {
        try {
            aws ecs create-cluster --cluster-name $ECSCluster --capacity-providers FARGATE --region $AWSRegion | Out-Null
            Write-Host "✓ Created ECS cluster $ECSCluster" -ForegroundColor Green
        }
        catch {
            Write-Host "✗ Failed to create ECS cluster $ECSCluster" -ForegroundColor Red
            Write-Host $_.Exception.Message -ForegroundColor Red
        }
    }
}

# Main deployment process
try {
    Write-Host "`n=== Step 1: Checking AWS Configuration ===" -ForegroundColor Magenta
    Test-AWSConfiguration
    
    Write-Host "`n=== Step 2: Connecting to ECR ===" -ForegroundColor Magenta
    Connect-ECR
    
    Write-Host "`n=== Step 3: Creating ECR Repositories ===" -ForegroundColor Magenta
    New-ECRRepositories
    
    Write-Host "`n=== Step 4: Building and Pushing Images ===" -ForegroundColor Magenta
    Push-DockerImages
    
    Write-Host "`n=== Step 5: Creating ECS Cluster ===" -ForegroundColor Magenta
    New-ECSCluster
    
    Write-Host "`n🎉 Deployment completed successfully!" -ForegroundColor Green
    
    Write-Host "`nNext steps:" -ForegroundColor Yellow
    Write-Host "1. Create RDS PostgreSQL instance" -ForegroundColor White
    Write-Host "2. Create ElastiCache Redis cluster" -ForegroundColor White
    Write-Host "3. Create ECS task definitions and services" -ForegroundColor White
    Write-Host "4. Configure Application Load Balancer" -ForegroundColor White
    Write-Host "5. Set up Route 53 for DNS" -ForegroundColor White
    
    Write-Host "`nRefer to AWS-DEPLOYMENT-GUIDE.md for detailed instructions." -ForegroundColor Cyan
}
catch {
    Write-Host "`n✗ Deployment failed!" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    exit 1
}
