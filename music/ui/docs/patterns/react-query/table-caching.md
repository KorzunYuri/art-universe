# Table Caching Pattern

## What It Is

Pattern for caching paginated table data where each page request initializes individual entity caches, avoiding N+1 query problem when rows access single-entity data.

## Why It Exists

Optimizes table rendering performance by pre-caching individual entities from page response, preventing redundant requests when table rows need entity details.

## The Problem

**Without Table Caching:**
1. Load page of 20 artists
2. Each table row component needs artist details
3. Each row triggers separate query (20 requests!)
4. Slow loading, unnecessary server load

**With Table Caching:**
1. Load page of 20 artists
2. Initialize cache for each artist from page response
3. Each row reads from cache (0 additional requests!)
4. Fast loading, efficient caching

## Implementation

Location: [useMasterEntityTable](../../src/music/data/master/hooks/useMasterEntityTable.ts)

### Step 1: Fetch Page with Relations

**Purpose:** Get full entity data including relations

**Function:** `fetchMasterEntitiesWithRelations()`

**Key:** `masterEntitiesKeys.withRelations(entityType, params)`

**Result:** Page of entities with all needed data for table display

### Step 2: Initialize Individual Entity Caches

**Purpose:** Cache each entity from page individually

**Method:** `queryClient.setQueryData()`

**For Each Entity in Page:**
- Key: `masterEntitiesKeys.withRelationsDetail(entityType, entity.id)`
- Data: `entity` (from page response)

**Benefits:**
- Subsequent detail queries hit cache
- No additional requests needed
- Instant data for row components

### Step 3: Row Components Use Cached Data

**Purpose:** Table rows read from pre-populated cache

**Pattern:**
- Row component uses `useMasterEntity()` or similar
- React Query checks cache first
- Finds pre-populated data from step 2
- Returns immediately without server request

## Prefetching Pattern

### Background Prefetch Next Page

**Purpose:** Prepare next page while user views current page for instant navigation

**Implementation:**
- Use `queryClient.prefetchQuery()` in `useEffect`
- Trigger after current page loads successfully
- Only prefetch if next page exists (`hasNextPage`)
- Same caching logic applies to prefetched page

**Prefetch Parameters:**
- Same query key structure as regular page
- Page number: `currentPage + 1`
- Same size, sort, search parameters

**Cache Management:**
- Prefetched data follows same cache rules
- Same invalidation strategies apply
- Stale time and cache time inherited

### User Experience Comparison

**Without Prefetch:**
1. User clicks "Next"
2. Loading state shown
3. Request sent
4. Wait for response
5. Page displays
6. **Total delay: ~500ms-2s**

**With Prefetch:**
1. User views page 0
2. Page 1 prefetches in background
3. User clicks "Next"
4. Page 1 instantly displayed (from cache)
5. **Total delay: ~0ms**

### Combined Benefits with Selective Caching

When prefetch and selective caching work together:
1. Next page prefetched in background
2. Response returns with entities
3. Individual entity caches initialized from response
4. User navigates to next page → instant display
5. Row components render → instant (cache hits)

### Edge Cases

**Last Page:**
- No next page exists
- Prefetch not triggered
- No wasted requests

**Search/Filter Change:**
- Cache keys different for new parameters
- Old prefetch becomes irrelevant
- New prefetch triggered for new context

**Error on Current Page:**
- Prefetch not triggered
- Avoids cascading failures

### Configuration & Trade-offs

**Prefetch Timing:**
- Current: Immediate after page load success
- Alternative: Delayed for slower connections

**Prefetch Scope:**
- Current: Next page only
- Possible: Next N pages (increases memory/network)

**Trade-off Analysis:**
- **Cost:** 1 extra request (may go unused if user doesn't navigate)
- **Benefit:** Instant navigation (when used)
- **Memory:** One additional page in cache (usually negligible)
- **Network:** Background request in idle time
- **Verdict:** Generally worth it for better UX

## Cache Consistency

### Page-Level Cache

**Key:** `masterEntitiesKeys.withRelations(entityType, { page, size, sort, search })`

**Contents:** Full page response (content array + pagination metadata)

### Entity-Level Cache

**Key:** `masterEntitiesKeys.withRelationsDetail(entityType, entityId)`

**Contents:** Individual entity with relations

### Invalidation Strategy

**After Mutation:**
- Invalidate page-level cache: `masterEntitiesKeys.type(entityType)`
- Entity-level caches auto-invalidated by prefix match
- Page refetch re-initializes all entity caches

## Performance Characteristics

### Initial Page Load

- **Request Count:** 1 (page request)
- **Cache Writes:** N+1 (1 page + N entities)
- **User Experience:** Standard loading time

### Row Rendering

- **Request Count:** 0 (all cache hits)
- **Cache Reads:** N (one per row)
- **User Experience:** Instant rendering

### Next Page Navigation (With Prefetch)

- **Request Count:** 0 (prefetched in background)
- **Cache Writes:** N+1 (from prefetch)
- **User Experience:** Instant navigation

### Next Page Navigation (Without Prefetch)

- **Request Count:** 1 (page request)
- **Cache Writes:** N+1
- **User Experience:** Standard loading time

## Comparison with Alternative Approaches

### Alternative 1: No Individual Caching

**Approach:** Only cache page, row components access page data

**Drawbacks:**
- Row components tightly coupled to page structure
- Can't use entity detail hooks
- Difficult to handle row actions (e.g., expand for details)

### Alternative 2: Lazy Load per Row

**Approach:** Each row fetches its own data

**Drawbacks:**
- N requests for N rows (N+1 problem)
- Slow table rendering
- Unnecessary server load
- Poor user experience

### Table Caching Pattern (Current)

**Advantages:**
- Single request for page
- Individual entity hooks work
- Efficient caching
- Great user experience
- Extensible (easy to add row details, actions, etc.)

## Implementation Files

- [useMasterEntityTable.ts](../../src/music/data/master/hooks/useMasterEntityTable.ts) - Table hook with caching
- [useMasterEntity.ts](../../src/music/data/master/hooks/useMasterEntity.ts) - Individual entity hook (reads cache)
- [music-data-common-fetching.ts](../../src/music/data/master/api/music-data-common-fetching.ts) - Page fetch functions

## Related Documentation

- [Cache Invalidation](./cache-invalidation.md) - Invalidation strategies after mutations
- [Pagination Pattern](../data-fetching/table-pagination.md) - Pagination UX implementation
- [Query Key Structure](./key-structure.md) - Key organization for cache entries
- [Hook Patterns](./hooks.md) - React Query hook usage patterns
