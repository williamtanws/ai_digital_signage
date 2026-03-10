# ==============================================
# AI Digital Signage - Raspberry Pi Quick Start
# ==============================================
# PowerShell script for Windows development/testing

$ErrorActionPreference = "Stop"

Write-Host "==================================" -ForegroundColor Cyan
Write-Host "AI Digital Signage - Pi Deployment" -ForegroundColor Cyan
Write-Host "==================================" -ForegroundColor Cyan
Write-Host ""

# Check if Docker is running
try {
    docker version | Out-Null
    Write-Host "Docker is running" -ForegroundColor Green
} catch {
    Write-Host "Error: Docker is not running" -ForegroundColor Red
    Write-Host "Please start Docker Desktop" -ForegroundColor Yellow
    exit 1
}

# Check if Docker Compose is available
try {
    docker compose version | Out-Null
    Write-Host "Docker Compose is available" -ForegroundColor Green
} catch {
    Write-Host "Error: Docker Compose is not available" -ForegroundColor Red
    exit 1
}

Write-Host ""

# Navigate to docker/raspi directory
$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptPath

Write-Host "Current directory: $scriptPath" -ForegroundColor Cyan
Write-Host ""

# Check if JARs need to be built
$signageJar = "..\..\microservices\digital-signage-service\target\digital-signage-service-1.0.0-SNAPSHOT.jar"
$etlJar = "..\..\microservices\analytics-etl-service\target\analytics-etl-service-1.0.0-SNAPSHOT.jar"
$dashboardDist = "..\..\microservices\digital-signage-dashboard\dist"

$buildNeeded = $false

if (-not (Test-Path $signageJar)) {
    Write-Host "Digital Signage Service JAR not found" -ForegroundColor Yellow
    $buildNeeded = $true
}

if (-not (Test-Path $etlJar)) {
    Write-Host "Analytics ETL Service JAR not found" -ForegroundColor Yellow
    $buildNeeded = $true
}

if (-not (Test-Path $dashboardDist)) {
    Write-Host "Dashboard build not found" -ForegroundColor Yellow
    $buildNeeded = $true
}

if ($buildNeeded) {
    Write-Host ""
    Write-Host "Building will happen inside Docker containers" -ForegroundColor Yellow
    Write-Host "This will take 10-20 minutes on first run" -ForegroundColor Yellow
    Write-Host ""
    
    $response = Read-Host "Continue? (y/n)"
    if ($response -ne "y") {
        exit 1
    }
}

# Stop existing containers
Write-Host ""
Write-Host "Stopping existing containers..." -ForegroundColor Yellow
docker compose down

# Pull base images
Write-Host ""
Write-Host "Pulling base images..." -ForegroundColor Yellow
try {
    docker compose pull
} catch {
    Write-Host "Warning: Could not pull some images" -ForegroundColor Yellow
}

# Build images
Write-Host ""
Write-Host "Building application images..." -ForegroundColor Yellow
docker compose build

# Start services
Write-Host ""
Write-Host "Starting services..." -ForegroundColor Green
docker compose up -d

# Wait for services to be healthy
Write-Host ""
Write-Host "Waiting for services to start..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

# Check service status
docker compose ps

# Display useful information
Write-Host ""
Write-Host "==================================" -ForegroundColor Green
Write-Host "Deployment Complete!" -ForegroundColor Green
Write-Host "==================================" -ForegroundColor Green
Write-Host ""
Write-Host "Service URLs:" -ForegroundColor Cyan
Write-Host "  Dashboard:        http://localhost:5173"
Write-Host "  Digital Signage:  http://localhost:8080/api"
Write-Host "  Analytics ETL:    http://localhost:8081"
Write-Host "  TDengine:         http://localhost:6041"
Write-Host ""
Write-Host "Useful commands:" -ForegroundColor Cyan
Write-Host "  View logs:        docker compose logs -f"
Write-Host "  Stop services:    docker compose down"
Write-Host "  Restart:          docker compose restart"
Write-Host "  Status:           docker compose ps"
Write-Host ""
Write-Host "System is ready!" -ForegroundColor Green
