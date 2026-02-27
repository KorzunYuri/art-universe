# Pagination Pattern

## What It Is

Standardized pagination implementation using Spring Boot Page format, integrated with React Query for efficient data fetching and caching.

## Why It Exists

Provides consistent pagination across all entity lists, enables efficient data loading, and integrates seamlessly with backend pagination format.

## Pagination Types

Location: [src/shared/types/page.ts](../../../src/shared/types/page.ts)

### Page<T> Interface

**Fields:**
- `content`: Array of items (type T)
- `totalElements`: Total item count across all pages
- `totalPages`: Total page count
- `number`: Current page number (0-indexed)
- `size`: Page size (items per page)
- `first`: Boolean - is first page
- `last`: Boolean - is last page
- `pageable`: Pagination metadata
- `sort`: Sort metadata
- `numberOfElements`: Items in current page
- `empty`: Boolean - is page empty

### BasePageSearchParams Interface

**Fields:**
- `page`: Page number (required)
- `search`: Search term (optional)
- `size`: Page size (optional, default 20)
- `sort`: Sort order (optional, default 'name,asc')

## React Query Integration

### useMasterEntityTable Hook

Location: [useMasterEntityTable.ts](../../src/music/data/master/hooks/useMasterEntityTable.ts)

**Features:**
- Manages pagination state (page, size, sort, search)
- Automatic cache management
- Prefetch next page
- Individual entity caching from page response

**Returns:**
- `entities`: Current page items
- `page`: Current page number
- `totalPages`: Total page count
- `hasNextPage`: Boolean
- `hasPrevPage`: Boolean
- `nextPage()`: Navigate to next page
- `prevPage()`: Navigate to previous page
- `goToPage(page)`: Navigate to specific page
- `setSearch(search)`: Update search term
- `setSort(sort)`: Update sort order
- `isLoading`: Loading state
- `isError`: Error state

## Pagination State Management

**Implementation:** `useState` for parameters + `useQuery` for data

**State Variables:**
- `page`: Current page number (0-indexed)
- `size`: Items per page (default 20)
- `sort`: Sort order (default 'name,asc')
- `search`: Search term (default empty)

**State Updates:**
- Page navigation: Update `page`, keep other params
- Search: Reset to page 0, update `search`
- Sort: Reset to page 0, update `sort`

## Navigation Functions

### nextPage()

**Logic:**
- Check `hasNextPage`
- If true: `setPage(page + 1)`

### prevPage()

**Logic:**
- Check `hasPrevPage` (page > 0)
- If true: `setPage(page - 1)`

### goToPage(targetPage)

**Logic:**
- Validate page number (0 <= targetPage < totalPages)
- If valid: `setPage(targetPage)`

## Prefetching

**Purpose:** Load next page in background while user views current page

**Implementation:**
- After page loads successfully
- Check if next page exists
- Call `queryClient.prefetchQuery()` with next page params

**Benefits:**
- Instant navigation to next page
- Pre-populated entity caches
- Smooth user experience

## Cache Key Pattern

**Page-Level:**
- Key: `masterEntitiesKeys.withRelations(entityType, { page, size, sort, search })`
- Different params = different cache entries

**Entity-Level:**
- Key: `masterEntitiesKeys.withRelationsDetail(entityType, entityId)`
- Populated from page response

## Default Values

**Standard Defaults:**
- Page size: 20 items
- Sort order: 'name,asc'
- Initial page: 0

**Customization:** Can be overridden per table if needed

## Backend Format

**Spring Boot Page JSON:**
- Zero-indexed pages
- Includes pagination metadata
- Sort format: `{field},{direction}`

## Related Patterns

- [Table Caching](../react-query/table-caching.md) - Complete table caching strategy including prefetch and selective entity caching

## Related Documentation

- [React Query Hooks](../react-query/hooks.md) - Pagination hook patterns
- [API Functions](../../api-integration/api-functions-patterns.md) - Paged fetch functions
- [Cache Invalidation](../react-query/cache-invalidation.md) - Invalidation after mutations
