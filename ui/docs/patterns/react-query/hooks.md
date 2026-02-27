# React Query Hook Patterns

## What It Is

Patterns for creating and using React Query hooks (useQuery and useMutation) to manage server state.

## Why It Exists

Provides consistent patterns for data fetching, mutations, and cache management across the application.

## Hook Types

### Query Hooks (Read Operations)

Use `useQuery` for fetching data from server.

**Location Pattern:** `{module}/hooks/use{Entity}{Operation}.ts`

### Mutation Hooks (Write Operations)

Use `useMutation` for creating, updating, or deleting data.

**Location Pattern:** `{module}/hooks/use{Entity}{Action}.ts`

## Query Hook Patterns

### Pattern 1: Simple Entity Fetch

**Use Case:** Fetch single entity by ID

**Implementation Pattern:**
- Function name: `use{Module}{Entity}`
- Parameters: `id` (entity ID)
- Returns: `{ entity, isLoading, isError, error }`
- Additional methods: `update()`, `invalidate()`

**Example:** [useMasterEntity](../../src/music/data/master/hooks/useMasterEntity.ts)

**Features:**
- Automatic caching by ID
- Cache update method for optimistic updates
- Cache invalidation method for refetch

### Pattern 2: Paginated List

**Use Case:** Fetch paginated collection of entities

**Implementation Pattern:**
- Function name: `use{Module}{Entity}Table`
- Parameters: `entityType`
- Returns: Pagination state + data + navigation functions
- Features: Search, filter, sort, prefetch next page

**Example:** [useMasterEntityTable](../../src/music/data/master/hooks/useMasterEntityTable.ts)

**Returns:**
- `entities`: Current page items
- `isLoading`: Loading state
- `isError`: Error state
- `page`: Current page number
- `totalPages`: Total page count
- `nextPage()`: Navigate to next page
- `prevPage()`: Navigate to previous page
- `goToPage(page)`: Navigate to specific page
- `setSearch(search)`: Set search term
- `setSort(sort)`: Set sort order

### Pattern 3: Entity Lookup (Autocomplete)

**Use Case:** Search entities for autocomplete/picker

**Implementation Pattern:**
- Function name: `useEntityLookup`
- Parameters: `dataSource`, `entityType`, `params`
- Returns: `{ currentOptions, isLoading, isError, error }`
- Features: Debounced search, empty search handling

**Example:** [useEntityLookup](../../../src/shared/hooks/useEntityLookup.ts)

**Configuration:**
- `enabled`: Only run if search string meets minimum length
- `staleTime`: Longer stale time for lookup results (5 minutes)
- Uses LookupRegistry for data source abstraction

### Pattern 4: Entity with Relations

**Use Case:** Fetch entity with nested relations

**Implementation Pattern:**
- Function name: `use{Entity}With{Relations}`
- Parameters: `id`
- Returns: Entity with populated relations
- Uses `withRelations` key variant

**Example:** [useArtistWithCategories](../../../src/music/data/master/hooks/useArtistWithCategories.ts)

### Pattern 5: Dependent Query

**Use Case:** Query B depends on data from Query A

**Implementation Pattern:**
- First query fetches prerequisite data
- Second query uses `enabled` option to wait for first query
- `enabled: !!prerequisiteData`

**Example:** Category with parents (fetch category, then fetch parent details)

## Mutation Hook Patterns

### Pattern 1: Create Entity

**Use Case:** Create new entity

**Implementation Pattern:**
- Function name: `useCreate{Entity}`
- Parameters via `mutate()`: Entity fields
- `onSuccess`: Invalidate list queries, show success notification
- `onError`: Show error notification

### Pattern 2: Update Entity

**Use Case:** Update existing entity

**Implementation Pattern:**
- Function name: `useUpdate{Entity}` or `useSave{Entity}`
- Parameters via `mutate()`: `id` + updated fields
- `onSuccess`: Invalidate detail and list queries
- `onError`: Show error notification

### Pattern 3: Delete Entity

**Use Case:** Delete entity

**Implementation Pattern:**
- Function name: `useDelete{Entity}`
- Parameters via `mutate()`: `id`
- `onSuccess`: Invalidate list queries
- `onError`: Show error notification

### Pattern 4: Approval Status Update

**Use Case:** Update approval status for raw entity

**Implementation Pattern:**
- Function name: `useLastfmEntityApproval`
- Features: Optimistic updates, auto-approve before binding
- Returns: `{ setApprovalStatus, ensureIsValidForBinding }`

**Example:** [useLastfmEntityApproval](../../src/music/data/raw/lastfm/hooks/useLastfmEntityApproval.ts)

**Optimistic Update Steps:**
1. `onMutate`: Update cache optimistically, save previous state
2. Server request
3. `onSuccess`: Confirmed, keep optimistic update
4. `onError`: Rollback to previous state
5. `onSettled`: Refetch to ensure consistency

### Pattern 5: Entity Binding

**Use Case:** Bind raw entity to master entity

**Implementation Pattern:**
- Function name: `use{Module}Binding`
- Parameters via `mutate()`: Binding details
- `onSuccess`: Invalidate both raw and master caches, reload

**Example:** [useQuizBinding](../../src/music/quiz/hooks/useQuizBinding.ts)

**Cache Invalidation:**
- Invalidate raw entity lookup keys
- Invalidate master entity lookup keys
- Reload entity with updated binding

## Hook Composition

### Custom Hook Wrapping Query

Create domain-specific hooks by wrapping `useQuery`:

**Pattern:**
- Import `useQuery` from React Query
- Define query key using key factory
- Define query function using API function
- Add custom logic (e.g., transformation, computed properties)
- Return custom interface

### Custom Hook Wrapping Mutation

Create domain-specific hooks by wrapping `useMutation`:

**Pattern:**
- Import `useMutation` from React Query
- Define mutation function using API function
- Add `onSuccess` handler for cache invalidation
- Add `onError` handler for error notification
- Return mutation interface

## State Management in Hooks

### Query State

- `data`: Fetched data (undefined during loading)
- `isLoading`: Initial load state
- `isFetching`: Background refetch state
- `isError`: Error occurred
- `error`: Error object
- `isSuccess`: Data loaded successfully

### Mutation State

- `mutate()`: Trigger mutation
- `isLoading`: Mutation in progress
- `isError`: Mutation failed
- `isSuccess`: Mutation succeeded
- `reset()`: Reset mutation state

## Hook Return Patterns

### Minimal Return

Return only React Query state:
- `data`, `isLoading`, `isError`, `error`

### Enhanced Return

Add domain-specific methods:
- `update()`: Update cache
- `invalidate()`: Trigger refetch
- Custom computed properties

### Full Control Return

Return everything:
- All React Query state
- Custom methods
- Pagination controls
- Search/filter/sort setters

## Related Documentation

- [Query Key Structure](./key-structure.md) - Keys used in hooks
- [Cache Invalidation](./cache-invalidation.md) - Invalidation in mutations
- [API Functions](../../api-integration/api-functions-patterns.md) - Functions called by hooks
- [Pagination Pattern](../data-fetching/table-pagination.md) - Paginated hooks
- [Table Caching](./table-caching.md) - Table data management with hooks
