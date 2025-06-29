@echo off
REM Cross-platform Docker stop script
REM Works in Windows Command Prompt

setlocal enabledelayedexpansion

REM Function to show usage
if "%1"=="" (
    echo ❌ Error: Environment parameter is required
    echo.
    echo Usage: %0 ^<environment^>
    echo.
    echo Available environments:
    echo   local - Stop local environment
    echo   prod  - Stop production environment
    echo   all   - Stop both environments
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

echo === Art Universe Docker Stop ===
echo Project root: %PROJECT_ROOT%
echo Environment: %ENVIRONMENT%
echo.

if "%ENVIRONMENT%"=="local" (
    echo Stopping local environment...
    docker compose -f "%PROJECT_ROOT%\env\docker\local\docker-compose.yml" stop 2>nul || echo Local environment not running
) else if "%ENVIRONMENT%"=="prod" (
    echo Stopping production environment...
    docker compose -f "%PROJECT_ROOT%\env\docker\prod\docker-compose.yml" stop 2>nul || echo Production environment not running
) else if "%ENVIRONMENT%"=="all" (
    echo Stopping local environment...
    docker compose -f "%PROJECT_ROOT%\env\docker\local\docker-compose.yml" stop 2>nul || echo Local environment not running
    echo Stopping production environment...
    docker compose -f "%PROJECT_ROOT%\env\docker\prod\docker-compose.yml" stop 2>nul || echo Production environment not running
)

echo.
echo ✅ Stop completed!

endlocal
