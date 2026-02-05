# Art Universe - Kubernetes Deployment

This directory contains Kubernetes manifests for deploying Art Universe using Kustomize.

## Prerequisites

1. **Docker Desktop** with Kubernetes enabled
2. **kubectl** configured for Docker Desktop cluster
3. **NGINX Ingress Controller** (install command below)

## Quick Start

### 1. Install NGINX Ingress Controller

```powershell
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.9.4/deploy/static/provider/cloud/deploy.yaml

# Wait for controller to be ready
kubectl wait --namespace ingress-nginx --for=condition=ready pod --selector=app.kubernetes.io/component=controller --timeout=120s
```

### 2. Build Local Images

**Windows (PowerShell):**
```powershell
.\scripts\build-images.ps1
```

**Linux/MacOS:**
```bash
chmod +x scripts/*.sh
./scripts/build-images.sh
```

### 3. Create Namespaces and Secrets

First, create the namespaces (secrets must be applied to existing namespaces):
```bash
kubectl apply -f base/namespaces.yaml
```

Then copy the secrets template and fill in your values:
```powershell
cp overlays\local\secrets\secrets.yaml.template overlays\local\secrets\secrets.yaml
# Edit secrets.yaml with your values (use same passwords as Docker Compose .secrets.env files)
```

Apply the secrets:
```bash
kubectl apply -f overlays\local\secrets\secrets.yaml
```

### 4. Deploy

**Windows (PowerShell):**
```powershell
.\scripts\deploy-local.ps1
```

**Linux/MacOS:**
```bash
./scripts/deploy-local.sh
```

**Or manually:**
```bash
kubectl apply -k overlays/local
```

## Namespace Structure

| Namespace | Purpose |
|-----------|---------|
| `mu-data` | PostgreSQL databases and migration jobs |
| `mu-lastfm` | LastFM ETL pipeline services |
| `mu-apps` | Core applications (music-data, music-quiz) |
| `mu-frontend` | UI and NGINX Ingress |
| `art-universe-monitoring` | Prometheus, Grafana, Zipkin |

## Access Points (Local)

| Service | URL |
|---------|-----|
| Music UI | http://localhost/ |
| LastFM API | http://localhost/api/lastfm/ |
| Music Data API | http://localhost/api/music/ |
| Music Quiz API | http://localhost/api/quiz/ |
| Grafana | http://localhost:30000 |
| Prometheus | http://localhost:30090 |

## Useful Commands

```powershell
# View all resources
kubectl get all -A | Select-String "mu-|art-universe"

# Check pod status in a namespace
kubectl get pods -n mu-lastfm

# View logs
kubectl logs -n mu-lastfm deployment/lastfm-rest-api

# Describe a failing pod
kubectl describe pod -n mu-data postgres-lastfm-master-0

# Port-forward for debugging
kubectl port-forward -n mu-apps svc/music-data 8082:8080

# Delete all resources
kubectl delete -k overlays\local
```

## Directory Structure

```
env/k8s/
├── README.md                 # This file
├── scripts/
│   ├── build-images.ps1      # Build Docker images (Windows)
│   ├── build-images.sh       # Build Docker images (Linux/MacOS)
│   ├── deploy-local.ps1      # Deploy to local K8s (Windows)
│   └── deploy-local.sh       # Deploy to local K8s (Linux/MacOS)
├── base/
│   ├── kustomization.yaml    # Main base kustomization
│   ├── namespaces.yaml       # Namespace definitions
│   ├── data/                 # Database StatefulSets
│   ├── lastfm/               # LastFM ETL services
│   ├── apps/                 # Core applications
│   ├── frontend/             # UI and Ingress
│   └── monitoring/           # Prometheus, Grafana, Zipkin
└── overlays/
    └── local/
        ├── kustomization.yaml
        └── secrets/          # Environment secrets (gitignored)
```

## Troubleshooting

### Pods stuck in Pending
Check if PVCs are bound: `kubectl get pvc -n mu-data`

### Pods stuck in Init
Check init container logs: `kubectl logs -n mu-lastfm <pod> -c wait-for-db`

### Database connection issues
Verify service DNS: `kubectl exec -n mu-lastfm deployment/lastfm-rest-api -- nslookup postgres-lastfm-master.mu-data.svc.cluster.local`

### Image pull errors
Ensure images are built locally and `imagePullPolicy: Never` is set.
