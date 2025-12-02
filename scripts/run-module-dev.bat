@echo off
setlocal enabledelayedexpansion

REM Check if we are in the project root
if not exist "gradlew.bat" (
    echo Error: Script must be run from the project root!
    exit /b 1
)

REM Check if module path argument is provided
if "%1"=="" (
    echo Usage: scripts\run-module-dev.bat ^<module-path^> [additional-args]
    echo Example: scripts\run-module-dev.bat music:data:master
    exit /b 1
)

set "MODULE_PATH=%1"

REM Extract module name from path (get the last part after the last colon)
for %%a in ("%MODULE_PATH::=" "%") do set "MODULE_NAME=%%~a"

echo Starting module %MODULE_PATH%

REM Load environment variables in order:
REM 1. .project-root.env (REQUIRED)
set "PROJECT_ROOT_ENV_FILE=.project-root.env"
if not exist "%PROJECT_ROOT_ENV_FILE%" (
    echo Error: .project-root.env file not found!
    echo Please run: scripts\set-project-root.bat
    exit /b 1
)
echo Loading PROJECT_ROOT from %PROJECT_ROOT_ENV_FILE%
for /f "usebackq tokens=1,2 delims==" %%a in ("%PROJECT_ROOT_ENV_FILE%") do (
    if not "%%a"=="" if not "%%a:~0,1%"=="#" (
        set "%%a=%%b"
    )
)

if not defined PROJECT_ROOT (
    echo Error: PROJECT_ROOT not set in .project-root.env
    exit /b 1
)

echo Using PROJECT_ROOT=%PROJECT_ROOT%

REM 2. env\docker\common\*.env (OPTIONAL - all common configuration files)
set "COMMON_ENV_DIR=%PROJECT_ROOT%\env\docker\common"
if exist "%COMMON_ENV_DIR%" (
    echo Loading common configuration files from %COMMON_ENV_DIR%
    for %%f in ("%COMMON_ENV_DIR%\*.env") do (
        if exist "%%f" (
            echo   - Loading %%~nxf
            for /f "usebackq tokens=1,2 delims==" %%a in ("%%f") do (
                if not "%%a"=="" if not "%%a:~0,1%"=="#" (
                    set "%%a=%%b"
                )
            )
        )
    )
) else (
    echo No common directory found at %COMMON_ENV_DIR%
)

REM 3. env\docker\local\[module name].env (OPTIONAL - local environment)
set "ENV_FILE_LOCAL=%PROJECT_ROOT%\env\docker\local\%MODULE_NAME%.env"
if exist "%ENV_FILE_LOCAL%" (
    echo Loading local environment from %ENV_FILE_LOCAL%
    for /f "usebackq tokens=1,2 delims==" %%a in ("%ENV_FILE_LOCAL%") do (
        if not "%%a"=="" if not "%%a:~0,1%"=="#" (
            set "%%a=%%b"
        )
    )
) else (
    echo No local .env file found for module %MODULE_NAME% (checked: %ENV_FILE_LOCAL%)
)

REM 4. env\docker\local\[module name].secrets.env (OPTIONAL - secrets)
set "ENV_FILE_SECRETS=%PROJECT_ROOT%\env\docker\local\%MODULE_NAME%.secrets.env"
if exist "%ENV_FILE_SECRETS%" (
    echo Loading secrets from %ENV_FILE_SECRETS%
    for /f "usebackq tokens=1,2 delims==" %%a in ("%ENV_FILE_SECRETS%") do (
        if not "%%a"=="" if not "%%a:~0,1%"=="#" (
            set "%%a=%%b"
        )
    )
) else (
    echo No secrets file found for module %MODULE_NAME% (checked: %ENV_FILE_SECRETS%)
)

REM 5. [module path]\dev.override.env (OPTIONAL - development overrides)
set "MODULE_DIR_PATH=%MODULE_PATH::=\%"
set "ENV_FILE_OVERRIDE=%PROJECT_ROOT%\%MODULE_DIR_PATH%\dev.override.env"
if exist "%ENV_FILE_OVERRIDE%" (
    echo Loading development overrides from %ENV_FILE_OVERRIDE%
    for /f "usebackq tokens=1,2 delims==" %%a in ("%ENV_FILE_OVERRIDE%") do (
        if not "%%a"=="" if not "%%a:~0,1%"=="#" (
            set "%%a=%%b"
        )
    )
) else (
    echo No development override file found for module %MODULE_NAME% (checked: %ENV_FILE_OVERRIDE%)
)

echo =========================================
echo Starting module with Spring profile: dev
echo =========================================

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
