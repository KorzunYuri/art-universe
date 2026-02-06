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
# Create ConfigMaps from shared init scripts (required before first deploy)
kubectl apply -f base/namespaces.yaml
kubectl create configmap postgres-lastfm-master-init -n mu-data \
    --from-file="01-init.sh=../../docker/common/db/mu-raw-lastfm/initdb/01-init.sh" \
    --dry-run=client -o yaml | kubectl apply -f -
kubectl create configmap postgres-music-data-init -n mu-data \
    --from-file="01-init.sh=../../docker/common/db/mu/initdb/01-init.sh" \
    --dry-run=client -o yaml | kubectl apply -f -

# Apply manifests
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
| Music UI | http://localhost:4000 |
| Music Data API | http://localhost:9082 |
| Music Quiz API | http://localhost:9083 |
| LastFM REST API | http://localhost:9084 |
| LastFM ETL REST API | http://localhost:9085 |
| Grafana | http://localhost:30000 |
| Prometheus | http://localhost:30090 |

## Teardown

Remove all deployed resources:
```powershell
kubectl delete -k overlays\local
```

To also remove persistent data (database volumes) and namespaces:
```powershell
# Delete PVCs (this destroys all database data)
kubectl delete pvc --all -n mu-data

# Delete namespaces (removes any remaining resources)
kubectl delete namespace mu-data mu-lastfm mu-apps mu-frontend art-universe-monitoring
```

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
```

## Architecture Notes

### Shared Database Init Scripts

Database initialization scripts are centralized at `env/docker/common/db/` and shared by
both Docker Compose and Kubernetes deployments:

- `env/docker/common/db/mu-raw-lastfm/initdb/01-init.sh` - LastFM database init
- `env/docker/common/db/mu/initdb/01-init.sh` - Music Data database init

For Docker Compose, these are mounted directly into `/docker-entrypoint-initdb.d/`.
For Kubernetes, the deploy scripts create ConfigMaps from these files, which are then
mounted into the PostgreSQL pods.

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
        ├── external-services.yaml  # LoadBalancer services for local access
        └── secrets/                # Environment secrets (gitignored)
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
