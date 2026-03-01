# TDengine Heartbeat Data Generator
# Adds heartbeat events with performance, environment, and diagnostics data

param(
    [int]$NumRecords = 10
)

$tdengineUrl = 'http://localhost:6041/rest/sql/digital_signage'
$credentials = 'root:taosdata'
$encodedCreds = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes($credentials))
$headers = @{'Authorization' = "Basic $encodedCreds"}

function Get-RandomHeartbeat {
    $fps = [math]::Round((Get-Random -Minimum 7.5 -Maximum 10.5), 1)
    $cpuTemp = [math]::Round((Get-Random -Minimum 50.0 -Maximum 65.0), 1)
    $uptime = Get-Random -Minimum 86400 -Maximum 864000
    $temperature = [math]::Round((Get-Random -Minimum 28.0 -Maximum 35.0), 2)
    $humidity = [math]::Round((Get-Random -Minimum 45.0 -Maximum 75.0), 2)
    $pressure = [math]::Round((Get-Random -Minimum 995.0 -Maximum 1010.0), 2)
    $gasResistance = [math]::Round((Get-Random -Minimum 20000.0 -Maximum 80000.0), 2)
    $noise = [math]::Round((Get-Random -Minimum 50.0 -Maximum 70.0), 1)
    $kptsValid = [math]::Round((Get-Random -Minimum 0.0 -Maximum 80.0), 1)
    $solvepnpSuccess = [math]::Round((Get-Random -Minimum 0.0 -Maximum 20.0), 1)
    $fallback = [math]::Round(100.0 - $solvepnpSuccess, 1)
    $facesInFrame = Get-Random -Minimum 0 -Maximum 4
    $faceConfidence = if ($facesInFrame -gt 0) { [math]::Round((Get-Random -Minimum 0.85 -Maximum 0.99), 2) } else { 0.0 }
    
    return @{
        Fps = $fps
        CpuTemp = $cpuTemp
        Uptime = $uptime
        Temperature = $temperature
        Humidity = $humidity
        Pressure = $pressure
        GasResistance = $gasResistance
        Noise = $noise
        KptsValidPercent = $kptsValid
        SolvepnpSuccessPercent = $solvepnpSuccess
        FallbackPercent = $fallback
        FacesInFrame = $facesInFrame
        FaceConfidence = $faceConfidence
    }
}

function Insert-HeartbeatEvent($Data) {
    $timestamp = (Get-Date).ToUniversalTime().ToString("yyyy-MM-ddTHH:mm:ss.fffZ")
    
    $jsonData = @{
        event = "heartbeat"
        timestamp = $timestamp
        performance = @{
            fps = $Data.Fps
            cpu_temp = $Data.CpuTemp
            uptime = $Data.Uptime
        }
        environment = @{
            temperature = $Data.Temperature
            humidity = $Data.Humidity
            pressure = $Data.Pressure
            gas_resistance = $Data.GasResistance
            noise = $Data.Noise
        }
        diagnostics = @{
            kpts_valid_percent = $Data.KptsValidPercent
            solvepnp_success_percent = $Data.SolvepnpSuccessPercent
            fallback_percent = $Data.FallbackPercent
            faces_in_frame = $Data.FacesInFrame
            face_confidence = $Data.FaceConfidence
        }
    } | ConvertTo-Json -Compress -Depth 10
    
    $jsonData = $jsonData.Replace("'", "''")
    $viewerId = "system_heartbeat"
    $sql = "INSERT INTO gaze_events_heartbeat USING gaze_events TAGS('heartbeat', '$viewerId') VALUES (NOW, '$jsonData');"
    
    try {
        $response = Invoke-RestMethod -Uri $tdengineUrl -Method Post -Headers $headers -Body $sql -TimeoutSec 5
        if ($response.code -eq 0) { return $true, $null }
        else { return $false, "Code: $($response.code) - $($response.desc)" }
    } catch {
        return $false, $_.Exception.Message
    }
}

function Get-HeartbeatCount {
    try {
        $sql = "SELECT COUNT(*) FROM gaze_events WHERE evt_type = 'heartbeat'"
        $response = Invoke-RestMethod -Uri $tdengineUrl -Method Post -Headers $headers -Body $sql -TimeoutSec 5
        if ($response.code -eq 0 -and $response.data) { return $response.data[0][0] }
    } catch { return $null }
}

Write-Host ""
Write-Host "Heartbeat Data Generator - Adding $NumRecords Events" -ForegroundColor Cyan
Write-Host ""

$initialCount = Get-HeartbeatCount
if ($null -ne $initialCount) {
    Write-Host "Current heartbeat events: $initialCount" -ForegroundColor White
}

Write-Host ""
Write-Host "Inserting $NumRecords heartbeat events..." -ForegroundColor Yellow

$successCount = 0
$failedCount = 0

for ($i = 0; $i -lt $NumRecords; $i++) {
    $data = Get-RandomHeartbeat
    $success, $error = Insert-HeartbeatEvent -Data $data
    
    if ($success) {
        $successCount++
        Write-Host "  [$($i + 1)/$NumRecords] FPS: $($data.Fps) | CPU: $($data.CpuTemp)C | Faces: $($data.FacesInFrame) | Conf: $($data.FaceConfidence)" -ForegroundColor Green
    } else {
        $failedCount++
        if ($failedCount -le 3) {
            Write-Host "  [$($i + 1)/$NumRecords] Error: $error" -ForegroundColor Red
        }
    }
    
    Start-Sleep -Milliseconds 100
}

Write-Host ""
Write-Host "Insertion Complete" -ForegroundColor Green
Write-Host "Heartbeats inserted: $successCount" -ForegroundColor Green

if ($failedCount -gt 0) {
    Write-Host "Failed: $failedCount events" -ForegroundColor Red
}

$finalCount = Get-HeartbeatCount
if ($null -ne $finalCount -and $null -ne $initialCount) {
    Write-Host ""
    Write-Host "Before: $initialCount heartbeats" -ForegroundColor White
    Write-Host "After:  $finalCount heartbeats" -ForegroundColor White
    Write-Host "Added:  $($finalCount - $initialCount) events" -ForegroundColor Cyan
}

Write-Host ""
Write-Host "Next: Start ETL service (mvn spring-boot:run) and trigger ETL" -ForegroundColor Yellow
Write-Host ""
