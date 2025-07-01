# Music Universe UI

> **See also**: [Development Guide](../../DEVELOPMENT.md) | [Architecture Overview](../../ARCHITECTURE.md)

## Module Purpose

React application providing management interface for Art Universe music data. Allows users to approve, bind, and manage music entities from various external sources for use in music quizzes.

## Key Components

### Data Sources Integration
- **LastFM Module** - Manage LastFM artists, tracks, tags with approval workflow
- **Music Data Module** - Bind external entities to internal approved entities
- **Future**: MusicBrainz, AlbumOfTheYear, Spotify integration

### Core UI Components
- `EntityTable` - Generic table for entity management with pagination/search
- `ApprovalToggle` - Toggle approval status (PENDING/APPROVED/DECLINED)
- `EntityBinding` - Bind external entities to internal approved entities
- `PaginatedResource` - Custom hook for API data management

### Page Components

#### Lastfm

- `LastfmArtists` - Manage LastFM artists with binding to Music Data
- `LastfmTracks` - Manage LastFM tracks with complex binding workflow
- `LastfmTags` - Manage LastFM tags with approval controls
- Navigation between different data sources and entity types

#### Approved entities (belong to music-data module)

- `Categories` - for associating artists, albums & tracks with categories
- `Dimensions` - categories' dimensions. Any category belongs to one dimension only and inherits dimension from its parent.
- `Artists` - future development
- `Albums` - future development
- `Tracks` - future development

## Data Flow Patterns

### Retrieval flow
1. Load paginated entities from LastFM API
2. Load bound entities from Music Data API
3. (future) load binding statuses from Music Quiz API

### Data source scoped approval flow (e.g. Lastfm)
1. Update entity status in data source related API (e.g. Lastfm)

### Music Data scoped approval flow
1. Ensure containing entity is approved in data source related API (e.g. Lastfm)
2. Ensure containing entity is bound to Music Data
3. Ensure managed entity is approved in data source API
4. Ensure managed entity is bound to Music Data

### Music Quiz approval flow (future)
1. repeat Music Data approval flow
2. update entity approval status in Music Quiz

### Approval System
- **PENDING** (1) - Default state from external APIs
- **APPROVED** (2) - Manually approved for use
- **DECLINED** (3) - Manually rejected
- **AUTOAPPROVED** (4) - Automatically approved

## API Integration

### LastFM API Client
- `fetchArtists`, `fetchTracks`, `fetchTags` - Paginated entity retrieval
- `updateArtistApprovalStatus`, `updateTrackApprovalStatus`, `updateTagApprovalStatus` - Approval management

### Music Data API Client

Watch **[Music Data docs](../music-data/KNOWLEDGE_BASE.md#api-conventions)**

## Technology Stack

### Frontend Framework
- **React 19** - Component-based UI
- **TypeScript** - Type safety and developer experience
- **Vite** - Fast development and building
- **SASS** - Advanced CSS features

### State Management
- React built-in state (useState, useEffect)
- Custom hooks for complex logic (`PaginatedResource`)
- Props and callbacks for component communication

## Development

**Local Development:**
```bash
npm run dev
# Runs on port 5173 with hot reload
```

**Docker Deployment:**
```bash
./env/docker/deploy.sh local   # Port 4000
./env/docker/deploy.sh prod    # Port 3000
```

## Configuration

### Environment Variables
- `VITE_MURAW_LASTFM_APP_*` - LastFM API endpoint configuration
- `VITE_MU_DATA_APP_*` - Music Data API endpoint configuration
- `VITE_MU_QUIZ_APP_*` - Music Quiz API endpoint configuration

### Build Configuration
- `vite.config.ts` - Vite configuration with path aliases
- `tsconfig.json` - TypeScript strict configuration
- Multi-stage Docker build with Nginx for production

## Project Structure

```
src/music-universe/
├── shared/                  # Shared components and utilities
│   ├── components/          # Reusable UI components
│   ├── hooks/               # Custom React hooks
│   └── types/               # TypeScript type definitions
├── sources/                 # Source-specific modules
│   ├── lastfm/              # LastFM-specific components
│   └── music-data/          # Music Data API integration
└── main.tsx                 # Application entry point
```

## Integration Points

- **LastFM Raw Data Service** - Entity management and approval
- **Music Data Service** - Entity binding operations
- **Music Quiz Service** - Future quiz management interface
- **External Links** - Direct links to LastFM, MusicBrainz pages
