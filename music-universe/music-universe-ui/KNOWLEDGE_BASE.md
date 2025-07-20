# Music Universe UI

> **See also**: [Development Guide](../../DEVELOPMENT.md) | [Architecture Overview](../../ARCHITECTURE.md)

## Module Purpose

React application providing management interface for Art Universe music data. Allows users to approve, bind, and manage music entities from various external sources for use in music quizzes.

## Key Components

### Data Sources Integration
- **LastFM Module** - Manage LastFM artists, tracks, tags with approval workflow
- **Music Data Module** - Manage master entities and bind raw entities to them
- **Music Quiz Module** - Manage quiz entities and bind master entities to them
- **Future**: MusicBrainz, AlbumOfTheYear, Spotify integration

### Core UI Components
- `EntityTable` - Generic table for entity management with pagination/search
- `ApprovalToggle` - Toggle approval status (PENDING/APPROVED/DECLINED)
- `EntityBinding` - Bind raw entities to master entities
- `PaginatedResource` - Custom hook for API data management

### Entity Architecture
- **Raw Entities** - Entities from external sources (e.g., LastFM artists, tracks, tags)
- **Master Entities** - Curated entities in Music Data module

### Page Components

#### Lastfm (Raw Entities)

- `LastfmArtists` - Manage LastFM artists with binding to master artists
- `LastfmTracks` - Manage LastFM tracks with complex binding workflow
- `LastfmTags` - Manage LastFM tags with approval controls
- Navigation between different data sources and entity types

#### Music Data (Master Entities)

- `Categories` - Master categories for associating with artists, albums & tracks
- `Dimensions` - Category dimensions. Any category belongs to one dimension only and inherits dimension from its parent.
- `Artists` - Master artists (future development)
- `Albums` - Master albums (future development)
- `Tracks` - Master tracks (future development)

## Data Flow Patterns

### Retrieval flow
1. Load paginated raw entities from LastFM API
2. Load bound master entities from Music Data API
3. Load quiz binding statuses from Music Quiz API

### Raw entity approval flow (e.g. Lastfm)
1. Update entity status in data source related API (e.g. Lastfm)

### Master entity binding flow
1. Ensure raw entity is approved in its source API (e.g. Lastfm)
2. Bind raw entity to master entity in Music Data
3. Update UI to reflect binding status

### Quiz binding flow
1. Ensure raw entity is bound to master entity
2. Bind master entity to quiz module
3. Update UI to reflect quiz binding status

### Approval System
- **PENDING** (1) - Default state from external APIs
- **APPROVED** (2) - Manually approved for use
- **DECLINED** (3) - Manually rejected
- **AUTOAPPROVED** (4) - Automatically approved

## API Integration

### LastFM API Client (Raw Entities)
- `fetchArtists`, `fetchTracks`, `fetchTags` - Paginated entity retrieval
- `updateArtistApprovalStatus`, `updateTrackApprovalStatus`, `updateTagApprovalStatus` - Approval management

### Music Data API Client (Master Entities)
- `bindArtistToExisting`, `createAndBindArtist`, `unbindArtist` - Artist binding operations
- `bindCategoryToExisting`, `createAndBindCategory`, `unbindCategory` - Category binding operations
- `bindTrack`, `unbindTrack` - Track binding operations

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
├── sources/                 # Source-specific modules (raw entities)
│   ├── lastfm/              # LastFM-specific components
│   └── adapters/            # Adapters for raw entities
├── music-data/              # Music Data integration (master entities)
│   ├── api/                 # API clients for master entities
│   ├── components/          # Components for master entities
│   ├── types/               # Types for master entities
│   └── adapters/            # Adapters for master entities
├── music-quiz/              # Music Quiz integration
│   ├── api/                 # API clients for quiz bindings
│   └── components/          # Components for quiz bindings
└── main.tsx                 # Application entry point
```

## Integration Points

- **LastFM Raw Data Service** - Raw entity management and approval
- **Music Data Service** - Master entity management and binding
- **Music Quiz Service** - Quiz binding operations
- **External Links** - Direct links to LastFM, MusicBrainz pages
