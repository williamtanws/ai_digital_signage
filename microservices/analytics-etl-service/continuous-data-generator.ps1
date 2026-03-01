# Continuous TDengine Data Generator
# Continuously adds random viewer sessions to simulate real-time traffic
# Press Ctrl+C to stop

param(
    [int]$IntervalSeconds = 10,  # How often to add new sessions
    [int]$SessionsPerInterval = 5  # Number of sessions to add each interval
)

$tdengineUrl = 'http://localhost:6041/rest/sql/digital_signage'
$credentials = 'root:taosdata'
$encodedCreds = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes($credentials))
$headers = @{'Authorization' = "Basic $encodedCreds"}

$ads = @('breakfast', 'burger', 'coffee', 'pizza', 'salad')
$genders = @('Male', 'Female')
$emotions = @('happy', 'neutral', 'surprised', 'calm', 'focused')

function Get-RandomViewer {
    param([int]$ViewerId)
    
    $age = Get-Random -Minimum 18 -Maximum 66
    $gender = $genders | Get-Random
    $emotion = $emotions | Get-Random
    $adName = $ads | Get-Random
    
    $gazeCount = Get-Random -Minimum 5 -Maximum 51
    $gazeTime = [math]::Round((Get-Random -Minimum 2.0 -Maximum 30.0), 2)
    $sessionDuration = [math]::Round($gazeTime * (Get-Random -Minimum 1.1 -Maximum 1.5), 2)
    $engagementRate = if ($sessionDuration -gt 0) { [math]::Round(($gazeTime / $sessionDuration) * 100, 2) } else { 0 }
    
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
    }
}

function Insert-ViewerSession($ViewerId, $Data) {
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
    
    $jsonData = $jsonData.Replace("'", "''")
    $sql = "INSERT INTO gaze_events_$ViewerId USING gaze_events TAGS('session_end', '$($Data.ViewerId)') VALUES (NOW, '$jsonData');"
    
    try {
        $response = Invoke-RestMethod -Uri $tdengineUrl -Method Post -Headers $headers -Body $sql -TimeoutSec 5
        return ($response.code -eq 0)
    } catch {
        return $false
    }
}

function Get-CurrentCount {
    try {
        $sql = "SELECT COUNT(*) FROM gaze_events WHERE evt_type = 'session_end'"
        $response = Invoke-RestMethod -Uri $tdengineUrl -Method Post -Headers $headers -Body $sql -TimeoutSec 5
        if ($response.code -eq 0 -and $response.data) { return $response.data[0][0] }
    } catch { }
    return $null
}

# Banner
Clear-Host
Write-Host ""
Write-Host "╔════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║       CONTINUOUS DATA GENERATOR - RUNNING             ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""
Write-Host "  Interval: $IntervalSeconds seconds" -ForegroundColor White
Write-Host "  Sessions per interval: $SessionsPerInterval" -ForegroundColor White
Write-Host "  Target: TDengine (digital_signage)" -ForegroundColor White
Write-Host ""
Write-Host "  Press Ctrl+C to stop" -ForegroundColor Yellow
Write-Host ""
Write-Host "────────────────────────────────────────────────────────" -ForegroundColor Gray

$startId = [int]([DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds() % 1000000)
$totalInserted = 0
$cycleCount = 0

try {
    while ($true) {
        $cycleCount++
        $cycleSuccess = 0
        $cycleFailed = 0
        
        # Insert batch of sessions
        for ($i = 0; $i -lt $SessionsPerInterval; $i++) {
            $viewerId = $startId + $totalInserted + $i
            $data = Get-RandomViewer -ViewerId $viewerId
            
            if (Insert-ViewerSession -ViewerId $viewerId -Data $data) {
                $cycleSuccess++
            } else {
                $cycleFailed++
            }
        }
        
        $totalInserted += $cycleSuccess
        $timestamp = Get-Date -Format "HH:mm:ss"
        
        # Get current total
        $totalRecords = Get-CurrentCount
        $totalDisplay = if ($null -ne $totalRecords) { "$totalRecords records" } else { "N/A" }
        
        # Status line
        $status = "[$timestamp] Cycle #$cycleCount | Added: $cycleSuccess"
        if ($cycleFailed -gt 0) {
            $status += " | Failed: $cycleFailed"
            Write-Host $status -ForegroundColor Yellow
        } else {
            Write-Host $status -ForegroundColor Green
        }
        
        # Summary every 10 cycles
        if ($cycleCount % 10 -eq 0) {
            Write-Host "────────────────────────────────────────────────────────" -ForegroundColor Gray
            Write-Host "  Total inserted this run: $totalInserted sessions" -ForegroundColor Cyan
            Write-Host "  Total in database: $totalDisplay" -ForegroundColor Cyan
            Write-Host "────────────────────────────────────────────────────────" -ForegroundColor Gray
        }
        
        Start-Sleep -Seconds $IntervalSeconds
    }
} finally {
    Write-Host ""
    Write-Host "════════════════════════════════════════════════════════" -ForegroundColor Yellow
    Write-Host "  Generator stopped" -ForegroundColor Yellow
    Write-Host "  Total sessions inserted: $totalInserted" -ForegroundColor White
    Write-Host "════════════════════════════════════════════════════════" -ForegroundColor Yellow
    Write-Host ""
}
