# Art Universe

**Art Universe** is a platform for collecting, curating, and utilizing data from the art domain. 

The project currently focuses on music data and quiz applications - generating sets of tracks for "Guess the Track" game where participants listen to tracks and guess artist/title.

## Quick Start

```bash
# Build Docker images and deploy local environment
./gradlew dockerBuildAll -x test
./env/docker/deploy.sh local
```

## High-level Architecture

```
┌─────────────────┐     ┌──────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   External      │     │   Raw Data       │     │   Master        │     │   Quiz          │
│   APIs          │---->│   Collection     │---->│   Data          │---->│   Generation    │
│                 │     │                  │     │   Management    │     │                 │
└─────────────────┘     └──────────────────┘     └─────────────────┘     └─────────────────┘
                                │                      │                         │
                                │                      │                         │
                                ▼                      ▼                         ▼
                         ┌───────────────────────────────────────────────────────────┐
                         │                     UI Management                         │
                         │    ┌─────────────┬─────────────┬─────────────────────┐    │
                         │    │  Raw        │  Master     │  Quiz               │    │
                         │    │  Data       │  Data       │  Admin              │    │
                         │    │  Admin      │  Admin      │                     │    │
                         │    └─────────────┴─────────────┴─────────────────────┘    │
                         └───────────────────────────────────────────────────────────┘
```

## Technology Stack

### Backend (Java Modules)
- **Java**: 21
- **Spring Boot**: 3.4.3
- **Spring Data JPA**: Included in Spring Boot
- **Liquibase**: 4.15.0
- **Lombok**: 1.18.30
- **TestContainers**: 1.20.4
- **PostgreSQL Driver**: Latest (from Spring Boot BOM)

### Frontend (UI Module)
- **React**: 19.1.0
- **TypeScript**: 5.8.3
- **Vite**: 6.3.5
- **React Router**: 6.30.0
- **TanStack Query**: 5.28.0
- **Axios**: 1.12.0

### Build Tools
- **Gradle**: 8.10
- **Gradle Wrapper**: Included (use `./gradlew`)

## Documentation

- **[Architecture](docs/ARCHITECTURE.md)** - More detailed architecture reference
- **[All modules](docs/MODULES.md)** - List of deployable services with ports
- **[All services & ports](docs/SERVICES.md)** - List of deployable services with ports
- **[Knowledge Base](docs/kb/README.md)** - LLM-oriented knowledge base
- **[Gradle commands reference](docs/kb/guides/gradle-commands.md)** - Complete modules list
- **[Development Guide](docs/DEVELOPMENT.md)** - Complete development workflow
- **[Docker Deployment Scripts](env/docker/README.md)** - Deployment guide (Docker Compose)
- **[Kubernetes Deployment](env/k8s/README.md)** - Deployment guide (Kubernetes/Kustomize)

