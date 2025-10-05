# Art Universe - Architecture Overview

## System Purpose

Data storage system for art-related information designed to create quizzes. Primary focus: "Guess the Track" game where participants listen to tracks and guess artist/title.

## High-Level Architecture

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   External      │    │   Raw Data       │    │   Curated       │
│   APIs          │───▶│   Collection     │───▶│   Data          │
│                 │    │                  │    │   Management    │
└─────────────────┘    └──────────────────┘    └─────────────────┘
                                │                  │        │
                                │                  ▼        │
                                │       ┌─────────────────┐ │
                                │       │   Quiz          │ │
                                │       │   Generation    │ │
                                │       └─────────────────┘ │
                                │               │           │
                                ▼               ▼           ▼
                       ┌─────────────────────────────────────┐
                       │          UI Management              │
                       │   ┌─────────┬─────────┬─────────┐   │
                       │   │Raw Data │ Curated │  Quiz   │   │
                       │   │ Admin   │  Data   │ Config  │   │
                       │   └─────────┴─────────┴─────────┘   │
                       └─────────────────────────────────────┘
```

## Module Dependencies

### Data Flow
1. **Raw Data Modules** collect from external APIs (LastFM, MusicBrainz, etc.)
2. **Music Data** manages approved/curated entities and bindings
3. **Music Quiz** generates quizzes from approved data subset
4. **UI** provides unified management interface for all three modules

### Dependency Graph
```
music-data-raw-* ──┐
                   ├──▶ music-data ──▶ music-quiz
art-universe-commons ──┘        │           │
                                │           │
                                ▼           ▼
                       ┌─────────────────────────┐
                       │        music-ui         │
                       │  ┌─────┬─────┬─────┐    │
                       │  │Raw  │Data │Quiz │    │
                       │  │Admin│Mgmt │Cfg  │    │
                       │  └─────┴─────┴─────┘    │
                       └─────────────────────────┘
```

### UI Module Responsibilities
- **Raw Data Administration**: Approve/decline entities, manage API collection settings
- **Curated Data Management**: Create bindings, manage approved entity relationships  
- **Quiz Configuration**: Set up quiz parameters, manage quiz-specific entity selections

## Data Architecture

### Entity Hierarchy
- **Raw Entities**: Direct API mappings with full attribute history
- **Approved Entities**: Curated subset with manual approval workflow  
- **Quiz Entities**: Approved subset specifically marked for quiz use

### Binding System
External entities can be bound to internal approved entities:
- **ArtistBinding**: Links external artists to approved artists
- **AlbumBinding**: Links external albums to approved albums
- **TrackBinding**: Links external tracks to approved tracks

### Approval Workflow
1. **PENDING** (1): Default state from external APIs
2. **APPROVED** (2): Manually approved for use
3. **DECLINED** (3): Manually rejected
4. **AUTOAPPROVED** (4): Automatically approved (temporary)

## Database Schema Design

### Schema Separation
- `mu_raw_lastfm`: LastFM raw data with full API responses
- `mu`: Approved/curated music data with bindings
- `mu_quiz`: Quiz-specific data and configurations

### Common Patterns
- All entities extend BaseEntity (id, created_at, updated_at)
- Coded enums stored in dictionary tables
- Attribute history using SCD2 (Slowly Changing Dimension)
- Foreign key constraints ensure referential integrity

## API Design Patterns

### REST Conventions
- **GET** `/api/v1/{entities}/search` - List with pagination/filtering
- **GET** `/api/v1/{entities}/{id}` - Single entity retrieval
- **POST** `/api/v1/{entities}` - Create new entity
- **PATCH** `/api/v1/{entities}/{id}` - Partial update
- **DELETE** `/api/v1/{entities}/{id}` - Remove entity

### Response Format
```json
{
  "success": boolean,
  "message": "string",
  "data": T | null
}
```

### Pagination
```json
{
  "content": T,
  "totalElements": number,
  "totalPages": number,
  "size": number,
  "number": number
}
```

## Security Architecture

### Authentication
- Currently: No authentication (local development)
- Planned: JWT-based authentication with role-based access

### Authorization
- Module-level access control
- Data source specific permissions
- Approval workflow permissions

### Data Protection
- Secrets management via environment files
- API keys stored in `.secrets.env` files
- Database credentials separated by environment

## Scalability Considerations

### Current Limitations
- Single-instance deployment
- Synchronous API processing
- Manual approval workflow

### Planned Improvements
- **Kafka Integration**: Asynchronous message processing
- **Kubernetes Deployment**: Container orchestration
- **API Rate Limiting**: Per-key scaling for external APIs
- **Caching Layer**: Redis for frequently accessed data

## Monitoring and Observability

### Planned Implementation
- **Prometheus**: Metrics collection
- **Grafana**: Visualization and alerting
- **Structured Logging**: JSON logs with correlation IDs
- **Health Checks**: Application and dependency monitoring
