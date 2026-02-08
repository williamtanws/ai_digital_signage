@echo off
REM Start Digital Signage Service with SQLite Browser

echo ========================================
echo Digital Signage Service Startup Script
echo ========================================
echo.

REM Set Java 21 environment
echo [1/4] Setting Java 21 environment...
set JAVA_HOME=C:\Program Files\SapMachine\JDK\21
set PATH=%JAVA_HOME%\bin;%PATH%

java -version
if %errorlevel% neq 0 (
    echo ERROR: Java 21 not found. Please install Java 21.
    pause
    exit /b 1
)
echo.

REM Start SQLite Browser Docker Container
echo [2/4] Starting SQLite Browser container...

REM Check if container already exists
docker ps -a --filter "name=sqlite-browser" --format "{{.Names}}" > temp_container_check.txt 2>nul
set /p EXISTING_CONTAINER=<temp_container_check.txt
del temp_container_check.txt 2>nul

if "%EXISTING_CONTAINER%"=="sqlite-browser" (
    echo       Found existing sqlite-browser container...
    
    REM Check if it's running
    docker ps --filter "name=sqlite-browser" --format "{{.Names}}" > temp_running_check.txt 2>nul
    set /p IS_RUNNING=<temp_running_check.txt
    del temp_running_check.txt 2>nul
    
    if "%IS_RUNNING%"=="sqlite-browser" (
        echo       SQLite browser is already running
    ) else (
        echo       Starting existing container...
        docker start sqlite-browser >nul 2>&1
        echo       SQLite browser started successfully
    )
) else (
    REM Create and start new container
    echo       Creating SQLite browser container...
    docker run -d --name sqlite-browser -p 3000:8080 -v "%CD%/data:/data" -e SQLITE_DATABASE=/data/digital-signage.db coleifer/sqlite-web >nul 2>&1
    if %errorlevel% equ 0 (
        echo       SQLite browser started successfully
    ) else (
        echo       Warning: Could not start SQLite browser
        echo       Service will continue without browser UI
    )
)
echo       Access at: http://localhost:3000
echo.

REM Create data directory
echo [3/4] Checking database directory...
if not exist "data" (
    mkdir data
    echo       Created data directory
)
echo.

REM Start Spring Boot Service
echo [4/4] Starting Spring Boot service...
echo       API will be available at: http://localhost:8080
echo       SQLite browser at: http://localhost:3000
echo.
echo ========================================
echo Press Ctrl+C to stop the service
echo ========================================
echo.

mvn spring-boot:run
