# Stop all Java processes
Write-Host "`n=== Stopping Services ===" -ForegroundColor Cyan
Get-Process java -ErrorAction SilentlyContinue | Stop-Process -Force
Start-Sleep -Seconds 3
Write-Host "✓ All Java services stopped" -ForegroundColor Green

# Delete ETL metadata to force full reprocess
Write-Host "`n=== Cleaning Metadata ===" -ForegroundColor Cyan
$metadataPath = ".\data\etl-metadata.txt"
if (Test-Path $metadataPath) {
    Remove-Item $metadataPath -Force
    Write-Host "✓ Deleted $metadataPath" -ForegroundColor Green
} else {
    Write-Host "⚠ Metadata file not found (will be created fresh)" -ForegroundColor Yellow
}

# Clean build
Write-Host "`n=== Building with Fixed Code ===" -ForegroundColor Cyan
mvn clean package -q -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Host "✗ Build failed!" -ForegroundColor Red
    exit 1
}
Write-Host "✓ Build successful with fixed TDengineJsonParser" -ForegroundColor Green

# Start service
Write-Host "`n=== Starting ETL Service ===" -ForegroundColor Cyan
Write-Host "This will process ALL 397 records with the fixed parser..." -ForegroundColor Yellow
Write-Host "Watch for: NO NullPointerException errors" -ForegroundColor Yellow
Write-Host "Expected: Demographics and engagement data extracted" -ForegroundColor Yellow
Write-Host "`nStarting in 3 seconds... (Press Ctrl+C to cancel)" -ForegroundColor Gray
Start-Sleep -Seconds 3

mvn spring-boot:run -q
