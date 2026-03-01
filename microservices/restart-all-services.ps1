# Restart All Services and Run E2E Testing
# Complete restart workflow for AI Digital Signage system

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "AI Digital Signage - Complete Service Restart" -ForegroundColor Cyan  
Write-Host "========================================`n" -ForegroundColor Cyan

# Step 1: Stop existing services
Write-Host "[Step 1/7] Stopping existing services..." -ForegroundColor Yellow

# Stop Java processes
$javaProcesses = Get-Process -Name java -ErrorAction SilentlyContinue | Where-Object {
    $cmdLine = (Get-CimInstance Win32_Process -Filter "ProcessId = $($_.Id)" -ErrorAction SilentlyContinue).CommandLine
    $cmdLine -like "*digital-signage*" -or $cmdLine -like "*analytics-etl*" -or $cmdLine -like "*spring-boot*"
}

if ($javaProcesses) {
    foreach ($process in $javaProcesses) {
        Write-Host "  Stopping Java process ID: $($process.Id)" -ForegroundColor Gray
        Stop-Process -Id $process.Id -Force -ErrorAction SilentlyContinue
    }
    Write-Host "  [OK] Java services stopped" -ForegroundColor Green
}
else {
    Write-Host "  No Java services running" -ForegroundColor Gray
}

# Stop Node processes on port 5174
$nodeProcesses = Get-NetTCPConnection -LocalPort 5174 -ErrorAction SilentlyContinue
if ($nodeProcesses) {
    foreach ($conn in $nodeProcesses) {
        Stop-Process -Id $conn.OwningProcess -Force -ErrorAction SilentlyContinue
    }
    Write-Host "  [OK] Dashboard stopped" -ForegroundColor Green
}
else {
    Write-Host "  No dashboard running" -ForegroundColor Gray
}

Start-Sleep -Seconds 2

# Step 2: Verify TDengine
Write-Host "`n[Step 2/7] Verifying TDengine..." -ForegroundColor Yellow
$tdengine = docker ps --filter "name=tdengine-tsdb" --format "{{.Names}}"
if ($tdengine) {
    Write-Host "  [OK] TDengine is running" -ForegroundColor Green
}
else {
    Write-Host "  [WARNING] TDengine not running!" -ForegroundColor Red
    Write-Host "  Starting TDengine..." -ForegroundColor Yellow
    docker start tdengine-tsdb
    Start-Sleep -Seconds 5
}

# Step 3: Set Java environment
Write-Host "`n[Step 3/7] Configuring Java environment..." -ForegroundColor Yellow
$env:JAVA_HOME = "C:\Program Files\SapMachine\JDK\21"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

$javaVersion = & java -version 2>&1 | Select-String "version" | ForEach-Object { $_ -replace '.*"(.*)".*', '$1' }
Write-Host "  Java version: $javaVersion" -ForegroundColor Green

# Step 4: Start Backend Service
Write-Host "`n[Step 4/7] Starting Backend Service (digital-signage-service)..." -ForegroundColor Yellow
Write-Host "  Port: 8080" -ForegroundColor Gray

$backendPath = "d:\Development\ai_digital_signage\microservices\digital-signage-service"
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$backendPath'; `$env:JAVA_HOME = 'C:\Program Files\SapMachine\JDK\21'; `$env:PATH = `"`$env:JAVA_HOME\bin;`$env:PATH`"; mvn spring-boot:run" -WindowStyle Normal

Write-Host "  [OK] Backend service starting..." -ForegroundColor Green
Write-Host "  Waiting 30 seconds for initialization..." -ForegroundColor Gray

Start-Sleep -Seconds 30

# Verify backend is up
$backendReady = $false
$maxAttempts = 6
for ($i = 1; $i -le $maxAttempts; $i++) {
    try {
        $health = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -TimeoutSec 3 -ErrorAction Stop
        if ($health.status -eq "UP") {
            Write-Host "  [OK] Backend service is UP and healthy!" -ForegroundColor Green
            $backendReady = $true
            break
        }
    }
    catch {
        Write-Host "  Attempt $i/$maxAttempts - Not ready yet, waiting..." -ForegroundColor Gray
        Start-Sleep -Seconds 5
    }
}

if (-not $backendReady) {
    Write-Host "  [WARNING] Backend may still be starting (this can take up to 60 seconds)" -ForegroundColor Yellow
}

# Step 5: Run ETL Service
Write-Host "`n[Step 5/7] Running ETL Service (analytics-etl-service)..." -ForegroundColor Yellow
Write-Host "  This processes data from TDengine to SQLite" -ForegroundColor Gray

$etlPath = "d:\Development\ai_digital_signage\microservices\analytics-etl-service"
cd $etlPath

try {
    mvn spring-boot:run
    Write-Host "  [OK] ETL processing completed" -ForegroundColor Green
} catch {
    Write-Host "  [WARNING] ETL service encountered an issue" -ForegroundColor Yellow
}

cd ..

Start-Sleep -Seconds 3

# Step 6: Start Dashboard
Write-Host "`n[Step 6/7] Starting Dashboard (digital-signage-dashboard)..." -ForegroundColor Yellow
Write-Host "  Port: 5174" -ForegroundColor Gray

$dashboardPath = "d:\Development\ai_digital_signage\microservices\digital-signage-dashboard"

# Check if node_modules exists
if (-not (Test-Path "$dashboardPath\node_modules")) {
    Write-Host "  Installing dependencies..." -ForegroundColor Gray
    cd $dashboardPath
    npm install
    cd ..
}

Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$dashboardPath'; npm run dev" -WindowStyle Normal

Write-Host "  [OK] Dashboard starting..." -ForegroundColor Green
Write-Host "  Waiting 10 seconds for initialization..." -ForegroundColor Gray
Start-Sleep -Seconds 10

# Step 7: Run E2E Tests
Write-Host "`n[Step 7/7] Running End-to-End Tests..." -ForegroundColor Yellow
Write-Host "========================================`n" -ForegroundColor Cyan

Start-Sleep -Seconds 2

& "d:\Development\ai_digital_signage\microservices\test-flow.ps1"

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "Services Status Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

Write-Host "`nService URLs:" -ForegroundColor White
Write-Host "  Dashboard:      http://localhost:5174" -ForegroundColor Cyan
Write-Host "  Backend API:    http://localhost:8080/api/dashboard/overview" -ForegroundColor Cyan
Write-Host "  Swagger UI:     http://localhost:8080/swagger-ui.html" -ForegroundColor Cyan
Write-Host "  SQLite Browser: http://localhost:3000" -ForegroundColor Cyan
Write-Host "  TDengine UI:    http://localhost:6060" -ForegroundColor Cyan

Write-Host "`nAll services restarted and tested!" -ForegroundColor Green
Write-Host ""
