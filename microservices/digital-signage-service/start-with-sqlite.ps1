# Start Digital Signage Service with SQLite Browser
# This script starts both the SQLite browser container and the Spring Boot service

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Digital Signage Service Startup Script" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Set Java 21 environment
Write-Host "[1/4] Setting Java 21 environment..." -ForegroundColor Yellow
$env:JAVA_HOME = "C:\Program Files\SapMachine\JDK\21"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

# Verify Java version
$javaVersion = & java -version 2>&1 | Select-String "version" | ForEach-Object { $_ -replace '.*"(.*)".*', '$1' }
Write-Host "      Java version: $javaVersion" -ForegroundColor Green
Write-Host ""

# Start SQLite Browser Docker Container
Write-Host "[2/4] Starting SQLite Browser container..." -ForegroundColor Yellow

# Check if docker is available
try {
    $dockerVersion = docker --version
    Write-Host "      Docker: $dockerVersion" -ForegroundColor Green
    
    # Check if container already exists
    $existingContainer = docker ps -a --filter "name=sqlite-browser" --format "{{.Names}}"
    
    if ($existingContainer -eq "sqlite-browser") {
        Write-Host "      Found existing sqlite-browser container..." -ForegroundColor Yellow
        
        # Check if it's running
        $isRunning = docker ps --filter "name=sqlite-browser" --format "{{.Names}}"
        
        if ($isRunning -eq "sqlite-browser") {
            Write-Host "      ✓ SQLite browser is already running" -ForegroundColor Green
        } else {
            Write-Host "      Starting existing container..." -ForegroundColor Yellow
            docker start sqlite-browser | Out-Null
            Write-Host "      ✓ SQLite browser started successfully" -ForegroundColor Green
        }
    } else {
        # Create and start new container
        Write-Host "      Creating SQLite browser container..." -ForegroundColor Yellow
        $workDir = Get-Location
        docker run -d --name sqlite-browser -p 3000:8080 -v "${workDir}/data:/data" -e SQLITE_DATABASE=/data/digital-signage.db coleifer/sqlite-web | Out-Null
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "      ✓ SQLite browser started successfully" -ForegroundColor Green
        } else {
            Write-Host "      ⚠ Warning: Could not start SQLite browser container" -ForegroundColor Red
            Write-Host "      The service will still start without the browser UI" -ForegroundColor Yellow
        }
    }
    
    Write-Host "      SQLite browser available at: http://localhost:3000" -ForegroundColor Cyan
}
catch {
    Write-Host "      ⚠ Warning: Docker not available or not running" -ForegroundColor Red
    Write-Host "      The service will still start without the SQLite browser" -ForegroundColor Yellow
}

Write-Host ""

# Create data directory if it doesn't exist
Write-Host "[3/4] Checking database directory..." -ForegroundColor Yellow
$dataDir = ".\data"
if (-not (Test-Path $dataDir)) {
    New-Item -ItemType Directory -Path $dataDir | Out-Null
    Write-Host "      ✓ Created data directory: $dataDir" -ForegroundColor Green
} else {
    Write-Host "      ✓ Data directory exists: $dataDir" -ForegroundColor Green
}

Write-Host ""

# Start Spring Boot Service
Write-Host "[4/4] Starting Spring Boot service..." -ForegroundColor Yellow
Write-Host "      Building and running digital-signage-service..." -ForegroundColor Green
Write-Host "      API will be available at: http://localhost:8080" -ForegroundColor Cyan
Write-Host "      SQLite browser at: http://localhost:3000" -ForegroundColor Cyan
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Press Ctrl+C to stop the service" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Start the Spring Boot application
mvn spring-boot:run
