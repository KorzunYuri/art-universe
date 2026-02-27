# API Integration Overview

The Music UI module integrates with multiple backend APIs. This document lists the principles.


## Core Principles

- HTTP client - Axios
  - One function per endpoint - Clear API function boundaries
  - Type-safe API calls - Full TypeScript coverage with DTOs
  - Centralized error handling - Axios interceptors transform errors
  - API functions are consumed through React Query hooks. See [React Query documentation](../react-query/README.md) for patterns.
- Server state management - [React Query](../react-query/README.md).
  - No manual state management for API data
  - One exception is page requests: after we receive a page of items we update individual items in cache 


## Backend APIs

The module integrates with four distinct backend APIs:

### LastFM Read API

- Purpose: Raw entity data (read-only)
- Location: [lastfmconfig.ts](../../src/music/data/raw/lastfm/config/lastfmconfig.ts)

### LastFM Write API

- Purpose: Approval operations and maintenance
- Location: [lastfmconfig.ts](../../src/music/data/raw/lastfm/config/lastfmconfig.ts)

### Music Data Master API

- Purpose: Master entities CRUD + binding operations
- Location: [musicdataconfig.ts](../../src/music/data/master/config/musicdataconfig.ts)

### Music Quiz API

- Purpose: Quiz generation and management
- Location: [musicquizconfig.ts](../../src/music/quiz/config/musicquizconfig.ts)


## Environment Configuration

Environment variables are defined in `.env` file


## Aspects

- [DTO mapping](api-dto-mapping.md)
- [Error Handling](api-error-handling.md)
- [API Functions Patterns](api-functions-patterns.md)
