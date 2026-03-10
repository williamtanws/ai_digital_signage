# ==============================================
# AI Digital Signage - Raspberry Pi Stop Script
# ==============================================
# PowerShell script for Windows development/testing

$ErrorActionPreference = "Stop"

Write-Host "==================================" -ForegroundColor Cyan
Write-Host "AI Digital Signage - Shutdown" -ForegroundColor Cyan
Write-Host "==================================" -ForegroundColor Cyan
Write-Host ""

# Check if Docker is running
try {
    docker version | Out-Null
} catch {
    Write-Host "Error: Docker is not running" -ForegroundColor Red
    Write-Host "Please start Docker Desktop" -ForegroundColor Yellow
    exit 1
}

# Navigate to docker/raspi directory
$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptPath

# Check if containers are running
$runningContainers = docker compose ps --services --filter "status=running" 2>$null

if (-not $runningContainers) {
    Write-Host "No running containers found" -ForegroundColor Yellow
    Write-Host ""
    docker compose ps
    exit 0
}

Write-Host "Stopping services..." -ForegroundColor Yellow
Write-Host ""

# Show current status
docker compose ps

Write-Host ""
Write-Host "Stopping containers gracefully..." -ForegroundColor Yellow

# Stop containers (gives them time to shutdown gracefully)
docker compose stop

# Optional: Remove containers (uncomment if you want to remove containers on stop)
# Write-Host ""
# Write-Host "Removing stopped containers..." -ForegroundColor Yellow
# docker compose down

Write-Host ""
Write-Host "==================================" -ForegroundColor Green
Write-Host "✓ Services Stopped" -ForegroundColor Green
Write-Host "==================================" -ForegroundColor Green
Write-Host ""
Write-Host "Container status:" -ForegroundColor Cyan
docker compose ps
Write-Host ""
Write-Host "To start again, run: .\start.ps1" -ForegroundColor Cyan
Write-Host "To remove containers and volumes: docker compose down -v" -ForegroundColor Cyan
Write-Host ""
