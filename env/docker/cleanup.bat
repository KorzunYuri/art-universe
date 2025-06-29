@echo off
REM Cross-platform Docker cleanup script
REM Works in Windows Command Prompt

setlocal enabledelayedexpansion

REM Function to show usage
if "%1"=="" (
    echo ❌ Error: Environment parameter is required
    echo.
    echo Usage: %0 ^<environment^>
    echo.
    echo Available environments:
    echo   local - Clean local environment
    echo   prod  - Clean production environment
    echo   all   - Clean both environments
    echo.
    echo Examples:
    echo   %0 local
    echo   %0 prod
    echo   %0 all
    exit /b 1
)

set "ENVIRONMENT=%1"

REM Validate environment parameter
if not "%ENVIRONMENT%"=="local" if not "%ENVIRONMENT%"=="prod" if not "%ENVIRONMENT%"=="all" (
    echo ❌ Error: Invalid environment '%ENVIRONMENT%'
    echo.
    echo Available environments: local, prod, all
    exit /b 1
)

REM Get the project root directory (two levels up from this script)
set "SCRIPT_DIR=%~dp0"
for %%i in ("%SCRIPT_DIR%..\..\") do set "PROJECT_ROOT=%%~fi"

echo === Art Universe Docker Cleanup ===
echo Project root: %PROJECT_ROOT%
echo Environment: %ENVIRONMENT%
echo.

if "%ENVIRONMENT%"=="local" (
    echo Step 1: Cleaning local environment...
    docker compose -f "%PROJECT_ROOT%\env\docker\local\docker-compose.yml" down --remove-orphans --rmi all 2>nul || echo Local environment not running or already cleaned
) else if "%ENVIRONMENT%"=="prod" (
    echo Step 2: Cleaning production environment...
    docker compose -f "%PROJECT_ROOT%\env\docker\prod\docker-compose.yml" down --remove-orphans --rmi all 2>nul || echo Production environment not running or already cleaned
) else if "%ENVIRONMENT%"=="all" (
    echo Step 1: Cleaning local environment...
    docker compose -f "%PROJECT_ROOT%\env\docker\local\docker-compose.yml" down --remove-orphans --rmi all 2>nul || echo Local environment not running or already cleaned
    echo Step 2: Cleaning production environment...
    docker compose -f "%PROJECT_ROOT%\env\docker\prod\docker-compose.yml" down --remove-orphans --rmi all 2>nul || echo Production environment not running or already cleaned
)

echo.
echo Step 3: Cleaning up dangling images...
docker image prune -f 2>nul || echo No dangling images to remove

echo.
echo ✅ Cleanup completed!
echo.
echo Note: Volumes are preserved. To remove them manually, run:
echo   docker volume prune -f
echo   or
echo   docker system prune -a --volumes

endlocal
