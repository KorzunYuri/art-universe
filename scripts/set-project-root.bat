@echo off
REM Script to set PROJECT_ROOT environment variable for IntelliJ IDEA run configurations
REM This script works in Windows Command Prompt and PowerShell

REM Get the directory where this batch file is located
set "SCRIPT_DIR=%~dp0"
REM Remove trailing backslash
set "SCRIPT_DIR=%SCRIPT_DIR:~0,-1%"

REM Set PROJECT_ROOT to one level up from scripts directory (project root)
for %%i in ("%SCRIPT_DIR%\..") do set "PROJECT_ROOT=%%~fi"

echo Windows environment detected
echo PROJECT_ROOT set to: %PROJECT_ROOT%

REM Create a temporary file with the variable for IntelliJ IDEA in project root
echo PROJECT_ROOT=%PROJECT_ROOT%> "%PROJECT_ROOT%\.project-root.env"
echo Environment variable saved to .project-root.env

REM Set the environment variable for current session
setx PROJECT_ROOT "%PROJECT_ROOT%" >nul 2>&1
if %errorlevel% equ 0 (
    echo Environment variable set permanently for current user
) else (
    echo Warning: Could not set permanent environment variable
    echo Use: set PROJECT_ROOT=%PROJECT_ROOT%
)

echo To use in current session, run: set PROJECT_ROOT=%PROJECT_ROOT%
