# Start All Services - AI Digital Signage
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Starting All Services" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# Set Java environment
$env:JAVA_HOME = "C:\Program Files\SapMachine\JDK\21"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

# Step 1: Verify TDengine
Write-Host "[1/4] Checking TDengine..." -ForegroundColor Yellow
$tdengine = docker ps --filter "name=tdengine-tsdb" --format "{{.Names}}"
if ($tdengine) {
    Write-Host "  TDengine is running" -ForegroundColor Green
} else {
    Write-Host "  Starting TDengine..." -ForegroundColor Yellow
    docker start tdengine-tsdb
    Start-Sleep -Seconds 5
}

# Step 2: Start Backend Service
Write-Host "`n[2/4] Starting Backend Service..." -ForegroundColor Yellow
Write-Host "  Port: 8080" -ForegroundColor Gray
Write-Host "  Opening in new window..." -ForegroundColor Gray

$backendScript = @"
cd d:\Development\ai_digital_signage\microservices\digital-signage-service
`$env:JAVA_HOME = 'C:\Program Files\SapMachine\JDK\21'
`$env:PATH = "`$env:JAVA_HOME\bin;`$env:PATH"
Write-Host 'Starting Backend Service...' -ForegroundColor Cyan
mvn spring-boot:run
"@

Start-Process powershell -ArgumentList "-NoExit", "-Command", $backendScript

Write-Host "  Backend starting (allow 30-45 seconds)..." -ForegroundColor Green
Write-Host "  Waiting for backend to initialize..." -ForegroundColor Gray
Start-Sleep -Seconds 40

# Step 3: Start Analytics ETL Service
Write-Host "`n[3/4] Starting Analytics ETL Service..." -ForegroundColor Yellow
Write-Host "  Port: 8081" -ForegroundColor Gray
Write-Host "  Opening in new window..." -ForegroundColor Gray

$etlScript = @"
cd d:\Development\ai_digital_signage\microservices\analytics-etl-service
`$env:JAVA_HOME = 'C:\Program Files\SapMachine\JDK\21'
`$env:PATH = "`$env:JAVA_HOME\bin;`$env:PATH"
Write-Host 'Starting Analytics ETL Service...' -ForegroundColor Cyan
Write-Host 'Processing data from TDengine to SQLite (runs every 5 minutes)' -ForegroundColor Gray
mvn spring-boot:run
"@

Start-Process powershell -ArgumentList "-NoExit", "-Command", $etlScript

Write-Host "  ETL service starting (continuous processing)..." -ForegroundColor Green

# Step 4: Start Dashboard
Write-Host "`n[4/4] Starting Dashboard..." -ForegroundColor Yellow
Write-Host "  Port: 5174" -ForegroundColor Gray
Write-Host "  Opening in new window..." -ForegroundColor Gray

$dashboardScript = @"
cd d:\Development\ai_digital_signage\microservices\digital-signage-dashboard
Write-Host 'Starting Dashboard...' -ForegroundColor Cyan
npm run dev
"@

Start-Process powershell -ArgumentList "-NoExit", "-Command", $dashboardScript

Write-Host "  Dashboard starting (allow 10-15 seconds)..." -ForegroundColor Green

# Wait and verify
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "Waiting 15 seconds for dashboard to initialize..." -ForegroundColor Yellow
Start-Sleep -Seconds 15

Write-Host "`nVerifying services..." -ForegroundColor Cyan

# Check Backend
Write-Host "Backend API: " -NoNewline
try {
    $health = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -TimeoutSec 5 -ErrorAction Stop
    Write-Host "READY ($($health.status))" -ForegroundColor Green
    
    $api = Invoke-RestMethod -Uri "http://localhost:8080/api/dashboard/overview" -TimeoutSec 3
    Write-Host "  Total Audience: $($api.totalAudience)" -ForegroundColor Gray
    Write-Host "  Total Views: $($api.totalViews)" -ForegroundColor Gray
} catch {
    Write-Host "STILL STARTING (check backend window)" -ForegroundColor Yellow
}

# Check Dashboard
Write-Host "Dashboard:   " -NoNewline
try {
    $dash = Invoke-WebRequest -Uri "http://localhost:5174" -TimeoutSec 3 -UseBasicParsing -ErrorAction Stop
    Write-Host "READY (HTTP $($dash.StatusCode))" -ForegroundColor Green
} catch {
    Write-Host "STILL STARTING (check dashboard window)" -ForegroundColor Yellow
}

# Check ETL Service
Write-Host "ETL Service: " -NoNewline
try {
    $etl = Invoke-RestMethod -Uri "http://localhost:8081/actuator/health" -TimeoutSec 3 -ErrorAction Stop
    Write-Host "READY ($($etl.status))" -ForegroundColor Green
} catch {
    Write-Host "STILL STARTING (check ETL window)" -ForegroundColor Yellow
}

Write-Host "`n========================================" -ForegroundColor Green
Write-Host "Service URLs:" -ForegroundColor Cyan
Write-Host "  Dashboard:      http://localhost:5174" -ForegroundColor White
Write-Host "  Backend API:    http://localhost:8080/api/dashboard/overview" -ForegroundColor White
Write-Host "  Backend Health: http://localhost:8080/actuator/health" -ForegroundColor White
Write-Host "  ETL Service:    http://localhost:8081/actuator/health" -ForegroundColor White
Write-Host "  Swagger UI:     http://localhost:8080/swagger-ui.html" -ForegroundColor White
Write-Host "`nServices are starting in separate windows" -ForegroundColor Yellow
Write-Host "Run .\test-flow.ps1 to verify complete system" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Green
