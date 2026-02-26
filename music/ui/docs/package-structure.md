# Package Structure

## Overview

The UI module follows a modular architecture with domain-driven organization. The top-level `src/` directory separates cross-domain shared infrastructure from domain-specific modules (`music/`, `art/`).

## Module Details

### Shared Layer (`src/shared/`)

Cross-cutting infrastructure used by all domain modules. Contains no domain-specific logic.

**Key contents:**
- `components/` — Reusable UI: [DataTable](../src/shared/components/DataTable/), [EntityTable](../src/shared/components/EntityTable/), [EntityPicker](../src/shared/components/EntityPicker/), [EntityLookup](../src/shared/components/EntityLookup/), [EditableText](../src/shared/components/EditableText/), [TableWithDetailLayout](../src/shared/components/TableWithDetailLayout/), [AppHeader](../src/shared/components/AppHeader/), [NavigationCard](../src/shared/components/NavigationCard/), [ConfirmDialog](../src/shared/components/ConfirmDialog/), etc.
- `config/` — [appConfig.ts](../src/shared/config/appConfig.ts): runtime config for all API endpoints
- `hooks/` — [useEntityLookup](../src/shared/hooks/useEntityLookup.ts), [useNotifications](../src/shared/hooks/useNotifications.ts), [useColumnPreferences](../src/shared/hooks/useColumnPreferences.ts)
- `services/` — [LookupRegistry](../src/shared/services/LookupRegistry.ts), [tracingInterceptor](../src/shared/services/tracingInterceptor.ts)
- `types/` — [page.ts](../src/shared/types/page.ts), [lookup.ts](../src/shared/types/lookup.ts), [notification.ts](../src/shared/types/notification.ts)

### Music-Specific Shared (`src/music/shared/`)

Types and utilities that are specific to the music domain.

- `types/entities.ts` — `MasterEntityType` union, entity interfaces and implementations (Artist, Album, Track, Category)
- `hooks/useRawEntity.tsx` — Raw data approval workflow hook
- `utils/query-keys.ts` — Query key factories for music entities (masterEntitiesKeys, rawEntitiesKeys, relationKeys, entityLookupKeys)

### Music Master Data (`src/music/data/master/`)

Canonical music entity management (artists, albums, tracks, categories, relation types).

- `api/` — Master data API client with entity-specific files
- `components/` — Master-specific UI ([ArtistsTable](../src/music/data/master/components/ArtistsTable/), [AlbumsTable](../src/music/data/master/components/AlbumsTable/), [CategoryDag](../src/music/data/master/components/CategoryDag/), etc.)
- `hooks/` — [useMasterEntityTable](../src/music/data/master/hooks/useMasterEntityTable.ts), [useMasterEntity](../src/music/data/master/hooks/useMasterEntity.ts), entity-specific hooks
- `services/` — [registerMasterLookups](../src/music/data/master/services/registerMasterLookups.ts)

### LastFM Module (`src/music/data/raw/lastfm/`)

LastFM external data integration.

- `api/` — LastFM API client with entity-specific files
- `components/` — LastFM-specific tables and row components
- `hooks/` — [useLastfmEntityApproval](../src/music/data/raw/lastfm/hooks/useLastfmEntityApproval.ts)
- `services/` — [registerLastfmLookups](../src/music/data/raw/lastfm/services/registerLastfmLookups.ts)

### Raw Data Shared (`src/music/data/raw/shared/`)

Shared code used across raw data sources (currently LastFM only).

- `api/` — Approval API
- `hooks/` — Approval status filter
- `types/` — Lookup context types, data source definitions
- `components/` — [ApprovalToggle](../src/music/data/raw/shared/components/ApprovalToggle/), [EntityBinding](../src/music/data/raw/shared/components/EntityBinding/), [ArtistRelatedEntityBinding](../src/music/data/raw/shared/components/ArtistRelatedEntityBinding/)

### Quiz Module (`src/music/quiz/`)

Quiz generation and management.

- `api/` — Quiz API client
- `components/` — Quiz-specific UI (GameDetails, PipelineEditor, StepBuilder, etc.)
- `hooks/` — [useQuizBinding](../src/music/quiz/hooks/useQuizBinding.ts), [useQuizData](../src/music/quiz/hooks/useQuizData.ts)
- `types/` — Quiz entity types, generation steps

### Art Master Data (`src/art/data/master/`)

Art domain entity management (persons, future: relations).

- `api/` — [art-data-persons.ts](../src/art/data/master/api/art-data-persons.ts): Person DTOs and CRUD API
- `config/` — [artdataconfig.ts](../src/art/data/master/config/artdataconfig.ts): Axios instance for art-data service
- `hooks/` — [usePersonTable](../src/art/data/master/hooks/usePersonTable.ts), [usePerson](../src/art/data/master/hooks/usePerson.ts)
- `components/` — [PersonsTable](../src/art/data/master/components/PersonsTable/) with inline edit and delete
- `pages/` — ArtDataHome, Persons, PersonDetail
- `utils/` — [query-keys.ts](../src/art/data/master/utils/query-keys.ts): artEntitiesKeys factory

## File Organization Patterns

### API Layer

```
api/
├── {module}-commons.ts           # Entity mappers, endpoint mapping
├── {module}-common-[feature].ts  # Generic functions for specific topic
├── {module}-[topic/entity].ts    # Topic/entity specific DTOs and functions
```

### Component Layer

```
components/
├── {ComponentName}/
│   ├── {ComponentName}.tsx          # Component implementation
│   ├── {ComponentName}.module.css   # Scoped styles
│   └── index.ts                     # Re-export
```

### Hooks Layer

```
hooks/
├── use{Entity}{Operation}.ts     # Entity-specific operations
├── use{Feature}.ts               # Feature-specific logic
└── index.ts                      # Re-exports
```

## Configuration

Each domain module has its own API config under `config/`:
- [appConfig.ts](../src/shared/config/appConfig.ts) — Central runtime config (all endpoints)
- [musicdataconfig.ts](../src/music/data/master/config/musicdataconfig.ts) — Music Master Data API
- [lastfmconfig.ts](../src/music/data/raw/lastfm/config/lastfmconfig.ts) — LastFM API
- [musicquizconfig.ts](../src/music/quiz/config/musicquizconfig.ts) — Quiz API
- [artdataconfig.ts](../src/art/data/master/config/artdataconfig.ts) — Art Master Data API

## Related Documentation

- [API Integration Overview](./api-integration/README.md) — API client architecture
- [React Query Overview](./patterns/react-query/README.md) — State management patterns
- [Component Overview](./components/overview.md) — Component reference
- [Entity Types Pattern](./patterns/data-types/entity-types.md) — Entity type system details
