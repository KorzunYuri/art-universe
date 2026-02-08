# Art Universe - Kubernetes Deployment

This directory contains Kubernetes manifests for deploying Art Universe using Kustomize.
The project is currently deployed to local k8s cluster in Docker desktop.

Three overlays are available:
- **Local**: Full stack with containerized PostgreSQL databases (fresh storage)
- **Local-Shared**: Same as Local, but reuses Docker Compose data volumes via hostPath
- **Prod**: Applications only, connecting to external databases

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


## Deployment

All overlays share the same workflow: build images, create secrets, deploy.

### Prerequisites

1. **Docker Desktop** with Kubernetes enabled
2. **kubectl** configured for Docker Desktop cluster
3. **NGINX Ingress Controller** (installed automatically by deploy scripts)

### 1. Build Images

All overlays use the same images, built via Gradle:

```bash
# From project root
./gradlew dockerBuildAll -x test

# Or build a single module
./gradlew :music:quiz:dockerBuild
```

Convenience wrapper scripts are also available at `scripts/build-images.ps1` and `scripts/build-images.sh`.

### 2. Create Secrets

Copy the secrets template for your overlay and fill in your values:

```powershell
# Local / Local-Shared (shared secrets)
cp overlays\local\secrets\secrets.yaml.template overlays\local\secrets\secrets.yaml
# Edit with your values (use same passwords as Docker Compose .secrets.env files)

# Prod
cp overlays\prod\secrets\secrets.yaml.template overlays\prod\secrets\secrets.yaml
# Edit with your production credentials (see overlays/prod/secrets/README.md)
```

### 3. Deploy

Each overlay has its own deploy script that handles namespaces, ConfigMaps, secrets, and Kustomize application:

- [Local](./scripts/deploy-local.sh)
- [Local-Shared](./scripts/deploy-local-shared.sh)
- [Prod](./scripts/deploy-prod.sh)

**Local-Shared note:** PostgreSQL requires exclusive access — stop Docker Compose before deploying (`docker compose down` in `env/docker/local/`).

### External Database Configuration (Prod)

By default, the prod overlay points to `host.docker.internal` (the Windows host from Docker Desktop K8s). 
To change the database host, edit `overlays/prod/external-databases.yaml`. 
For IP-based databases (no DNS name), see the comments in that file for the Service+Endpoints alternative.

## Shared Volumes (Local-Shared)

The `local-shared` overlay reuses Docker Compose named volumes in Kubernetes via `hostPath` PersistentVolumes. This lets you switch between Docker Compose and K8s without losing data.

### Switching Between Environments

**Docker Compose → K8s:**
```powershell
cd env/docker/local && docker compose down
cd ../../../env/k8s && .\scripts\deploy-local-shared.ps1
```

**K8s → Docker Compose:**
```powershell
cd env/k8s && kubectl delete -k overlays\local-shared
cd ../docker/local && docker compose up -d
```

### Volume Mapping

| Docker Compose Volume | K8s PersistentVolume |
|---|---|
| `local__lastfm__db-postgres-data` | `shared-lastfm-master-data` |
| `local__lastfm__db-postgres-replica-data` | `shared-lastfm-replica-data` |
| `local__mu-data__db-postgres-data` | `shared-music-data-db` |
| `local__mu__prometheus-data` | `shared-prometheus-data` |
| `local__mu__grafana-data` | `shared-grafana-data` |

## Teardown

```powershell
# Remove resources for a specific overlay
kubectl delete -k overlays\<local|local-shared|prod>

# To also remove namespaces:
kubectl delete namespace mu-data mu-lastfm mu-apps mu-frontend art-universe-monitoring

# Local only — remove persistent data (database volumes):
kubectl delete pvc --all -n mu-data

# Local-Shared only — remove PVs (data stays in Docker named volumes):
kubectl delete pv shared-lastfm-master-data shared-lastfm-replica-data shared-music-data-db shared-prometheus-data shared-grafana-data
```

## Architecture

### Namespace Structure

**mu-data** — Database layer.
- PostgreSQL master and replica for LastFM data run as StatefulSets (ordered startup, stable network identity, persistent storage via volumeClaimTemplates).
- a separate StatefulSet runs the music-data PostgreSQL instance.
- Liquibase migration Jobs run once on deploy to apply schema changes.
- In the prod overlay, StatefulSets are replaced by ExternalName services pointing to databases running outside the cluster.

**mu-lastfm** — LastFM ETL pipeline.
- `lastfm-rest-api` (read API)
- `lastfm-etl-rest-api` (write API)
- ETL workers (currently not scalable)
    - `lastfm-calls-generator`
    - `lastfm-calls-performer`
    - `lastfm-response-parser`

**mu-apps** — Core application services running as Deployments. Both connect to databases in mu-data via cross-namespace DNS
- `music-data` (curated data management)
- `music-quiz` (quiz generation) .

**mu-frontend** — UI layer.
- `music-ui` serves the React app via NGINX. An Ingress resource routes external traffic.

**art-universe-monitoring** — Observability stack.
- Prometheus runs as a StatefulSet (persistent metrics storage).
- Grafana runs as a Deployment with dashboards and datasources provisioned from shared ConfigMaps.
- Zipkin provides distributed tracing.

### Internal Communication

Services communicate across namespaces using Kubernetes DNS: `<service>.<namespace>.svc.cluster.local`. For example, applications in mu-apps reach the database via `postgres-music-data.mu-data.svc.cluster.local:5432`. External traffic enters through NGINX Ingress in the frontend namespace. Services within the same namespace use short names (e.g., `postgres-lastfm-master`).

### Overlays

- **Local** includes the full `base/` (with PostgreSQL StatefulSets), plus LoadBalancer services on 9xxx/4000 ports
- **Local-Shared** extends Local with pre-provisioned `hostPath` PersistentVolumes pointing to Docker Compose named volume directories, enabling data sharing between environments
- **Prod** selectively includes base sub-kustomizations (skipping `data/` StatefulSets), adds ExternalName services for database connectivity, and uses LoadBalancer services on 8xxx/3000 ports

### Why Kustomize

- Native to kubectl — no extra tooling required
- Overlay model matches the existing local/dev/prod Docker Compose pattern
- Keeps base manifests readable (no templating syntax), good for learning K8s concepts

### Shared Resources

Database init scripts, Grafana dashboards, and datasource configurations are centralized at `env/docker/common/` and shared by both Docker Compose and Kubernetes deployments:

- **DB init scripts** (`env/docker/common/db/`) — Mounted directly in Docker Compose; created as ConfigMaps by deploy scripts for K8s (local overlays only)
- **Grafana provisioning** (`env/docker/common/grafana/provisioning/`) — Mounted directly in Docker Compose; created as ConfigMaps by deploy scripts for K8s

### Resource Limits

| Service Type | CPU Request | CPU Limit | Memory Request | Memory Limit |
|---|---|---|---|---|
| REST APIs | 50m | 200m | 256Mi | 512Mi |
| ETL Workers | 50-100m | 200-500m | 256-512Mi | 512Mi-1Gi |
| PostgreSQL | 200m | 500m | 128Mi | 512Mi |
| Prometheus | 200m | 500m | 512Mi | 1Gi |
| Grafana | 50m | 200m | 96Mi | 192Mi |

### Image Tags

All images are tagged `:latest` in base manifests. Images are built via `./gradlew dockerBuildAll` (each module has a `dockerBuild` task). All overlays use the same images — environment differences are handled at runtime via env vars and Kustomize patches.


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

# Scale a deployment
kubectl scale deployment lastfm-calls-performer -n mu-lastfm --replicas=3

# Check HPA status
kubectl get hpa -n mu-lastfm

# Render Kustomize output without applying
kubectl kustomize overlays/local
kubectl kustomize overlays/prod
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
