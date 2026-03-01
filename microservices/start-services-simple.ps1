# Simple Service Startup Script
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Starting Digital Signage Services" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# Set Java environment
$env:JAVA_HOME = "C:\Program Files\SapMachine\JDK\21"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

Write-Host "[1/3] Starting Backend Service..." -ForegroundColor Yellow
Write-Host "  Location: digital-signage-service" -ForegroundColor Gray
Write-Host "  Port: 8080" -ForegroundColor Gray
cd digital-signage-service
Start-Process powershell -ArgumentList '-NoExit', '-Command', 'cd d:\Development\ai_digital_signage\microservices\digital-signage-service; $env:JAVA_HOME = "C:\Program Files\SapMachine\JDK\21"; $env:PATH = "$env:JAVA_HOME\bin;$env:PATH"; mvn spring-boot:run'
cd ..

Write-Host "Waiting 40 seconds for backend to start..." -ForegroundColor Gray
Start-Sleep -Seconds 40

Write-Host "`n[2/3] Running ETL Service..." -ForegroundColor Yellow
Write-Host "  Processing data from TDengine to SQLite..." -ForegroundColor Gray
cd analytics-etl-service
mvn spring-boot:run -q
cd ..

Write-Host "`n[3/3] Starting Dashboard..." -ForegroundColor Yellow
Write-Host "  Location: digital-signage-dashboard" -ForegroundColor Gray
Write-Host "  Port: 5174" -ForegroundColor Gray
cd digital-signage-dashboard
Start-Process powershell -ArgumentList '-NoExit', '-Command', 'cd d:\Development\ai_digital_signage\microservices\digital-signage-dashboard; npm run dev'
cd ..

Write-Host "`nWaiting 10 seconds for dashboard to start..." -ForegroundColor Gray
Start-Sleep -Seconds 10

Write-Host "`n========================================" -ForegroundColor Green
Write-Host "Services Started!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green

Write-Host "`nService URLs:" -ForegroundColor Cyan
Write-Host "  Backend API:  http://localhost:8080/api/dashboard/overview" -ForegroundColor White
Write-Host "  Dashboard:    http://localhost:5174" -ForegroundColor White
Write-Host "  Swagger UI:   http://localhost:8080/swagger-ui.html" -ForegroundColor White

Write-Host "`nNow run: .\test-flow.ps1 to verify everything is working" -ForegroundColor Yellow
