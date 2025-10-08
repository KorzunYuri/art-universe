@echo off
REM Cross-platform Docker deployment script
REM Works in Windows Command Prompt

setlocal enabledelayedexpansion

REM Function to show usage
if "%1"=="" (
    echo [ERROR] Environment parameter is required
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
    echo [ERROR] Invalid environment '%ENVIRONMENT%'
    echo.
    echo Available environments: local, prod
    exit /b 1
)

REM Get the project root directory (two levels up from this script)
set "SCRIPT_DIR=%~dp0"
for %%i in ("%SCRIPT_DIR%..\..\") do set "PROJECT_ROOT=%%~fi"

REM Set environment-specific variables
set "COMPOSE_FILE=%PROJECT_ROOT%\env\docker\%ENVIRONMENT%\docker-compose.yml"
REM Source the environment file to get the ports
for /f "tokens=1,* delims==" %%a in (%PROJECT_ROOT%\env\docker\%ENVIRONMENT%\.env) do (
    set "%%a=%%b"
)

echo === Art Universe %ENVIRONMENT% Environment Deployment ===
echo Project root: %PROJECT_ROOT%
echo Gradle command: gradlew.bat
echo Environment: %ENVIRONMENT%
echo.

REM Stop and remove existing containers and images
echo Step 1: Stopping and removing existing containers...
docker compose -f "%COMPOSE_FILE%" down --remove-orphans

REM Build the project only for local environment
if "%ENVIRONMENT%"=="local" (
    echo.
    echo Step 2: Building project for local environment...
    cd /d "%PROJECT_ROOT%"
    call gradlew.bat clean build extractLayers -x test

    if !errorlevel! neq 0 (
        echo [ERROR] Build failed! Aborting deployment.
        exit /b 1
    )
    
) else (
    echo.
    echo Step 2: Skipping Gradle build for production environment (will build inside Docker)
)

REM Start with rebuilding images
echo.
echo Step 3: Starting %ENVIRONMENT% environment...
docker compose -f "%COMPOSE_FILE%" up -d --build --force-recreate
set "DEPLOY_ERROR=%ERRORLEVEL%"

if "%DEPLOY_ERROR%"=="0" (
    echo.
    echo [OK] %ENVIRONMENT% environment deployed successfully!
    echo.
    echo Services available at:
    
    REM Output services info line by line to avoid dash interpretation
    echo   - LastFM REST API: http://localhost:!MURAW_LASTFM_REST_API_EXTERNAL_PORT! (actuator: !MURAW_LASTFM_REST_API_ACTUATOR_EXTERNAL_PORT!)
    echo   - LastFM ETL REST API: http://localhost:!MURAW_LASTFM_ETL_REST_API_EXTERNAL_PORT! (actuator: !MURAW_LASTFM_ETL_REST_API_ACTUATOR_EXTERNAL_PORT!)
    echo   - LastFM Calls Generator: actuator http://localhost:!MURAW_LASTFM_CALLS_GENERATOR_ACTUATOR_EXTERNAL_PORT!
    echo   - LastFM Calls Performer: actuator http://localhost:!MURAW_LASTFM_CALLS_PERFORMER_ACTUATOR_EXTERNAL_PORT!
    echo   - LastFM Response Parser: actuator http://localhost:!MURAW_LASTFM_RESPONSE_PARSER_ACTUATOR_EXTERNAL_PORT!
    echo   - Music Data: http://localhost:!MU_DATA_APP_EXTERNAL_PORT! (actuator: !MU_DATA_ACTUATOR_EXTERNAL_PORT!)
    echo   - Music Quiz: http://localhost:!MU_QUIZ_APP_EXTERNAL_PORT! (actuator: !MU_QUIZ_ACTUATOR_EXTERNAL_PORT!)
    echo   - UI: http://localhost:!MU_UI_EXTERNAL_PORT!
    echo   - Adminer: http://localhost:!DB_ADMINER_PORT!
    echo   - Prometheus: http://localhost:!PROMETHEUS_PORT!
    echo   - Grafana: http://localhost:!GRAFANA_PORT!
    echo.
    if "%ENVIRONMENT%"=="prod" (
        echo Note: Applications connect to external databases on host machine
    )
) else (
    echo [ERROR] Deployment failed with exit code %DEPLOY_ERROR%
    exit /b %DEPLOY_ERROR%
)

endlocal
