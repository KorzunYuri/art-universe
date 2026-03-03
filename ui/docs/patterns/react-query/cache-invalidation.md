# Cache Invalidation Strategies

## What It Is

Strategies for invalidating React Query cache to trigger data refetch after mutations or external changes.

## Why It Exists

Keeps UI synchronized with server state after data modifications, ensuring users see up-to-date information without manual refresh.

## Invalidation Methods

### queryClient.invalidateQueries()

Marks queries as stale and triggers refetch.

**Access:** Get `queryClient` from `useQueryClient()` hook

## Strategy 1: Invalidate All Queries for Entity Type

**Use Case:** Mutation affects multiple queries of same entity type

**Pattern:**
- Invalidate using type-level key from key factory
- All queries of that type refetch

**Example:** After creating/updating/deleting artist
- Key: `masterEntitiesKeys.type('artist')`
- Effect: Refetches all artist lists, all artist details, all artist lookups

**When to Use:**
- Don't know which specific queries are affected
- Mutation may affect multiple entities (e.g., bulk operation)
- Safest approach (ensures consistency)

## Strategy 2: Invalidate Specific Query

**Use Case:** Know exactly which query is affected

**Pattern:**
- Invalidate using specific key (detail, list with params, etc.)
- Only matching queries refetch

**Example:** After updating specific artist
- Key: `masterEntitiesKeys.detail('artist', artistId)`
- Effect: Refetches only that artist's detail query

**When to Use:**
- Mutation affects single entity
- Want to minimize refetch overhead
- Know exact cache entry to invalidate

## Strategy 3: Update Cache Directly (No Refetch)

**Use Case:** Have new data from mutation response

**Pattern:**
- Use `queryClient.setQueryData()` to update cache
- No server request needed

**Example:** After creating artist
- Key: `masterEntitiesKeys.detail('artist', newArtist.id)`
- Data: `newArtist` from mutation response

**When to Use:**
- Mutation response contains full updated entity
- Want to avoid extra server request
- Optimizing for performance

## Strategy 4: Invalidate Related Entities

**Use Case:** Mutation affects relationships between entities

**Pattern:**
- Invalidate multiple entity type keys
- All related queries refetch

**Example:** After binding raw artist to master artist
- Keys:
  - `rawEntitiesKeys.detail('lastfm', 'artist', rawArtistId)`
  - `masterEntitiesKeys.detail('artist', masterArtistId)`
- Effect: Both raw and master artist queries refetch

**When to Use:**
- Mutation creates/updates/deletes relationships
- Multiple entity types affected
- Need to keep relations synchronized

## Strategy 5: Invalidate Then Set

**Use Case:** Partial update with known changes

**Pattern:**
1. Invalidate lookup caches (to get fresh search results)
2. Set specific detail cache with known data
3. Avoid refetching detail (already have data)

**Example:** After binding entity
1. Invalidate: `entityLookupKeys.type('master', 'artist')`
2. Set: `masterEntitiesKeys.detail('artist', masterId)` with bound entity

**When to Use:**
- Have partial data from mutation
- Want fresh lookup results but avoid detail refetch
- Optimizing for specific use case

## Strategy 6: Invalidate on Relationship Change

**Use Case:** Entity relationship modified (e.g., artist categories, category parents)

**Pattern:**
- Invalidate entity with relations keys
- Regular detail queries unaffected
- Only relation-inclusive queries refetch

**Example:** After adding category to artist
- Key: `masterEntitiesKeys.withRelationsDetail('artist', artistId)`
- Effect: Refetches artist with categories, not plain artist detail

**When to Use:**
- Relationship changed but base entity unchanged
- Using separate queries for with/without relations
- Want granular control over refetch

## Invalidation Timing

### In onSuccess Callback

**Most Common Pattern:**
- Mutation completes successfully
- Invalidate in `onSuccess` callback
- Queries refetch with updated data

### In onSettled Callback

**Use When:**
- Want invalidation regardless of success/failure
- Error may still affect cached data
- Ensure consistency after any mutation completion

## Optimistic Updates with Invalidation

**Pattern:**
1. `onMutate`: Update cache optimistically
2. Mutation executes
3. `onError`: Rollback optimistic update
4. `onSettled`: Invalidate to ensure consistency

**Benefits:**
- Instant UI feedback (optimistic update)
- Guaranteed consistency (invalidation after)
- Rollback on error

## Implementation Locations

### In Mutation Hooks

Location: `{module}/hooks/use{Entity}{Action}.ts`

**Example:** [useLastfmEntityApproval](../../src/music/data/raw/lastfm/hooks/useLastfmEntityApproval.ts)

**Pattern:**
- Define `useMutation` hook
- Add `onSuccess` with invalidation logic
- Export hook for component use

### In Components

**Direct Usage:**
- Get `queryClient` from `useQueryClient()`
- Call `invalidateQueries()` after custom operations

## Prefetching After Invalidation

**Pattern:**
- Invalidate current page
- Prefetch next page in background
- User sees updated current page
- Next page ready when navigating

**Example:** [useMasterEntityTable](../../src/music/data/master/hooks/useMasterEntityTable.ts)

## Multiple Invalidations

**Pattern:**
- Chain multiple `invalidateQueries()` calls
- Each invalidation targets different cache subset
- All affected queries refetch

**Example:** After batch operation
- `invalidateQueries(masterEntitiesKeys.type('artist'))`
- `invalidateQueries(masterEntitiesKeys.type('album'))`
- `invalidateQueries(entityLookupKeys.dataSource('master'))`

## Related Documentation

- [Query Key Structure](./key-structure.md) - Keys used for invalidation
- [Hook Patterns](./hooks.md) - Using invalidation in hooks
- [Table Caching](./table-caching.md) - Cache management for tables
