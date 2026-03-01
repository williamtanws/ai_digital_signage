# TDengine Data Generator
# Adds random viewer session data to TDengine
# 
# Usage:
#   .\add-test-data.ps1                     # Add 100 records (one-time)
#   .\add-test-data.ps1 -NumRecords 50      # Add 50 records (one-time)
#   .\add-test-data.ps1 -ResetEtl           # Add 100 records + full ETL extraction
#   .\add-test-data.ps1 -Continuous         # Continuous mode: add data every 10 seconds
#   .\add-test-data.ps1 -Continuous -IntervalSeconds 5 -BatchSize 10

param(
    [int]$NumRecords = 100,
    [switch]$Continuous,
    [int]$IntervalSeconds = 10,
    [int]$BatchSize = 5,
    [switch]$ResetEtl
)

$tdengineUrl = 'http://localhost:6041/rest/sql/digital_signage'
$etlTriggerUrl = 'http://localhost:8081/api/etl/trigger'
$etlMetadataPath = Join-Path $PSScriptRoot 'data\etl-metadata.txt'
$credentials = 'root:taosdata'
$encodedCreds = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes($credentials))
$headers = @{'Authorization' = "Basic $encodedCreds"}

$ads = @('breakfast', 'burger', 'coffee', 'pizza', 'salad')
$genders = @('Male', 'Female')
$emotions = @('anger', 'contempt', 'disgust', 'fear', 'happiness', 'neutral', 'sadness', 'surprise')

function Get-RandomViewer {
    param([int]$ViewerId)
    
    # Age distribution matching ETL categorization:
    # Children: 0-12, Teenagers: 13-19, Young Adults: 20-35, Mid-Aged: 36-55, Seniors: 56+
    # Distribution: 5% children, 10% teens, 40% young adults, 30% mid-aged, 15% seniors
    $ageRoll = Get-Random -Minimum 1 -Maximum 101
    if ($ageRoll -le 5) {
        $age = Get-Random -Minimum 5 -Maximum 13      # Children: 5-12 (matching 0-12)
    } elseif ($ageRoll -le 15) {
        $age = Get-Random -Minimum 13 -Maximum 20     # Teenagers: 13-19
    } elseif ($ageRoll -le 55) {
        $age = Get-Random -Minimum 20 -Maximum 36     # Young Adults: 20-35
    } elseif ($ageRoll -le 85) {
        $age = Get-Random -Minimum 36 -Maximum 56     # Mid-Aged: 36-55
    } else {
        $age = Get-Random -Minimum 56 -Maximum 81     # Seniors: 56+
    }
    
    $gender = $genders | Get-Random
    $emotion = $emotions | Get-Random
    $adName = $ads | Get-Random
    
    $gazeCount = Get-Random -Minimum 5 -Maximum 51
    $gazeTime = [math]::Round((Get-Random -Minimum 2.0 -Maximum 30.0), 2)
    
    # 30% chance of low engagement (didn't look at ad much)
    $isLowEngagement = (Get-Random -Minimum 1 -Maximum 101) -le 30
    if ($isLowEngagement) {
        # Low engagement: session much longer than gaze time (engagement 10-45%)
        $sessionDuration = [math]::Round($gazeTime * (Get-Random -Minimum 2.2 -Maximum 10.0), 2)
    } else {
        # High engagement: gaze time close to session duration (engagement 55-90%)
        $sessionDuration = [math]::Round($gazeTime * (Get-Random -Minimum 1.1 -Maximum 1.8), 2)
    }
    
    $engagementRate = if ($sessionDuration -gt 0) { [math]::Round(($gazeTime / $sessionDuration) * 100, 2) } else { 0 }
    $minutesAgo = Get-Random -Minimum 1 -Maximum 181
    
    return @{
        ViewerId = "viewer_$ViewerId"
        Age = $age
        Gender = $gender
        Emotion = $emotion
        AdName = $adName
        GazeCount = $gazeCount
        GazeTime = $gazeTime
        SessionDuration = $sessionDuration
        EngagementRate = $engagementRate
        MinutesAgo = $minutesAgo
    }
}

function Insert-ViewerSession($ViewerId, $Data) {
    # Build JSON event data for TDengine
    $timestamp = (Get-Date).ToUniversalTime().ToString("yyyy-MM-ddTHH:mm:ssZ")
    $engagementRate = [math]::Round($Data.EngagementRate / 100, 2)
    
    $jsonData = @{
        timestamp = $timestamp
        event = "session_end"
        viewer_id = $Data.ViewerId
        session_stats = @{
            total_gaze_time = $Data.GazeTime
            gaze_count = $Data.GazeCount
            session_duration = $Data.SessionDuration
            engagement_rate = $engagementRate
        }
        demographics = @{
            age = $Data.Age
            gender = $Data.Gender
            emotions = @{
                $Data.Emotion = 1
            }
        }
        ad_context = @{
            ad_name = $Data.AdName
        }
    } | ConvertTo-Json -Compress -Depth 10
    
    # Escape single quotes in JSON for SQL
    $jsonData = $jsonData.Replace("'", "''")
    
    # Insert with current timestamp (NOW) for incremental ETL compatibility
    $sql = "INSERT INTO gaze_events_$ViewerId USING gaze_events TAGS('session_end', '$($Data.ViewerId)') VALUES (NOW, '$jsonData');"
    
    try {
        $response = Invoke-RestMethod -Uri $tdengineUrl -Method Post -Headers $headers -Body $sql -TimeoutSec 5
        if ($response.code -eq 0) { return $true, $null }
        else { return $false, "Code: $($response.code) - $($response.desc)" }
    } catch {
        return $false, $_.Exception.Message
    }
}

function Get-CurrentCount {
    try {
        $sql = "SELECT COUNT(*) FROM gaze_events WHERE evt_type = 'session_end'"
        $response = Invoke-RestMethod -Uri $tdengineUrl -Method Post -Headers $headers -Body $sql -TimeoutSec 5
        if ($response.code -eq 0 -and $response.data) { return $response.data[0][0] }
    } catch { return $null }
}

function Trigger-EtlProcess {
    param([switch]$Reset)
    
    if ($Reset) {
        if (Test-Path $etlMetadataPath) {
            Remove-Item $etlMetadataPath -Force -ErrorAction SilentlyContinue
            Write-Host "  ETL metadata reset (full extraction)" -ForegroundColor Yellow
        }
    }
    
    try {
        $response = Invoke-RestMethod -Uri $etlTriggerUrl -Method Post -TimeoutSec 30
        if ($response.status -eq 'success') {
            Write-Host "  ETL triggered successfully (${($response.durationMs)}ms)" -ForegroundColor Green
            return $true
        } else {
            Write-Host "  ETL trigger failed: $($response.message)" -ForegroundColor Red
            return $false
        }
    } catch {
        Write-Host "  ETL trigger failed: $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

function Insert-BatchRecords {
    param([int]$Count, [ref]$StartId, [ref]$ViewerPool)
    
    $successCount = 0
    $failedCount = 0
    $newViewers = 0
    $returnViewers = 0
    
    for ($i = 0; $i -lt $Count; $i++) {
        # 40% chance of being a return viewer (if we have viewers in pool)
        $isReturnViewer = ($ViewerPool.Value.Count -gt 0) -and ((Get-Random -Minimum 0 -Maximum 100) -lt 40)
        
        if ($isReturnViewer) {
            # Pick a random existing viewer from the pool
            $viewerId = $ViewerPool.Value | Get-Random
            $returnViewers++
        } else {
            # Create a new viewer
            $viewerId = $StartId.Value
            $StartId.Value++
            $ViewerPool.Value += $viewerId
            $newViewers++
        }
        
        $data = Get-RandomViewer -ViewerId $viewerId
        $success, $error = Insert-ViewerSession -ViewerId $viewerId -Data $data
        
        if ($success) { $successCount++ }
        else { $failedCount++ }
    }
    
    return $successCount, $failedCount, $newViewers, $returnViewers
}

Write-Host ""
Write-Host "========================================================" -ForegroundColor Cyan
if ($Continuous) {
    Write-Host " TDengine Data Generator - CONTINUOUS MODE" -ForegroundColor Cyan
    Write-Host " Batch Size: $BatchSize | Interval: ${IntervalSeconds}s" -ForegroundColor Cyan
} else {
    Write-Host " TDengine Data Generator - Inserting $NumRecords Sessions" -ForegroundColor Cyan
}
Write-Host "========================================================" -ForegroundColor Cyan
Write-Host ""

$initialCount = Get-CurrentCount
if ($null -ne $initialCount) {
    Write-Host "Current TDengine records: $initialCount" -ForegroundColor White
} else {
    Write-Host "Warning: Could not fetch current count" -ForegroundColor Yellow
}

$startId = [int]([DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds() % 1000000)
$viewerPool = @()  # Pool of viewer IDs for return viewers

if ($Continuous) {
    # Continuous mode: add data in batches and trigger ETL
    Write-Host ""
    Write-Host "Starting continuous data generation (Press Ctrl+C to stop)..." -ForegroundColor Yellow
    Write-Host "(~40% of sessions are return viewers)" -ForegroundColor Gray
    Write-Host ""
    
    $totalInserted = 0
    $totalNewViewers = 0
    $totalReturnViewers = 0
    $iteration = 0
    
    try {
        while ($true) {
            $iteration++
            $batchStart = Get-Date
            
            Write-Host "[$(Get-Date -Format 'HH:mm:ss')] Iteration $iteration - Inserting $BatchSize records..." -ForegroundColor Cyan
            
            $success, $failed, $newViewers, $returnViewers = Insert-BatchRecords -Count $BatchSize -StartId ([ref]$startId) -ViewerPool ([ref]$viewerPool)
            $totalInserted += $success
            $totalNewViewers += $newViewers
            $totalReturnViewers += $returnViewers
            
            Write-Host "  Inserted: $success | New Viewers: $newViewers | Return: $returnViewers | Pool: $($viewerPool.Count)" -ForegroundColor $(if ($failed -eq 0) { "Green" } else { "Yellow" })
            
            # Trigger ETL to process new data
            Write-Host "  Triggering ETL process..." -ForegroundColor Gray
            Trigger-EtlProcess -Reset:$ResetEtl
            
            # Wait for next interval
            $elapsed = ((Get-Date) - $batchStart).TotalSeconds
            $sleepTime = [Math]::Max(1, $IntervalSeconds - $elapsed)
            Write-Host "  Next batch in ${sleepTime}s..." -ForegroundColor Gray
            Write-Host ""
            Start-Sleep -Seconds $sleepTime
        }
    } catch {
        Write-Host ""
        Write-Host "========================================================" -ForegroundColor Yellow
        Write-Host " Continuous mode stopped" -ForegroundColor Yellow
        Write-Host " Total sessions: $totalInserted | Unique viewers: $($viewerPool.Count)" -ForegroundColor Green
        Write-Host " New viewers: $totalNewViewers | Return viewers: $totalReturnViewers" -ForegroundColor Cyan
        Write-Host "========================================================" -ForegroundColor Yellow
    }
} else {
    # One-time mode: insert specified number of records
    $successCount = 0
    $failedCount = 0
    $newViewerCount = 0
    $returnViewerCount = 0

    Write-Host ""
    Write-Host "Inserting $NumRecords viewer sessions (~40% return viewers)..." -ForegroundColor Yellow

    for ($i = 0; $i -lt $NumRecords; $i++) {
        # 40% chance of being a return viewer (if we have viewers in pool)
        $isReturnViewer = ($viewerPool.Count -gt 0) -and ((Get-Random -Minimum 0 -Maximum 100) -lt 40)
        
        if ($isReturnViewer) {
            $viewerId = $viewerPool | Get-Random
            $returnViewerCount++
        } else {
            $viewerId = $startId + $newViewerCount
            $viewerPool += $viewerId
            $newViewerCount++
        }
        
        $data = Get-RandomViewer -ViewerId $viewerId
        
        $success, $error = Insert-ViewerSession -ViewerId $viewerId -Data $data
        
        if ($success) {
            $successCount++
            if (($i + 1) % 20 -eq 0) {
                Write-Host "  Progress: $($i + 1)/$NumRecords sessions inserted" -ForegroundColor Gray
            }
        } else {
            $failedCount++
            if ($failedCount -le 5) {
                $vid = "viewer_$viewerId"
                Write-Host "  Error inserting $vid : $error" -ForegroundColor Red
            }
        }
    }

    Write-Host ""
    Write-Host "========================================================" -ForegroundColor Green
    Write-Host " Insertion Complete" -ForegroundColor Green
    Write-Host "========================================================" -ForegroundColor Green
    Write-Host " Sessions inserted: $successCount" -ForegroundColor Green
    Write-Host " Unique viewers:    $newViewerCount (new) | $returnViewerCount (return)" -ForegroundColor Cyan

    if ($failedCount -gt 0) {
        Write-Host " Failed: $failedCount sessions" -ForegroundColor Red
    }

    $finalCount = Get-CurrentCount
    if ($null -ne $finalCount -and $null -ne $initialCount) {
        Write-Host ""
        Write-Host " Before: $initialCount records" -ForegroundColor White
        Write-Host " After:  $finalCount records" -ForegroundColor White
        Write-Host " Added:  $($finalCount - $initialCount) records" -ForegroundColor Cyan
    }

    Write-Host ""
    Write-Host " Triggering ETL process..." -ForegroundColor Yellow
    Trigger-EtlProcess -Reset:$ResetEtl

    Write-Host ""
    Write-Host " Dashboard: http://localhost:8080/api/dashboard/overview" -ForegroundColor Gray
    Write-Host "========================================================" -ForegroundColor Cyan
    Write-Host ""
}
