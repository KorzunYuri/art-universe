@echo off
REM Define project root as current directory
set PROJECT_ROOT=%CD%

REM Check if we are in the project root
if not exist "gradlew.bat" (
    echo Error: Script must be run from the project root!
    exit /b 1
)

REM Check if module path argument is provided
if "%~1"=="" (
    echo Usage: run-local.bat ^<module-path^> [additional-args]
    echo Example: run-local.bat music-universe:music-data
    exit /b 1
)

set MODULE_PATH=%~1
shift

REM Extract module name from path
for /f "tokens=2 delims=:" %%a in ("%MODULE_PATH%") do set MODULE_NAME=%%a

REM Replace : with \ to get module directory path
set MODULE_DIR=%MODULE_PATH::=\%

REM Define path to module's .env file
set ENV_FILE=%PROJECT_ROOT%\%MODULE_DIR%\.env

REM Load environment variables from module's .env file if it exists
if exist "%ENV_FILE%" (
    echo Loading environment variables from %ENV_FILE%
    for /f "tokens=*" %%a in ('type "%ENV_FILE%" ^| findstr /v "^#"') do (
        set %%a
    )
) else (
    echo No .env file found for module %MODULE_NAME% at path %ENV_FILE%
)

echo Starting module %MODULE_PATH% with PROJECT_ROOT=%PROJECT_ROOT%

REM Run gradle with provided arguments
call gradlew.bat ":%MODULE_PATH%:bootRun" --args="--spring.profiles.active=local" %*
