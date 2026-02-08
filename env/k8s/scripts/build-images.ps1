# Art Universe - Build Docker Images for K8s Deployment
# Usage: .\build-images.ps1 [-SkipGradleBuild]
# Images are built via Gradle dockerBuild tasks defined in each module.
# Run ./gradlew dockerBuildAll directly for the same result.

param(
    [switch]$SkipGradleBuild
)

$ErrorActionPreference = "Stop"
$ProjectRoot = Resolve-Path "$PSScriptRoot\..\..\..\"

Write-Host "Building Art Universe images for K8s deployment..." -ForegroundColor Cyan

if (-not $SkipGradleBuild) {
    Push-Location $ProjectRoot
    try {
        & .\gradlew.bat dockerBuildAll -x test
        if ($LASTEXITCODE -ne 0) { throw "Docker image build failed" }
    }
    finally {
        Pop-Location
    }
} else {
    Write-Host "Skipping Gradle build (using existing images)" -ForegroundColor Yellow
}

Write-Host "`nAll images built successfully!" -ForegroundColor Green
Write-Host "`nLocal images:" -ForegroundColor Cyan
docker images | Select-String "^mu-"
