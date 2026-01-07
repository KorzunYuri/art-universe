# Music Universe UI Module

React + TypeScript frontend application for managing music data from external sources and internal master entities.

Key capabilities:
- Master data management (artists, albums, tracks, categories)
  - Category hierarchy visualization (DAG graph)
- LastFM raw data management (approval, binding to master entities)
- Quiz tracks pack generation and management

## Backend APIs Required

- [LastFM Read API](../data/raw/lastfm/lastfm-rest-api/README.md): Raw artist/track data
- [LastFM Write API](../data/raw/lastfm/etl/lastfm-etl-rest-api): Write (Approval, maintenance initiation) operations
- [Music Data Master API](../data/master/README.md): Master entity CRUD + binding
- [Music Quiz API](../quiz/README.md): Quiz generation

## Technology Stack

### Core Technologies
- React 19 - Component framework
- TypeScript (strict mode) - Type safety
- Vite - Build tool with HMR
- SASS - Styling with CSS Modules

### Libraries
- React Router - Client-side routing
- @tanstack/react-query (TanStack Query) - Server state management
- Axios - HTTP client for API integration
- @xyflow/react - Graph visualization (Category DAG)


## Documentation

This module provides detailed documentation organized by topic:

- [Package Structure](docs/package-structure.md) - Directory organization
- [Component Overview](docs/components/overview.md) - All UI components categorized by purpose
- [API Integration](docs/api-integration/README.md) - HTTP client setup, DTO mapping, error handling
- [React Query](docs/patterns/react-query/README.md) - Server state management with TanStack Query
- [Patterns Overview](docs/patterns/overview.md) - Reusable implementation patterns
- [Workflows Overview](docs/flows/overview.md) - End-to-end user workflows
- [Quiz Documentation](docs/quiz/overview.md) - Quiz generation and pipeline system


## Component Architecture

For a complete categorized list of all components, see:
- [Component Overview](docs/components/overview.md) - All components categorized by purpose and reusability


## Routing

### Main Router

File: `music/ui/src/music/MusicUniverseApp.tsx`

Routes:
```
/                           → Home
/lastfm/artists             → LastFM Artists
/lastfm/tracks              → LastFM Tracks
/lastfm/tags                → LastFM Tags
/master/artists             → Master Artists
/master/categories          → Master Categories
/master/tracks              → Master Tracks
/quiz/pipelines             → Quiz Pipelines
/quiz/games                 → Quiz Games
```


## Configuration Files

| File | Purpose |
|------|---------|
| `vite.config.ts` | Vite build configuration |
| `tsconfig.app.json` | TypeScript configuration (app) |
| `tsconfig.node.json` | TypeScript configuration (build) |
| `package.json` | Dependencies and scripts |
| `.env` | Environment variables |
| `.eslintrc.cjs` | ESLint configuration |


## Related Documentation

### Backend API Patterns
- [Entities Lookup Pattern](../../docs/kb/patterns/backend/api/lookup.md) - Backend lookup endpoint design

### Project Documentation
- [Architecture Overview](../../docs/ARCHITECTURE.md)
- [Project Modules Index](../../docs/MODULES.md)
- [Development Workflow](../../docs/kb/guides/development-tasks.md)
