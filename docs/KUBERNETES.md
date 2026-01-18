# Art Universe - Kubernetes Deployment

This document describes the Kubernetes deployment architecture for the Art Universe project, designed for learning K8s using Docker Desktop's built-in cluster.

## Overview

The Kubernetes deployment mirrors the existing Docker Compose setup while providing:
- Namespace isolation for different concerns
- Scalability via Horizontal Pod Autoscalers (HPA)
- Environment-specific configurations via Kustomize overlays
- External traffic routing via NGINX Ingress

## Architecture

### Namespace Structure

The project uses 5 namespaces to isolate different concerns:

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              Kubernetes Cluster                                  │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                  │
│  ┌─────────────────────┐  ┌─────────────────────┐  ┌─────────────────────────┐  │
│  │  art-universe-data  │  │ art-universe-lastfm │  │   art-universe-apps     │  │
│  │                     │  │                     │  │                         │  │
│  │  • postgres-lastfm  │  │  • lastfm-rest-api  │  │  • music-data           │  │
│  │    (master/replica) │  │  • lastfm-etl-api   │  │  • music-quiz           │  │
│  │  • postgres-music   │  │  • calls-generator  │  │                         │  │
│  │  • liquibase jobs   │  │  • calls-performer  │  │                         │  │
│  │                     │  │  • response-parser  │  │                         │  │
│  └─────────────────────┘  └─────────────────────┘  └─────────────────────────┘  │
│                                                                                  │
│  ┌─────────────────────┐  ┌─────────────────────────────────────────────────┐   │
│  │art-universe-frontend│  │           art-universe-monitoring               │   │
│  │                     │  │                                                 │   │
│  │  • music-ui         │  │  • prometheus    • grafana    • zipkin          │   │
│  │  • ingress (nginx)  │  │                                                 │   │
│  └─────────────────────┘  └─────────────────────────────────────────────────┘   │
│                                                                                  │
└─────────────────────────────────────────────────────────────────────────────────┘
```

| Namespace | Purpose |
|-----------|---------|
| `art-universe-data` | Databases and migration jobs |
| `art-universe-lastfm` | LastFM ETL pipeline services |
| `art-universe-apps` | Core application services (music-data, music-quiz) |
| `art-universe-frontend` | UI and API Gateway (future) |
| `art-universe-monitoring` | Prometheus, Grafana, Zipkin |

### Internal Communication

- Services communicate directly within and across namespaces (not through gateway)
- DNS format: `<service>.<namespace>.svc.cluster.local`
- External traffic routes through NGINX Ingress in frontend namespace
- API Gateway will be added to frontend namespace when ready

### Database Strategy

| Environment | Approach |
|-------------|----------|
| Local (learning) | PostgreSQL runs in K8s via StatefulSets |
| Production | External databases via ExternalName Service |

## Kubernetes Resources

### Databases (art-universe-data)

| Resource | K8s Type | Purpose | Replicas |
|----------|----------|---------|----------|
| postgres-lastfm-master | StatefulSet | LastFM master database | 1 |
| postgres-lastfm-replica | StatefulSet | LastFM read replica | 1 |
| postgres-music-data | StatefulSet | Music data and quiz database | 1 |
| lastfm-liquibase-job | Job | Database migrations for LastFM | N/A |
| music-data-liquibase-job | Job | Database migrations for music-data | N/A |

**Services:**
- `postgres-lastfm-master` - ClusterIP (port 5432)
- `postgres-lastfm-replica` - ClusterIP (port 5432)
- `postgres-music-data` - ClusterIP (port 5432)

### LastFM Services (art-universe-lastfm)

| Resource | K8s Type | Purpose | Scalable |
|----------|----------|---------|----------|
| lastfm-rest-api | Deployment | Read-only REST API for LastFM raw data | Yes |
| lastfm-etl-rest-api | Deployment | Write operations REST API for LastFM ETL | Yes |
| lastfm-calls-generator | Deployment + HPA | Generates API calls for LastFM data collection | Yes |
| lastfm-calls-performer | Deployment + HPA | Executes API calls against LastFM API | Yes |
| lastfm-response-parser | Deployment + HPA | Parses and processes LastFM API responses | Yes |

**Services:**
- `lastfm-rest-api` - ClusterIP (port 8084)
- `lastfm-etl-rest-api` - ClusterIP (port 8085)

**HPA Configuration (ETL Services):**
| Service | Min Replicas | Max Replicas | Target CPU |
|---------|--------------|--------------|------------|
| lastfm-calls-generator | 1 | 3 | 70% |
| lastfm-calls-performer | 1 | 5 | 70% |
| lastfm-response-parser | 1 | 5 | 70% |

### Core Apps (art-universe-apps)

| Resource | K8s Type | Purpose |
|----------|----------|---------|
| music-data | Deployment | Curated data management and binding service |
| music-quiz | Deployment | Quiz generation from approved data |

**Services:**
- `music-data` - ClusterIP (port 8082)
- `music-quiz` - ClusterIP (port 8083)

### Frontend (art-universe-frontend)

| Resource | K8s Type | Purpose |
|----------|----------|---------|
| music-ui | Deployment | React management interface |
| ingress | Ingress (NGINX) | External traffic routing |

**Services:**
- `music-ui` - ClusterIP (port 3000)

**Ingress Routes:**
| Path | Backend Service | Port |
|------|-----------------|------|
| `/` | music-ui | 3000 |
| `/api/lastfm/*` | lastfm-rest-api | 8084 |
| `/api/lastfm/etl/*` | lastfm-etl-rest-api | 8085 |
| `/api/music/*` | music-data | 8082 |
| `/api/quiz/*` | music-quiz | 8083 |

### Monitoring (art-universe-monitoring)

| Resource | K8s Type | Purpose |
|----------|----------|---------|
| prometheus | StatefulSet | Metrics collection and time-series storage |
| grafana | Deployment | Metrics visualization and dashboards |
| zipkin | Deployment | Distributed tracing |

**Services:**
- `prometheus` - ClusterIP (port 9090)
- `grafana` - ClusterIP (port 3000)
- `zipkin` - ClusterIP (port 9411)

## Configuration Management: Kustomize

### Why Kustomize?

- Native to kubectl (no extra tools required)
- Environment overlays match existing local/dev/prod pattern
- Better for learning K8s concepts than Helm
- Simpler for this project's scale

### File Structure

```
env/k8s/
├── README.md                           # Quick start guide
├── scripts/
│   ├── deploy-local.sh                 # Linux/Mac deployment script
│   └── deploy-local.bat                # Windows deployment script
├── base/
│   ├── kustomization.yaml              # Main base kustomization
│   ├── namespaces.yaml                 # All namespace definitions
│   ├── data/
│   │   ├── kustomization.yaml          # Data namespace resources
│   │   ├── postgres-lastfm-master/
│   │   │   ├── statefulset.yaml        # StatefulSet definition
│   │   │   ├── service.yaml            # ClusterIP service
│   │   │   └── pvc.yaml                # PersistentVolumeClaim
│   │   ├── postgres-lastfm-replica/
│   │   │   ├── statefulset.yaml
│   │   │   ├── service.yaml
│   │   │   └── pvc.yaml
│   │   ├── postgres-music-data/
│   │   │   ├── statefulset.yaml
│   │   │   ├── service.yaml
│   │   │   └── pvc.yaml
│   │   └── migrations/
│   │       ├── lastfm-liquibase-job.yaml
│   │       └── music-data-liquibase-job.yaml
│   ├── lastfm/
│   │   ├── kustomization.yaml          # LastFM namespace resources
│   │   ├── lastfm-rest-api/
│   │   │   ├── deployment.yaml
│   │   │   └── service.yaml
│   │   ├── lastfm-etl-rest-api/
│   │   │   ├── deployment.yaml
│   │   │   └── service.yaml
│   │   ├── lastfm-calls-generator/
│   │   │   ├── deployment.yaml
│   │   │   └── hpa.yaml
│   │   ├── lastfm-calls-performer/
│   │   │   ├── deployment.yaml
│   │   │   └── hpa.yaml
│   │   └── lastfm-response-parser/
│   │       ├── deployment.yaml
│   │       └── hpa.yaml
│   ├── apps/
│   │   ├── kustomization.yaml          # Apps namespace resources
│   │   ├── music-data/
│   │   │   ├── deployment.yaml
│   │   │   └── service.yaml
│   │   └── music-quiz/
│   │       ├── deployment.yaml
│   │       └── service.yaml
│   ├── frontend/
│   │   ├── kustomization.yaml          # Frontend namespace resources
│   │   ├── music-ui/
│   │   │   ├── deployment.yaml
│   │   │   └── service.yaml
│   │   └── ingress.yaml                # NGINX Ingress configuration
│   └── monitoring/
│       ├── kustomization.yaml          # Monitoring namespace resources
│       ├── prometheus/
│       │   ├── statefulset.yaml
│       │   ├── service.yaml
│       │   ├── configmap.yaml          # Prometheus configuration
│       │   └── pvc.yaml
│       ├── grafana/
│       │   ├── deployment.yaml
│       │   ├── service.yaml
│       │   └── configmap.yaml          # Grafana datasources/dashboards
│       └── zipkin/
│           ├── deployment.yaml
│           └── service.yaml
└── overlays/
    ├── local/
    │   ├── kustomization.yaml          # Local environment overlay
    │   ├── secrets/
    │   │   ├── db-credentials.yaml     # Database credentials (sealed)
    │   │   └── api-keys.yaml           # API keys (sealed)
    │   └── configmaps/
    │       └── env-config.yaml         # Environment-specific config
    └── prod/
        ├── kustomization.yaml          # Production environment overlay
        ├── external-db-config.yaml     # ExternalName services for DBs
        └── secrets/
            └── (managed externally)
```

### Configuration File Purposes

#### Base Configuration

| File | Purpose |
|------|---------|
| `base/kustomization.yaml` | References all namespace-specific kustomizations |
| `base/namespaces.yaml` | Defines all 5 namespaces with labels |

#### Data Namespace

| File | Purpose |
|------|---------|
| `statefulset.yaml` | PostgreSQL StatefulSet with ordered pod management |
| `service.yaml` | Headless service for stable network identity |
| `pvc.yaml` | PersistentVolumeClaim for database storage |
| `*-liquibase-job.yaml` | Kubernetes Job for running Liquibase migrations |

#### Service Namespaces (lastfm, apps, frontend)

| File | Purpose |
|------|---------|
| `deployment.yaml` | Pod template, replicas, resource limits, probes |
| `service.yaml` | ClusterIP service exposing the deployment |
| `hpa.yaml` | HorizontalPodAutoscaler for ETL services |
| `ingress.yaml` | NGINX Ingress rules for external traffic |

#### Monitoring Namespace

| File | Purpose |
|------|---------|
| `configmap.yaml` | Prometheus scrape config, Grafana datasources |
| `statefulset.yaml` | Prometheus with persistent storage |
| `deployment.yaml` | Grafana and Zipkin deployments |

#### Overlays

| File | Purpose |
|------|---------|
| `kustomization.yaml` | Environment-specific patches and image tags |
| `secrets/` | Environment-specific secrets (use SealedSecrets for production) |
| `configmaps/` | Environment-specific configuration |
| `external-db-config.yaml` | ExternalName services pointing to external DBs |

## Versioning Strategy

### Semantic Versioning

All container images use semantic versioning: `<major>.<minor>.<patch>`

| Version Part | When to Increment |
|--------------|-------------------|
| Major | Breaking API changes, incompatible schema changes |
| Minor | New features, backward-compatible changes |
| Patch | Bug fixes, small improvements |

### Image Tags

```
ghcr.io/<org>/art-universe/<service>:<version>

# Examples:
ghcr.io/art-universe/lastfm-rest-api:1.0.0
ghcr.io/art-universe/music-data:1.2.3
ghcr.io/art-universe/music-ui:2.0.0
```

### Version Management

- Image versions are defined in overlay `kustomization.yaml`
- Base configurations use placeholder image references
- Each overlay specifies exact versions for its environment

```yaml
# overlays/local/kustomization.yaml
images:
  - name: lastfm-rest-api
    newName: ghcr.io/art-universe/lastfm-rest-api
    newTag: "1.0.0"
```

## Artifact Repository: GitHub Container Registry (GHCR)

### Why GHCR?

- Free for public repositories
- Integrated with GitHub Actions for CI/CD
- Supports image signing and attestations
- No additional account setup required

### Image Naming Convention

```
ghcr.io/<github-org>/art-universe/<service>:<version>
```

### Publishing Images

Images are published via GitHub Actions on:
- Push to `main` branch (tagged as `latest`)
- Git tags matching `v*.*.*` (semantic version)

Example workflow:
```yaml
# .github/workflows/publish-images.yaml
name: Publish Container Images
on:
  push:
    tags: ['v*.*.*']
    branches: [main]
```

## Health Probes and Resource Limits

### Health Probes

All deployments include liveness and readiness probes:

```yaml
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8080
  initialDelaySeconds: 60
  periodSeconds: 10
  failureThreshold: 3

readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 5
  failureThreshold: 3
```

### Resource Limits

| Service Type | CPU Request | CPU Limit | Memory Request | Memory Limit |
|--------------|-------------|-----------|----------------|--------------|
| REST APIs | 100m | 500m | 256Mi | 512Mi |
| ETL Services | 200m | 1000m | 512Mi | 1Gi |
| Databases | 250m | 1000m | 512Mi | 2Gi |
| Monitoring | 100m | 500m | 256Mi | 512Mi |

## Deployment

### Prerequisites

1. Docker Desktop with Kubernetes enabled
2. kubectl configured for Docker Desktop cluster
3. NGINX Ingress Controller installed

```bash
# Install NGINX Ingress Controller
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.9.4/deploy/static/provider/cloud/deploy.yaml
```

### Local Deployment

```bash
# Navigate to k8s directory
cd env/k8s

# Deploy using Kustomize (local overlay)
kubectl apply -k overlays/local

# Verify deployment
kubectl get pods -A | grep art-universe

# Check service status
kubectl get svc -n art-universe-apps
kubectl get svc -n art-universe-lastfm
kubectl get svc -n art-universe-frontend
```

### Deployment Order

The deployment follows this order:
1. Namespaces
2. ConfigMaps and Secrets
3. Databases (StatefulSets)
4. Migration Jobs (wait for completion)
5. Application Services (Deployments)
6. Monitoring Stack
7. Ingress

### Accessing Services

| Service | Local URL |
|---------|-----------|
| Music UI | http://localhost/ |
| LastFM API | http://localhost/api/lastfm/ |
| Music Data API | http://localhost/api/music/ |
| Grafana | http://localhost:9000 (NodePort) |
| Prometheus | http://localhost:9090 (NodePort) |

### Useful Commands

```bash
# View all resources in a namespace
kubectl get all -n art-universe-lastfm

# Check pod logs
kubectl logs -n art-universe-lastfm deployment/lastfm-rest-api

# Describe a pod for troubleshooting
kubectl describe pod -n art-universe-data postgres-lastfm-master-0

# Scale a deployment
kubectl scale deployment lastfm-calls-performer -n art-universe-lastfm --replicas=3

# Check HPA status
kubectl get hpa -n art-universe-lastfm

# Port-forward for debugging
kubectl port-forward -n art-universe-apps svc/music-data 8082:8082

# Apply changes after editing manifests
kubectl apply -k overlays/local

# Delete all resources
kubectl delete -k overlays/local
```

## Production Considerations

### External Database Configuration

Production uses external databases via ExternalName Services:

```yaml
# overlays/prod/external-db-config.yaml
apiVersion: v1
kind: Service
metadata:
  name: postgres-lastfm-master
  namespace: art-universe-data
spec:
  type: ExternalName
  externalName: lastfm-master.database.example.com
```

### Secrets Management

- Local: Secrets stored in `secrets/` directory (gitignored)
- Production: Use Sealed Secrets or external secrets manager

### TLS/SSL

Production ingress should enable TLS:
```yaml
spec:
  tls:
    - hosts:
        - art-universe.example.com
      secretName: art-universe-tls
```

## Future: API Gateway Integration

When the API Gateway is ready, it will be deployed to the frontend namespace:

```
┌─────────────────────────────────────────────────────────────────────┐
│                    art-universe-frontend                             │
│                                                                      │
│  ┌──────────┐    ┌─────────────┐    ┌─────────────────────────────┐ │
│  │  Ingress │───▶│ API Gateway │───▶│ Backend Services            │ │
│  │  (NGINX) │    │   (future)  │    │ (lastfm, apps namespaces)   │ │
│  └──────────┘    └─────────────┘    └─────────────────────────────┘ │
│                         │                                            │
│                         ▼                                            │
│                  ┌─────────────┐                                     │
│                  │  music-ui   │                                     │
│                  └─────────────┘                                     │
└─────────────────────────────────────────────────────────────────────┘
```

The API Gateway will handle:
- Authentication (JWT validation)
- Rate limiting
- Request routing
- API versioning

## See Also

- [Services Reference](SERVICES.md) - Complete service list and ports
- [Architecture Reference](ARCHITECTURE.md) - System architecture overview
- [Development Reference](DEVELOPMENT.md) - Development workflow
- [Docker Deployment Reference](../env/docker/README.md) - Docker Compose deployment
