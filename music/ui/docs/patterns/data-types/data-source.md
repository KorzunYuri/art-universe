# Data Source Abstraction

## What It Is

Type-safe abstraction layer for multiple external data sources, enabling generic operations across different sources while preserving source-specific behavior.

## Why It Exists

Supports multiple external data sources (LastFM, future: Spotify, MusicBrainz), provides unified interface for raw entity operations, and enables source-agnostic components.

## Data Source Type

Location: [src/music/data/raw/shared/types/data-sources.ts](../../src/music/data/raw/shared/types/data-sources.ts)

**Type Definition:**
- `type DataSource = 'lastfm'`

**Extensibility:** Add new sources by extending union type

## Dispatcher Pattern

### Approval Status Update Dispatcher

Location: [src/music/data/raw/shared/api/approval.tsx](../../src/music/data/raw/shared/api/approval.tsx)

**Purpose:** Route approval updates to correct source-specific API

**Implementation:**
- Function takes `dataSource` parameter
- Switch statement dispatches to source-specific function
- Type-safe per-source approval status types

**Pattern:**
- Generic function signature with data source parameter
- Internal dispatch based on data source value
- Source-specific implementations

## Source-Specific Implementations

### LastFM Implementation

**Approval Statuses:**
- `PENDING`
- `APPROVED`
- `DECLINED`
- `AUTOAPPROVED`

**API Module:** [lastfm/api/](../../src/music/data/raw/lastfm/api/)

**Functions:**
- `updateApprovalStatus_Lastfm()`
- Entity-specific CRUD operations
- Source-specific maintenance operations

## Type-Safe Source Handling

### DataSourceApprovalStatus Map

**Purpose:** Map data source to source-specific approval status type

**Structure:**
- `lastfm`: LastfmApprovalStatus
- (Future sources): Their approval status types

**Benefit:** Type checker ensures correct status values per source

### Generic Functions with Source Parameter

**Pattern:**
- Type parameter `DS extends DataSource`
- Source-specific types indexed by `DS`
- Compile-time type safety

## Component Integration

### Source-Agnostic Components

Components work with any data source by:
- Accepting `dataSource` prop
- Using dispatcher functions for operations
- Rendering source-agnostic UI

**Example:** EntityBinding component

### Source-Specific Components

Components tied to specific source:
- Hard-code source value
- Use source-specific types directly
- Access source-specific features

**Example:** LastfmArtistsTable

## Future Extensibility

### Adding New Data Source

**Steps:**
1. Add source to `DataSource` union type
2. Implement source-specific API module
3. Add source case to dispatcher functions
4. Define source-specific approval statuses
5. Register source lookups in LookupRegistry

**Example:** Adding Spotify
- `type DataSource = 'lastfm' | 'spotify'`
- Create `spotify/` module
- Add `case 'spotify':` to dispatchers
- Define Spotify entity types and statuses

## Source Identification

**In RawEntity:**
- `getDataSource()` method returns source
- Used for dispatching operations
- Part of entity interface contract

**In API Requests:**
- Data source passed as parameter
- Backend routes to source-specific handlers
- Preserves source context through stack

## Related Patterns

- [Entity Types](../data-types/entity-types.md) - Raw entity interface with getDataSource()
- [Binding Mappers](../binding/binding-mappers.md) - Source-specific mapper dispatch
- [Lookup Registry](../lookup/lookup-registry.md) - Source-specific lookup registration

## Related Documentation

- [Approval Workflow](../../flows/approval-workflow.md) - Approval status by source
- [Binding Workflow](../../flows/binding-workflow.md) - Source parameter in binding
