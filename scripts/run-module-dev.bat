@echo off
setlocal enabledelayedexpansion

REM Define project root as current directory
set "PROJECT_ROOT=%CD%"

REM Check if we are in the project root
if not exist "gradlew.bat" (
    echo Error: Script must be run from the project root!
    exit /b 1
)

REM Check if module path argument is provided
if "%1"=="" (
    echo Usage: scripts\run-module-dev.bat ^<module-path^> [additional-args]
    echo Example: scripts\run-module-dev.bat music-universe:music-data
    exit /b 1
)

set "MODULE_PATH=%1"

REM Extract module name from path
for /f "tokens=2 delims=:" %%a in ("%MODULE_PATH%") do set "MODULE_NAME=%%a"

echo Starting module %MODULE_PATH% with PROJECT_ROOT=%PROJECT_ROOT%

REM Load environment variables in order:
REM 1. env\docker\local\[module name].env
set "ENV_FILE_1=%PROJECT_ROOT%\env\docker\local\%MODULE_NAME%.env"
if exist "%ENV_FILE_1%" (
    echo Loading environment variables from %ENV_FILE_1%
    for /f "usebackq tokens=1,2 delims==" %%a in ("%ENV_FILE_1%") do (
        if not "%%a"=="" if not "%%a:~0,1%"=="#" (
            set "%%a=%%b"
        )
    )
) else (
    echo No main .env file found for module %MODULE_NAME% at path %ENV_FILE_1%
)

REM 2. env\docker\local\[module name].secrets.env
set "ENV_FILE_2=%PROJECT_ROOT%\env\docker\local\%MODULE_NAME%.secrets.env"
if exist "%ENV_FILE_2%" (
    echo Loading secrets from %ENV_FILE_2%
    for /f "usebackq tokens=1,2 delims==" %%a in ("%ENV_FILE_2%") do (
        if not "%%a"=="" if not "%%a:~0,1%"=="#" (
            set "%%a=%%b"
        )
    )
) else (
    echo No secrets file found for module %MODULE_NAME% at path %ENV_FILE_2%
)

REM 3. [module path]\dev.override.env
set "MODULE_DIR_PATH=%MODULE_PATH::=\%"
set "ENV_FILE_3=%PROJECT_ROOT%\%MODULE_DIR_PATH%\dev.override.env"
if exist "%ENV_FILE_3%" (
    echo Loading local overrides from %ENV_FILE_3%
    for /f "usebackq tokens=1,2 delims==" %%a in ("%ENV_FILE_3%") do (
        if not "%%a"=="" if not "%%a:~0,1%"=="#" (
            set "%%a=%%b"
        )
    )
) else (
    echo No local override file found for module %MODULE_NAME% at path %ENV_FILE_3%
)

REM Collect additional arguments (skip first argument which is MODULE_PATH)
set "ADDITIONAL_ARGS="
set "SKIP_FIRST=1"
for %%a in (%*) do (
    if defined SKIP_FIRST (
        set "SKIP_FIRST="
    ) else (
        if defined ADDITIONAL_ARGS (
            set "ADDITIONAL_ARGS=!ADDITIONAL_ARGS! %%a"
        ) else (
            set "ADDITIONAL_ARGS=%%a"
        )
    )
)

REM Run gradle with provided arguments
call gradlew.bat :%MODULE_PATH%:bootRun --args="--spring.profiles.active=dev" %ADDITIONAL_ARGS%

endlocal
