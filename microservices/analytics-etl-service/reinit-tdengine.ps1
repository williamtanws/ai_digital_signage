# Reinitialize TDengine Database with JSON Schema
# This script drops existing data and recreates the database with proper JSON format

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "TDengine Database Reinitialization" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

$tdengineUrl = 'http://localhost:6041/rest/sql'
$credentials = 'root:taosdata'
$encodedCreds = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes($credentials))
$headers = @{'Authorization' = "Basic $encodedCreds"}

# Step 1: Drop existing database
Write-Host "[1/3] Dropping existing database..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri $tdengineUrl -Method Post -Headers $headers -Body 'DROP DATABASE IF EXISTS digital_signage' -TimeoutSec 5
    if ($response.code -eq 0) {
        Write-Host "  Database dropped successfully" -ForegroundColor Green
    } else {
        Write-Host "  Warning: $($response.desc)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Step 2: Execute init script
Write-Host "`n[2/3] Creating database and loading initial data..." -ForegroundColor Yellow
Write-Host "  This may take 30-60 seconds..." -ForegroundColor Gray

try {
    # Read the init SQL file
    $initSql = Get-Content -Path ".\tdengine_init.sql" -Raw
    
    # Split by statements and execute
    $statements = $initSql -split ';' | Where-Object { $_.Trim() -ne '' -and $_.Trim() -notmatch '^--' }
    
    $successCount = 0
    $errorCount = 0
    
    foreach ($stmt in $statements) {
        $cleanStmt = $stmt.Trim()
        if ($cleanStmt -ne '') {
            try {
                $response = Invoke-RestMethod -Uri $tdengineUrl -Method Post -Headers $headers -Body $cleanStmt -TimeoutSec 10
                if ($response.code -eq 0) {
                    $successCount++
                } else {
                    $errorCount++
                    if ($errorCount -le 3) {
                        Write-Host "  Warning: $($response.desc)" -ForegroundColor Yellow
                    }
                }
            } catch {
                $errorCount++
            }
        }
    }
    
    Write-Host "  Executed $successCount statements successfully" -ForegroundColor Green
    if ($errorCount -gt 0) {
        Write-Host "  $errorCount statements had warnings/errors" -ForegroundColor Yellow
    }
} catch {
    Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Step 3: Verify
Write-Host "`n[3/3] Verifying database..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$tdengineUrl/digital_signage" -Method Post -Headers $headers -Body "SELECT COUNT(*) FROM gaze_events WHERE evt_type = 'session_end'" -TimeoutSec 5
    $count = $response.data[0][0]
    Write-Host "  Total records: $count" -ForegroundColor Green
    
    # Show table structure
    $response = Invoke-RestMethod -Uri "$tdengineUrl/digital_signage" -Method Post -Headers $headers -Body "DESCRIBE gaze_events" -TimeoutSec 5
    Write-Host "`n  Table Structure:" -ForegroundColor Cyan
    Write-Host "    ts         - TIMESTAMP" -ForegroundColor White
    Write-Host "    event_data - NCHAR(4096) [JSON format]" -ForegroundColor White
    Write-Host "    evt_type   - TAG (NCHAR)" -ForegroundColor White
    Write-Host "    viewer_id  - TAG (NCHAR)" -ForegroundColor White
} catch {
    Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host "`n========================================" -ForegroundColor Green
Write-Host "Database Reinitialization Complete" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host "`nNext steps:" -ForegroundColor Cyan
Write-Host "  1. Run ETL: cd ..; mvn spring-boot:run -q" -ForegroundColor White
Write-Host "  2. Check backend API: http://localhost:8080/api/dashboard/overview" -ForegroundColor White
Write-Host "  3. View dashboard: http://localhost:5174" -ForegroundColor White
Write-Host ""
