# React Query Overview

## Introduction

React Query (TanStack Query) manages all server state in the Music UI module, providing automatic caching, background updates, loading states, and optimistic updates.

## Core Philosophy

- **Declarative data fetching** - Use hooks, not imperative calls
- **Automatic caching** - Query results cached by key
- **Background refetching** - Stale data updated in background
- **Optimistic updates** - UI updates before server confirms
- **No manual state** - React Query manages all server state

## QueryClient Configuration

Location: [src/shared/providers/QueryProvider.tsx](../../../src/shared/providers/QueryProvider.tsx)

**Default Options:**

**Queries:**
- `staleTime`: 5 minutes (5 * 60 * 1000ms)
- `cacheTime`: 10 minutes (10 * 60 * 1000ms)
- `retry`: 1 (retry failed requests once)
- `refetchOnWindowFocus`: true
- `refetchOnReconnect`: true

**Mutations:**
- `retry`: 0 (don't retry mutations)

## DevTools

React Query DevTools enabled in development for debugging cache state.

Location: [src/shared/providers/QueryProvider.tsx](../../../src/shared/providers/QueryProvider.tsx)

## Documentation Structure

This section contains detailed documentation on React Query patterns:

- [Query Key Structure](./key-structure.md) - Hierarchical key factories
- [Hook Patterns](./hooks.md) - useQuery and useMutation patterns
- [Cache Invalidation](./cache-invalidation.md) - Invalidation strategies
- [Table Caching](./table-caching.md) - Batch request + per-entity caching

## Usage Pattern

1. **Define query key** using key factory
2. **Create React Query hook** (useQuery or useMutation)
3. **Use hook in component** to access data/loading/error states
4. **Handle cache invalidation** after mutations

## Key Concepts

### Query Keys

Hierarchical keys identify cached data. See [Query Key Structure](./key-structure.md).

### Query Functions

Functions that fetch data from API. See [API Functions](../api-integration/api-functions-patterns).

### Queries

Read operations using `useQuery`. See [Hook Patterns](./hooks.md).

### Mutations

Write operations using `useMutation`. See [Hook Patterns](./hooks.md).

### Cache Invalidation

Trigger refetch after mutations. See [Cache Invalidation](./cache-invalidation.md).

### Optimistic Updates

Update UI before server confirms. See [Hook Patterns](./hooks.md).

## Integration with API Layer

React Query hooks consume API functions defined in [API Integration](../../api-integration/README.md).

**Flow:**
1. Component uses React Query hook
2. Hook calls API function
3. API function makes HTTP request
4. Response mapped to domain entity
5. Entity cached by React Query
6. Component renders with data

## Related Documentation

- [API Integration Overview](../../api-integration/README.md) - API functions
- [Package Structure](../../package-structure.md) - Hook locations
- [Pagination Pattern](../data-fetching/table-pagination) - Paginated data with React Query
