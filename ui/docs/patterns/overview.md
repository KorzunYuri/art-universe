# UI Patterns Documentation

This directory documents reusable implementation patterns used throughout the Music UI application.

## Available Patterns

### Data Types & Validation
Patterns for type definitions, runtime validation, and entity state management.

- [Entity Types](data-types/entity-types.md) - TypeScript type definitions for entities
- [Type Guards](data-types/type-guards.md) - Runtime type validation utilities
- [Entity Validation](data-types/entity-validation.md) - Entity data validation patterns

### Lookup & Search
Patterns for entity lookup, autocomplete, and search functionality.

- [Lookup Registry](lookup/lookup-registry.md) - Centralized lookup configuration registry
- [Lookup Context](lookup/lookup-context.md) - Context-based lookup system
- [Context Factory](lookup/context-factory.md) - Factory for creating lookup contexts
- [Debounced Lookup](lookup/debounced-lookup.md) - Debouncing for search inputs
- [Batch Lookup](lookup/batch-lookup.md) - Batch loading of entity lookups

### Entity Binding
Patterns for mapping and binding between different entity types.

- [Binding Mappers](binding/binding-mappers.md) - Mapping between raw and master entities

### Data Fetching
General data fetching patterns for loading and caching data.

- [Data Source](data-fetching/data-source.md) - Abstraction for data sources (LastFM, future sources)
- [Pagination](data-fetching/table-pagination.md) - Pagination UX patterns for tables

### React Query
Server state management patterns using TanStack Query.

- [React Query Overview](react-query/README.md) - Introduction and core concepts
- [Query Key Structure](react-query/key-structure.md) - Hierarchical key factories
- [Hook Patterns](react-query/hooks.md) - useQuery and useMutation patterns
- [Cache Invalidation](react-query/cache-invalidation.md) - Invalidation strategies after mutations
- [Table Caching](react-query/table-caching.md) - Complete table data management: selective entity caching, prefetching, and optimization
