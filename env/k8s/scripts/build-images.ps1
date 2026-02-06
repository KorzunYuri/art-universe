# Art Universe - Build Docker Images for K8s Local Deployment
# Usage: .\build-images.ps1 [-SkipGradleBuild] [-SkipLayerExtraction]

param(
    [switch]$SkipGradleBuild,
    [switch]$SkipLayerExtraction
)

$ErrorActionPreference = "Stop"
$ProjectRoot = Resolve-Path "$PSScriptRoot\..\..\..\"

Write-Host "Building Art Universe images for K8s local deployment..." -ForegroundColor Cyan
Write-Host "Project root: $ProjectRoot" -ForegroundColor Gray

# Gradle modules that need bootJar and extractLayers
$gradleModules = @(
    ":music:data:raw:lastfm:lastfm-rest-api",
    ":music:data:raw:lastfm:etl:lastfm-etl-rest-api",
    ":music:data:raw:lastfm:etl:lastfm-calls-generator",
    ":music:data:raw:lastfm:etl:lastfm-calls-performer",
    ":music:data:raw:lastfm:etl:lastfm-response-parser",
    ":music:data:master",
    ":music:quiz"
)

# Build Gradle project and extract layers (unless skipped)
if (-not $SkipGradleBuild) {
    Write-Host "`nBuilding Gradle project and extracting layers..." -ForegroundColor Yellow
    Push-Location $ProjectRoot
    try {
        # Build all modules
        $buildTasks = ($gradleModules | ForEach-Object { "${_}:build" }) -join " "
        & .\gradlew.bat $buildTasks.Split(" ") -x test
        if ($LASTEXITCODE -ne 0) { throw "Gradle build failed" }

        # Extract layers for all modules (unless skipped)
        if (-not $SkipLayerExtraction) {
            Write-Host "`nCleaning old docker-layers directories..." -ForegroundColor Yellow
            # Module paths for cleanup
            $modulePaths = @(
                "music\data\raw\lastfm\lastfm-rest-api",
                "music\data\raw\lastfm\etl\lastfm-etl-rest-api",
                "music\data\raw\lastfm\etl\lastfm-calls-generator",
                "music\data\raw\lastfm\etl\lastfm-calls-performer",
                "music\data\raw\lastfm\etl\lastfm-response-parser",
                "music\data\master",
                "music\quiz"
            )
            foreach ($modulePath in $modulePaths) {
                $layersDir = Join-Path $ProjectRoot "$modulePath\build\docker-layers"
                if (Test-Path $layersDir) {
                    Remove-Item -Recurse -Force $layersDir
                    Write-Host "  Removed: $layersDir" -ForegroundColor Gray
                }
            }

            Write-Host "`nExtracting Spring Boot layers..." -ForegroundColor Yellow
            $extractTasks = ($gradleModules | ForEach-Object { "${_}:extractLayers" }) -join " "
            & .\gradlew.bat $extractTasks.Split(" ")
            if ($LASTEXITCODE -ne 0) { throw "Layer extraction failed" }
        }
    }
    finally {
        Pop-Location
    }
}

# Also build non-bootJar modules (liquibase, UI)
if (-not $SkipGradleBuild) {
    Write-Host "`nBuilding additional modules..." -ForegroundColor Yellow
    Push-Location $ProjectRoot
    try {
        & .\gradlew.bat ":music:data:raw:lastfm:migrations:lastfm-liquibase-service:build" -x test
        if ($LASTEXITCODE -ne 0) { throw "Liquibase build failed" }
    }
    finally {
        Pop-Location
    }
}

# Service definitions for Docker build
$services = @(
    @{ Path = "music\data\raw\lastfm\migrations\lastfm-liquibase-service"; Image = "mu-lastfm-liquibase" },
    @{ Path = "music\data\raw\lastfm\lastfm-rest-api"; Image = "mu-lastfm-rest-api" },
    @{ Path = "music\data\raw\lastfm\etl\lastfm-etl-rest-api"; Image = "mu-lastfm-etl-rest-api" },
    @{ Path = "music\data\raw\lastfm\etl\lastfm-calls-generator"; Image = "mu-lastfm-calls-generator" },
    @{ Path = "music\data\raw\lastfm\etl\lastfm-calls-performer"; Image = "mu-lastfm-calls-performer" },
    @{ Path = "music\data\raw\lastfm\etl\lastfm-response-parser"; Image = "mu-lastfm-response-parser" },
    @{ Path = "music\data\master"; Image = "mu-music-data" },
    @{ Path = "music\quiz"; Image = "mu-music-quiz" },
    @{
        Path = "music\ui"
        Image = "mu-music-ui"
        Dockerfile = "Dockerfile"
        # UI build args for K8s local deployment (same ports as Docker Compose)
        BuildArgs = @(
            "--build-arg", "VITE_MURAW_LASTFM_READ_API_HOST=localhost",
            "--build-arg", "VITE_MURAW_LASTFM_READ_API_EXTERNAL_PORT=9084",
            "--build-arg", "VITE_MURAW_LASTFM_WRITE_API_HOST=localhost",
            "--build-arg", "VITE_MURAW_LASTFM_WRITE_API_EXTERNAL_PORT=9085",
            "--build-arg", "VITE_MU_DATA_APP_HOST=localhost",
            "--build-arg", "VITE_MU_DATA_APP_EXTERNAL_PORT=9082",
            "--build-arg", "VITE_MU_QUIZ_APP_HOST=localhost",
            "--build-arg", "VITE_MU_QUIZ_APP_EXTERNAL_PORT=9083"
        )
    }
)

$successful = @()
$failed = @()

Write-Host "`nBuilding Docker images..." -ForegroundColor Yellow

foreach ($service in $services) {
    $servicePath = Join-Path $ProjectRoot $service.Path
    $dockerfile = if ($service.Dockerfile) { $service.Dockerfile } else { "Dockerfile.local" }
    $imageName = "$($service.Image):latest"

    Write-Host "`nBuilding $imageName..." -ForegroundColor Yellow
    Write-Host "  Context: $servicePath" -ForegroundColor Gray
    Write-Host "  Dockerfile: $dockerfile" -ForegroundColor Gray

    try {
        $buildArgs = @("-t", $imageName, "-f", "$servicePath\$dockerfile")
        if ($service.BuildArgs) {
            $buildArgs += $service.BuildArgs
        }
        $buildArgs += $servicePath

        docker build @buildArgs
        if ($LASTEXITCODE -ne 0) { throw "Docker build failed" }
        $successful += $imageName
        Write-Host "  Success!" -ForegroundColor Green
    }
    catch {
        $failed += @{ Image = $imageName; Error = $_.Exception.Message }
        Write-Host "  Failed: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "Build Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

if ($successful.Count -gt 0) {
    Write-Host "`nSuccessfully built:" -ForegroundColor Green
    $successful | ForEach-Object { Write-Host "  - $_" -ForegroundColor Green }
}

if ($failed.Count -gt 0) {
    Write-Host "`nFailed to build:" -ForegroundColor Red
    $failed | ForEach-Object { Write-Host "  - $($_.Image): $($_.Error)" -ForegroundColor Red }
    exit 1
}

Write-Host "`nAll images built successfully!" -ForegroundColor Green
Write-Host "`nLocal images:" -ForegroundColor Cyan
docker images | Select-String "^mu-"
