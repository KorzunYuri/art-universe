@echo off
REM Cross-platform Docker deployment script
REM Works in Windows Command Prompt

setlocal enabledelayedexpansion

REM Function to show usage
if "%1"=="" (
    echo ❌ Error: Environment parameter is required
    echo.
    echo Usage: %0 ^<environment^>
    echo.
    echo Available environments:
    echo   local - Deploy with local databases in containers
    echo   prod  - Deploy with external databases on host machine
    echo.
    echo Examples:
    echo   %0 local
    echo   %0 prod
    exit /b 1
)

set "ENVIRONMENT=%1"

REM Validate environment parameter
if not "%ENVIRONMENT%"=="local" if not "%ENVIRONMENT%"=="prod" (
    echo ❌ Error: Invalid environment '%ENVIRONMENT%'
    echo.
    echo Available environments: local, prod
    exit /b 1
)

REM Get the project root directory (two levels up from this script)
set "SCRIPT_DIR=%~dp0"
for %%i in ("%SCRIPT_DIR%..\..\") do set "PROJECT_ROOT=%%~fi"

REM Set environment-specific variables
if "%ENVIRONMENT%"=="local" (
    set "COMPOSE_FILE=%PROJECT_ROOT%\env\docker\local\docker-compose.yml"
    set "ENV_NAME=Local"
    set "SERVICES_INFO=  - LastFM Raw Data: http://localhost:9081
  - Music Data: http://localhost:9082
  - Music Quiz: http://localhost:9083
  - UI: http://localhost:4000
  - Adminer: http://localhost:9980"
) else (
    set "COMPOSE_FILE=%PROJECT_ROOT%\env\docker\prod\docker-compose.yml"
    set "ENV_NAME=Production"
    set "SERVICES_INFO=  - LastFM Raw Data: http://localhost:8081
  - Music Data: http://localhost:8082
  - Music Quiz: http://localhost:8083
  - UI: http://localhost:3000
  - Adminer: http://localhost:8880

Note: Applications connect to external databases on host machine"
)

echo === Art Universe %ENV_NAME% Environment Deployment ===
echo Project root: %PROJECT_ROOT%
echo Gradle command: gradlew.bat
echo Environment: %ENVIRONMENT%
echo.

REM Stop and remove existing containers and images
echo Step 1: Stopping and removing existing containers...
docker compose -f "%COMPOSE_FILE%" down --remove-orphans

REM Build the project
echo.
echo Step 2: Building project...
cd /d "%PROJECT_ROOT%"
call gradlew.bat clean build -x test

if !errorlevel! neq 0 (
    echo ❌ Build failed! Aborting deployment.
    exit /b 1
)

REM Start with rebuilding images
echo.
echo Step 3: Starting %ENVIRONMENT% environment...
docker compose -f "%COMPOSE_FILE%" up -d --build --force-recreate

if !errorlevel! equ 0 (
    echo.
    echo ✅ %ENV_NAME% environment deployed successfully!
    echo.
    echo Services available at:
    echo !SERVICES_INFO!
) else (
    echo ❌ Deployment failed!
    exit /b 1
)

endlocal
