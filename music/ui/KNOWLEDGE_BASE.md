# Music Universe UI

> **For developers:** See [Claude Code Documentation](../../.claude/commands/mu-ui/OVERVIEW.md) for detailed technical reference
>
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
- `EntityBinding` - Bind raw entities to master entities
- `EntityPicker` - Universal entity selection modal with autocomplete
- `LookupRegistry` - Centralized entity search and autocomplete system
- `ApprovalToggle` - Toggle approval status (PENDING/APPROVED/DECLINED) for raw entities
- `CategoryDag` - Interactive graph visualization for category hierarchies
- `QuizBinding` - Bind master entities to quiz module
- `PipelineEditor` - Visual editor for quiz track generation pipelines

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

- `Categories` - Master categories with DAG visualization for hierarchy
- `Artists` - Master artists (future development)
- `Albums` - Master albums (future development)
- `Tracks` - Master tracks (future development)

#### Music Quiz

- `Games` - Quiz game management
- `PipelineEditor` - Configure multi-step track generation pipelines (filters, penalties, balancers)
- `Generations` - View and manage quiz track generations
- Various step types for flexible track curation

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
- **React Query** (@tanstack/react-query) - Server state management with caching
- React built-in state (useState, useEffect) - Local UI state
- Custom hooks for complex logic (table management, entity lookups)
- Context API for global state (notifications)
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
src/music/
├── main.tsx                 # Application entry point
├── MusicUniverseApp.tsx     # Root router component
├── shared/                  # Shared across all modules
│   ├── components/          # Reusable UI components (EntityTable, EntityPicker)
│   ├── hooks/               # Custom React hooks
│   ├── services/            # Core services (LookupRegistry)
│   ├── types/               # Core type definitions
│   └── utils/               # Utilities (query keys)
├── data/                    # Data management layer
│   ├── raw/lastfm/          # LastFM raw data integration
│   │   ├── api/             # LastFM API clients
│   │   ├── components/      # LastFM components (ApprovalToggle, EntityBinding)
│   │   ├── pages/           # LastFM pages (Artists, Tracks, Tags)
│   │   └── types/           # LastFM types
│   └── master/              # Master data management
│       ├── api/             # Master data API clients
│       ├── components/      # Master components (CategoryDag)
│       ├── pages/           # Master pages (Categories, Artists)
│       └── types/           # Master entity types
└── quiz/                    # Quiz game management
    ├── api/                 # Quiz API client
    ├── components/          # Quiz components (PipelineEditor, Games)
    ├── types/               # Pipeline step types
    └── utils/               # Quiz utilities
```

## Integration Points

- **LastFM Raw Data Service** - Raw entity management and approval
- **Music Data Service** - Master entity management and binding
- **Music Quiz Service** - Quiz binding operations
- **External Links** - Direct links to LastFM, MusicBrainz pages
