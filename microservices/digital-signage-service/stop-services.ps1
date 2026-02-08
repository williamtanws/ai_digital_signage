# Stop Digital Signage Service and SQLite Browser
# This script stops both the Spring Boot service and SQLite browser container

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Stopping Digital Signage Services" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Stop Spring Boot Service (if running)
Write-Host "[1/2] Stopping Spring Boot services..." -ForegroundColor Yellow

$springBootProcesses = Get-Process -Name java -ErrorAction SilentlyContinue | Where-Object {
    $_.CommandLine -like "*digital-signage-service*"
}

if ($springBootProcesses) {
    foreach ($process in $springBootProcesses) {
        Write-Host "      Stopping process ID: $($process.Id)" -ForegroundColor Green
        Stop-Process -Id $process.Id -Force
    }
    Write-Host "      ✓ Spring Boot service stopped" -ForegroundColor Green
} else {
    Write-Host "      No running Spring Boot service found" -ForegroundColor Yellow
}

Write-Host ""

# Stop SQLite Browser Docker Container
Write-Host "[2/2] Stopping SQLite Browser container..." -ForegroundColor Yellow

try {
    # Check if container exists
    $existingContainer = docker ps -a --filter "name=sqlite-browser" --format "{{.Names}}" 2>$null
    
    if ($existingContainer -eq "sqlite-browser") {
        docker stop sqlite-browser 2>&1 | Out-Null
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "      ✓ SQLite browser container stopped" -ForegroundColor Green
            
            # Optionally remove the container (uncomment if desired)
            # docker rm sqlite-browser 2>&1 | Out-Null
            # Write-Host "      ✓ SQLite browser container removed" -ForegroundColor Green
        } else {
            Write-Host "      ⚠ Warning: Could not stop SQLite browser container" -ForegroundColor Red
        }
    } else {
        Write-Host "      No SQLite browser container found" -ForegroundColor Yellow
    }
}
catch {
    Write-Host "      ⚠ Docker not available or container not running" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Services stopped" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
