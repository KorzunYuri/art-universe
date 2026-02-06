# Art Universe - Kubernetes Deployment

This directory contains Kubernetes manifests for deploying Art Universe using Kustomize.

Two overlays are available:
- **Local**: Full stack with containerized PostgreSQL databases
- **Prod**: Applications only, connecting to external databases

## Prerequisites

1. **Docker Desktop** with Kubernetes enabled
2. **kubectl** configured for Docker Desktop cluster
3. **NGINX Ingress Controller** (installed automatically by deploy scripts)

## Quick Start (Local)

### 1. Build Images

**Windows (PowerShell):**
```powershell
.\scripts\build-images.ps1
```

**Linux/MacOS:**
```bash
chmod +x scripts/*.sh
./scripts/build-images.sh
```

### 2. Create Namespaces and Secrets

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

### 3. Deploy

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

## Quick Start (Prod)

The prod overlay connects to external databases instead of running PostgreSQL in containers.

### 1. Build Images

Same as local (images are tagged `:latest` for both overlays):
```powershell
.\scripts\build-images.ps1
```

### 2. Create Secrets

```powershell
cp overlays\prod\secrets\secrets.yaml.template overlays\prod\secrets\secrets.yaml
# Edit secrets.yaml with your production credentials
```

See `overlays/prod/secrets/README.md` for details.

### 3. Deploy

**Windows (PowerShell):**
```powershell
.\scripts\deploy-prod.ps1
```

**Linux/MacOS:**
```bash
./scripts/deploy-prod.sh
```

The deploy script will:
1. Apply namespaces
2. Create Grafana ConfigMaps from shared provisioning files
3. Apply the prod Kustomize overlay
4. Apply secrets (if `secrets.yaml` exists)
5. Wait for migrations and applications

### External Database Configuration

By default, the prod overlay points to `host.docker.internal` (the Windows host from Docker Desktop K8s). To change the database host, edit `overlays/prod/external-databases.yaml`:

```yaml
spec:
  type: ExternalName
  externalName: host.docker.internal   # Change this to your DB host
```

For IP-based databases (no DNS name), see the comments in `external-databases.yaml` for the Service+Endpoints alternative.

## Namespace Structure

| Namespace | Purpose |
|-----------|---------|
| `mu-data` | PostgreSQL databases (local) or ExternalName services (prod), migration jobs |
| `mu-lastfm` | LastFM ETL pipeline services |
| `mu-apps` | Core applications (music-data, music-quiz) |
| `mu-frontend` | UI and NGINX Ingress |
| `art-universe-monitoring` | Prometheus, Grafana, Zipkin |

## Access Points

### Local

| Service | URL |
|---------|-----|
| Music UI | http://localhost:4000 |
| Music Data API | http://localhost:9082 |
| Music Quiz API | http://localhost:9083 |
| LastFM REST API | http://localhost:9084 |
| LastFM ETL REST API | http://localhost:9085 |
| Grafana | http://localhost:30000 |
| Prometheus | http://localhost:30090 |

### Prod

| Service | URL |
|---------|-----|
| Music UI | http://localhost:3000 |
| Music Data API | http://localhost:8082 |
| Music Quiz API | http://localhost:8083 |
| LastFM REST API | http://localhost:8084 |
| LastFM ETL REST API | http://localhost:8085 |
| Grafana | http://localhost:30000 |
| Prometheus | http://localhost:30090 |

## Teardown

### Local
```powershell
kubectl delete -k overlays\local

# To also remove persistent data (database volumes) and namespaces:
kubectl delete pvc --all -n mu-data
kubectl delete namespace mu-data mu-lastfm mu-apps mu-frontend art-universe-monitoring
```

### Prod
```powershell
kubectl delete -k overlays\prod

# To remove namespaces:
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

# Render Kustomize output without applying
kubectl kustomize overlays/local
kubectl kustomize overlays/prod
```

## Architecture Notes

### Local vs Prod Overlay

- **Local** includes the full `base/` (with PostgreSQL StatefulSets in `data/`), plus LoadBalancer services on 9xxx/4000 ports
- **Prod** selectively includes base sub-kustomizations (skipping `data/` StatefulSets), adds ExternalName services for database connectivity, and uses LoadBalancer services on 8xxx/3000 ports

### Shared Resources

Database init scripts, Grafana dashboards, and datasource configurations are centralized at `env/docker/common/` and shared by both Docker Compose and Kubernetes deployments:

- **DB init scripts** (`env/docker/common/db/`) - Mounted directly in Docker Compose; created as ConfigMaps by deploy scripts for K8s (local overlay only)
- **Grafana provisioning** (`env/docker/common/grafana/provisioning/`) - Mounted directly in Docker Compose; created as ConfigMaps by deploy scripts for K8s

### Image Tags

All images are tagged `:latest` in base manifests. The `build-images` scripts build and tag images accordingly. Both overlays use the same images.

## Directory Structure

```
env/k8s/
├── README.md                 # This file
├── scripts/
│   ├── build-images.ps1      # Build Docker images (Windows)
│   ├── build-images.sh       # Build Docker images (Linux/MacOS)
│   ├── deploy-local.ps1      # Deploy to local K8s (Windows)
│   ├── deploy-local.sh       # Deploy to local K8s (Linux/MacOS)
│   ├── deploy-prod.ps1       # Deploy to prod K8s (Windows)
│   └── deploy-prod.sh        # Deploy to prod K8s (Linux/MacOS)
├── base/
│   ├── kustomization.yaml    # Main base kustomization
│   ├── namespaces.yaml       # Namespace definitions
│   ├── data/                 # Database StatefulSets + migration jobs
│   ├── lastfm/               # LastFM ETL services
│   ├── apps/                 # Core applications
│   ├── frontend/             # UI and Ingress
│   └── monitoring/           # Prometheus, Grafana, Zipkin
└── overlays/
    ├── local/
    │   ├── kustomization.yaml
    │   ├── external-services.yaml  # LoadBalancer services (9xxx/4000 ports)
    │   └── secrets/                # Environment secrets (gitignored)
    └── prod/
        ├── kustomization.yaml
        ├── external-databases.yaml   # ExternalName services for DB connectivity
        ├── external-services.yaml    # LoadBalancer services (8xxx/3000 ports)
        └── secrets/                  # Prod secrets (gitignored)
            ├── secrets.yaml.template
            └── README.md
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

### ExternalName resolution (prod)
Verify external DB connectivity: `kubectl exec -n mu-lastfm deployment/lastfm-rest-api -- nslookup host.docker.internal`
