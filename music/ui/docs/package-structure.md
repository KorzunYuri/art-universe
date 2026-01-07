# Package Structure

## Overview

The Music UI module follows a modular architecture with domain-driven organization, separating concerns between shared infrastructure, raw data integration, master data management, and quiz functionality.

## Module Organization

### Shared Layer

Location: [src/music/shared/](../src/music/shared/)

**Purpose:** Cross-cutting infrastructure used by all domain modules

**Key Subdirectories:**
- `components/` - Reusable UI components ([EntityTable](../src/music/shared/components/EntityTable/), [EntityPicker](../src/music/shared/components/EntityPicker/), etc.)
- `hooks/` - Shared React hooks ([useEntityLookup](../src/music/shared/hooks/useEntityLookup.ts), [useNotifications](../src/music/shared/hooks/useNotifications.ts))
- `services/` - Core services ([LookupRegistry](../src/music/shared/services/LookupRegistry.ts))
- `types/` - Base type definitions ([entities.ts](../src/music/shared/types/entities.ts), [page.ts](../src/music/shared/types/page.ts))
- `utils/` - Utilities ([query-keys.ts](../src/music/shared/utils/query-keys.ts))

### Master Data Module

Location: [src/music/data/master/](../src/music/data/master/)

**Purpose:** Canonical master entity management

**Key Subdirectories:**
- `api/` - Master data API client with entity-specific files
  - [music-data-commons.ts](../src/music/data/master/api/music-data-commons.ts) - Entity mappers, endpoint mapping
  - [music-data-common-fetching.ts](../src/music/data/master/api/music-data-common-fetching.ts) - Page fetching
  - [music-data-common-lookup.ts](../src/music/data/master/api/music-data-common-lookup.ts) - Lookup functions
  - [music-data-common-binding.ts](../src/music/data/master/api/music-data-common-binding.ts) - Binding operations
  - [music-data-artists.ts](../src/music/data/master/api/music-data-artists.ts) - Artist DTOs and API
  - [music-data-albums.ts](../src/music/data/master/api/music-data-albums.ts) - Album DTOs and API
  - [music-data-tracks.ts](../src/music/data/master/api/music-data-tracks.ts) - Track DTOs and API
  - [music-data-categories.ts](../src/music/data/master/api/music-data-categories.ts) - Category DTOs and API
- `components/` - Master-specific UI ([ArtistsTable](../src/music/data/master/components/ArtistsTable/), [CategoryDag](../src/music/data/master/components/CategoryDag/))
- `hooks/` - Master-specific hooks ([useMasterEntityTable](../src/music/data/master/hooks/useMasterEntityTable.ts), [useMasterEntity](../src/music/data/master/hooks/useMasterEntity.ts))
- `services/` - [registerMasterLookups](../src/music/data/master/services/registerMasterLookups.ts)

### LastFM Module

Location: [src/music/data/raw/lastfm/](../src/music/data/raw/lastfm/)

**Purpose:** LastFM external data integration

**Key Subdirectories:**
- `api/` - LastFM API client with entity-specific files
  - [lastfm-common.ts](../src/music/data/raw/lastfm/api/lastfm-common.ts) - Entity mappers, endpoint mapping
  - [lastfm-common-fetching.ts](../src/music/data/raw/lastfm/api/lastfm-common-fetching.ts) - Page fetching
  - [lastfm-lookup.ts](../src/music/data/raw/lastfm/api/lastfm-lookup.ts) - Lookup by entity type
  - [lastfm-artists.ts](../src/music/data/raw/lastfm/api/lastfm-artists.ts) - Artist DTOs and API
  - [lastfm-albums.ts](../src/music/data/raw/lastfm/api/lastfm-albums.ts) - Album DTOs and API
  - [lastfm-tracks.ts](../src/music/data/raw/lastfm/api/lastfm-tracks.ts) - Track DTOs and API
  - [lastfm-tags.ts](../src/music/data/raw/lastfm/api/lastfm-tags.ts) - Tag DTOs and API
- `components/` - LastFM-specific UI ([ApprovalToggle](../src/music/data/raw/lastfm/components/ApprovalToggle/), [EntityBinding](../src/music/data/raw/lastfm/components/EntityBinding/))
- `hooks/` - [useLastfmEntityApproval](../src/music/data/raw/lastfm/hooks/useLastfmEntityApproval.ts)
- `constants/` - [approvalStatus.ts](../src/music/data/raw/lastfm/constants/approvalStatus.ts)
- `services/` - [registerLastfmLookups](../src/music/data/raw/lastfm/services/registerLastfmLookups.ts)

### Quiz Module

Location: [src/music/quiz/](../src/music/quiz/)

**Purpose:** Quiz generation and management

**Key Subdirectories:**
- `api/` - Quiz API client
- `components/` - Quiz-specific UI
- `hooks/` - [useQuizBinding](../src/music/quiz/hooks/useQuizBinding.ts), [useQuizData](../src/music/quiz/hooks/useQuizData.ts)
- `types/` - Quiz entity types, generation steps

## File Organization Patterns

### API Layer Pattern

Each domain module follows consistent API organization:

```
api/
├── {module}-commons.ts           # Entity mappers, endpoint mapping
├── {module}-common-[feature].ts  # Generic functions for specific topic
├── {module}-[topic/entity].ts    # Topic/entity specific DTOs and functions
```

### Component Layer Pattern

Components organized by domain with consistent structure:

```
components/
├── {ComponentName}/
│   ├── {ComponentName}.tsx       # Component implementation
│   ├── {ComponentName}.module.scss  # Scoped styles
│   └── index.ts                  # Re-export
```

### Hooks Layer Pattern

Hooks organized by purpose:

```
hooks/
├── use{Entity}{Operation}.ts     # Entity-specific operations
├── use{Feature}.ts               # Feature-specific logic
└── index.ts                      # Re-exports
```

## Configuration Files

Location: `src/music/data/{module}/config/`

**Purpose:** Environment-based configuration for API endpoints

**Files:**
- [musicdataconfig.ts](../src/music/data/master/config/musicdataconfig.ts) - Master data API configuration
- [lastfmconfig.ts](../src/music/data/raw/lastfm/config/lastfmconfig.ts) - LastFM API configuration
- [musicquizconfig.ts](../src/music/quiz/config/musicquizconfig.ts) - Quiz API configuration

## Type Definitions

### Shared Types

Location: [src/music/shared/types/](../src/music/shared/types/)

- [entities.ts](../src/music/shared/types/entities.ts) - Base entity interfaces and implementations
- [page.ts](../src/music/shared/types/page.ts) - Pagination types
- [lookup.ts](../src/music/shared/types/lookup.ts) - Lookup configuration types

### Domain-Specific Types

Each domain module has its own types directory with entity-specific definitions:

- Master: [src/music/data/master/types/](../src/music/data/master/types/)
- LastFM: [src/music/data/raw/lastfm/types/](../src/music/data/raw/lastfm/types/)
- Quiz: [src/music/quiz/types/](../src/music/quiz/types/)

## Related Documentation

- [API Integration Overview](./api-integration/README.md) - API client architecture
- [React Query Overview](./patterns/react-query/README.md) - State management patterns
- [Component Index](./components/index.md) - Component reference
- [Entity Types Pattern](./patterns/data-types/entity-types.md) - Entity type system details
