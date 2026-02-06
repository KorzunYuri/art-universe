# Art Universe - Deploy to Production Kubernetes
# Connects to external databases (no containerized PostgreSQL)
# Usage: .\deploy-prod.ps1 [-SkipIngressCheck] [-WaitTimeout 600]

param(
    [switch]$SkipIngressCheck,
    [int]$WaitTimeout = 600
)

$ErrorActionPreference = "Stop"
$K8sDir = Resolve-Path "$PSScriptRoot\.."
$ProjectRoot = Resolve-Path "$K8sDir\..\.."

Write-Host "Deploying Art Universe to production Kubernetes..." -ForegroundColor Cyan

# Verify kubectl context
$context = kubectl config current-context
if ($context -ne "docker-desktop") {
    Write-Host "Warning: Current context is '$context', not 'docker-desktop'" -ForegroundColor Yellow
    $response = Read-Host "Continue? (y/n)"
    if ($response -ne "y") {
        Write-Host "Aborted." -ForegroundColor Red
        exit 1
    }
}

# Check secrets
$secretsFile = "$K8sDir\overlays\prod\secrets\secrets.yaml"
if (-not (Test-Path $secretsFile)) {
    Write-Host "`nWarning: secrets.yaml not found at $secretsFile" -ForegroundColor Yellow
    Write-Host "  Copy secrets.yaml.template and fill in your credentials." -ForegroundColor Yellow
    Write-Host "  See secrets/README.md for setup instructions." -ForegroundColor Yellow
    $response = Read-Host "Continue without secrets? (y/n)"
    if ($response -ne "y") {
        Write-Host "Aborted." -ForegroundColor Red
        exit 1
    }
}

# Install NGINX Ingress if not present
if (-not $SkipIngressCheck) {
    $ingressNs = kubectl get namespace ingress-nginx --ignore-not-found -o name
    if (-not $ingressNs) {
        Write-Host "`nInstalling NGINX Ingress Controller..." -ForegroundColor Yellow
        kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.9.4/deploy/static/provider/cloud/deploy.yaml

        Write-Host "Waiting for Ingress controller to be ready..." -ForegroundColor Yellow
        kubectl wait --namespace ingress-nginx --for=condition=ready pod --selector=app.kubernetes.io/component=controller --timeout=120s
    }
    else {
        Write-Host "NGINX Ingress Controller already installed." -ForegroundColor Green
    }
}

# Apply namespaces first (needed before ConfigMaps and overlay resources)
Write-Host "`nCreating namespaces..." -ForegroundColor Yellow
kubectl apply -f "$K8sDir\base\namespaces.yaml"
Write-Host "Namespaces created." -ForegroundColor Green

# Create Grafana ConfigMaps from shared provisioning files
Write-Host "`nCreating Grafana ConfigMaps from shared provisioning files..." -ForegroundColor Yellow
$dashboardsDir = "$ProjectRoot\env\docker\common\grafana\provisioning\dashboards"
kubectl create configmap grafana-dashboards -n art-universe-monitoring `
    --from-file="dashboard.yml=$dashboardsDir\dashboard.yml" `
    --from-file="lastfm_raw_data_dashboard.json=$dashboardsDir\lastfm_raw_data_dashboard.json" `
    --from-file="lastfm_database_metrics_dashboard.json=$dashboardsDir\lastfm_database_metrics_dashboard.json" `
    --from-file="system_metrics_dashboard.json=$dashboardsDir\system_metrics_dashboard.json" `
    --from-file="repository_performance_dashboard.json=$dashboardsDir\repository_performance_dashboard.json" `
    --from-file="rest_api_performance_dashboard.json=$dashboardsDir\rest_api_performance_dashboard.json" `
    --dry-run=client -o yaml | kubectl apply -f -
kubectl create configmap grafana-datasources -n art-universe-monitoring `
    --from-file="datasource.yml=$ProjectRoot\env\docker\common\grafana\provisioning\datasources\datasource.yml" `
    --dry-run=client -o yaml | kubectl apply -f -
Write-Host "Grafana ConfigMaps created." -ForegroundColor Green

# Apply Kustomize overlay
Write-Host "`nApplying Kubernetes manifests (prod overlay)..." -ForegroundColor Yellow
kubectl apply -k "$K8sDir\overlays\prod"

# Apply secrets if present
if (Test-Path $secretsFile) {
    Write-Host "`nApplying secrets..." -ForegroundColor Yellow
    kubectl apply -f $secretsFile
    Write-Host "Secrets applied." -ForegroundColor Green
}

# Wait for migrations
Write-Host "`nWaiting for migrations to complete..." -ForegroundColor Yellow
try {
    kubectl wait --for=condition=complete job/lastfm-liquibase-migration -n mu-data --timeout="${WaitTimeout}s"
    Write-Host "Migrations completed." -ForegroundColor Green
}
catch {
    Write-Host "Migration job not complete within timeout. Check: kubectl logs job/lastfm-liquibase-migration -n mu-data" -ForegroundColor Yellow
}

# Wait for applications
Write-Host "`nWaiting for applications to be ready..." -ForegroundColor Yellow
$apps = @(
    @{ Label = "app=music-data"; Namespace = "mu-apps" },
    @{ Label = "app=music-quiz"; Namespace = "mu-apps" },
    @{ Label = "app=lastfm-rest-api"; Namespace = "mu-lastfm" },
    @{ Label = "app=lastfm-etl-rest-api"; Namespace = "mu-lastfm" },
    @{ Label = "app=music-ui"; Namespace = "mu-frontend" }
)

foreach ($app in $apps) {
    try {
        kubectl wait --for=condition=ready pod -l $app.Label -n $app.Namespace --timeout="${WaitTimeout}s"
        Write-Host "  $($app.Label) ready." -ForegroundColor Green
    }
    catch {
        Write-Host "  $($app.Label) not ready within timeout." -ForegroundColor Yellow
    }
}

# Summary
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "Production Deployment Complete!" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

Write-Host "`nNamespace Status:" -ForegroundColor Yellow
kubectl get pods -n mu-data --no-headers 2>$null | ForEach-Object { Write-Host "  [mu-data] $_" }
kubectl get pods -n mu-lastfm --no-headers 2>$null | ForEach-Object { Write-Host "  [mu-lastfm] $_" }
kubectl get pods -n mu-apps --no-headers 2>$null | ForEach-Object { Write-Host "  [mu-apps] $_" }
kubectl get pods -n mu-frontend --no-headers 2>$null | ForEach-Object { Write-Host "  [mu-frontend] $_" }
kubectl get pods -n art-universe-monitoring --no-headers 2>$null | ForEach-Object { Write-Host "  [monitoring] $_" }

Write-Host "`nAccess Points:" -ForegroundColor Yellow
Write-Host "  - UI:                  http://localhost:3000" -ForegroundColor Green
Write-Host "  - Music Data API:      http://localhost:8082" -ForegroundColor Green
Write-Host "  - Music Quiz API:      http://localhost:8083" -ForegroundColor Green
Write-Host "  - LastFM REST API:     http://localhost:8084" -ForegroundColor Green
Write-Host "  - LastFM ETL REST API: http://localhost:8085" -ForegroundColor Green
Write-Host "  - Grafana:             http://localhost:30000" -ForegroundColor Green
Write-Host "  - Prometheus:          http://localhost:30090" -ForegroundColor Green
