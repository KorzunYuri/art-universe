# API Patterns Overview

This document provides a quick reference guide to all API-related patterns used in the Art Universe project.

## Available Patterns

### [REST API Conventions](conventions.md)

**Purpose**: Standard patterns and conventions for building REST APIs in Spring Boot modules.

**When to use**:
- Creating new REST controllers
- Adding new API endpoints
- Reviewing API design
- Ensuring consistency across services

**Provides**:
- Endpoint naming conventions (`/api/v1/{entity}`)
- Standard CRUD operations (GET, POST, PUT, DELETE)
- Pagination support with Spring Data
- Response status codes and error handling
- Request/response DTO patterns

**Examples**: Artist API, Album API, Track API endpoints

---

### [Lookup Pattern](lookup.md)

**Purpose**: Minimal-data entity lookup optimized for form controls, dropdowns, and quick selection with fast, lightweight queries.

**When to use**:
- Form dropdowns (`<select>` elements)
- Autocomplete fields (type-ahead search)
- Quick entity selection
- Client-side filtering (small datasets)
- Related entity selection in forms

**Provides**:
- Lightweight lookup endpoint pattern
- Query parameters for filtering and limiting
- Minimal response format (id + name)
- Fast performance for UI components

**Examples**: Artist lookup for binding forms, Category selection dropdowns

---

## Common API Design Principles

All API patterns in Art Universe follow these core principles:

### 1. Versioning
- All endpoints prefixed with `/api/v1/`
- Version in URL path for clear compatibility
- Major version changes require new endpoint versions

### 2. RESTful Resource Design
- Resources are nouns, not verbs
- Use HTTP methods for operations (GET, POST, PUT, DELETE)
- Hierarchical resource paths (`/api/v1/games/{gameId}/generations`)

### 3. Response Consistency
- Standard DTO objects for all responses
- Consistent error response format
- HTTP status codes follow REST conventions

### 4. Pagination
- Use Spring Data Pageable for large datasets
- Query parameters: `page`, `size`, `sort`
- Response includes page metadata

### 5. Query Optimization
- Lookup endpoints for minimal data retrieval
- Full CRUD endpoints for complete entity access
- Separate endpoints for different use cases

---

## Pattern Relationships

```
REST API Conventions
    ├─> Used by: All controller endpoints
    └─> Defines: Standard CRUD operations

Lookup Pattern
    ├─> Extends: REST API Conventions
    ├─> Optimizes: Entity selection for UI
    └─> Used by: Form controls, autocomplete fields
```

---

## Related Patterns

### Entity Patterns
- **[Entity Patterns Overview](../entities/overview.md)** - JPA entity design that APIs expose

### Database Patterns
- **[Database Patterns Overview](../database/overview.md)** - Schema design for API data

### Testing Patterns
- **[Testing Patterns Overview](../testing/overview.md)** - Testing API endpoints
- **[Testing Controllers](../testing/testing-controllers.md)** - Controller testing approach

---

## Deep-Dive Documentation

For detailed implementation guides:

1. **REST API Conventions**: See [conventions.md](conventions.md)
2. **Lookup Pattern**: See [lookup.md](lookup.md)

For module-specific API implementations, see individual module documentation in the [Project Modules Index](../../../../MODULES.md).
