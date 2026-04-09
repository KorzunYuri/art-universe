# Lookup Implementation

This document describes the lookup implementation used for entity search and autocomplete functionality in the Music Master Data Management Service.

## Overview

The lookup system provides fast, lightweight search capabilities optimized for UI dropdown/autocomplete components. Unlike full paginated search, lookup operations return limited result sets with minimal data (ID + name).

The implementation uses a template pattern with two specialized service types:
- [MasterEntityLookupService.java](../../src/main/java/yurykorzun/art/universe/music/data/master/service/lookup/MasterEntityLookupService.java) - for basic entity lookups
- [ArtistRelatedLookupService.java](../../src/main/java/yurykorzun/art/universe/music/data/master/service/lookup/ArtistRelatedLookupService.java) - for artist-related entity lookups (adds filter by artist)

## MasterEntityLookupService

### Concept

A type-safe wrapper around [BaseLookupService.java](../../../../../common/commons-jpa/src/main/java/yurykorzun/art/universe/common/service/lookup/BaseLookupService.java) from [commons-jpa](../../../../../common/commons-jpa/README.md) module, providing standard name-based lookup functionality for master entities (Artist, Category).

### How It Works

- Extends `BaseLookupService<MasterEntityType, MasterEntityMetadata>`
- Instantiated per entity type (e.g., ARTIST, CATEGORY)
- Performs simple name-based searches against a single entity table
- Delegates most logic to the base class implementation

## ArtistRelatedLookupService

A specialized lookup service for entities that belong to an artist (Album, Track). Extends `MasterEntityLookupService` with artist-specific filtering and result formatting.

### How It Works

Supports three lookup modes:
1. Name-only search - Search by entity name with optional artist filter
2. Master artist lookup - Filter by internal master artist ID
3. External artist lookup - Filter by external artist ID from a specific data source (LastFM, Spotify, etc.)

### Key Features

- JOINs the artist table to include artist information in results
- Allows searches without a name if artist ID is provided
- Returns results formatted as "ArtistName - EntityName"
- Supports batch lookup for multiple searches in one request


## Key Components

### Service Classes

| Class | Location | Purpose |
|-------|----------|---------|
| [BaseLookupService](../../../../common/commons-jpa/src/main/java/yurykorzun/art/universe/common/service/lookup/BaseLookupService.java) | commons-jpa | Generic base service with core lookup logic |
| [MasterEntityLookupService](../../src/main/java/yurykorzun/art/universe/music/data/master/service/lookup/MasterEntityLookupService.java) | master module | Type-safe wrapper for master entities |
| [ArtistRelatedLookupService](../../src/main/java/yurykorzun/art/universe/music/data/master/service/lookup/ArtistRelatedLookupService.java) | master module | Specialized service for artist-related entities |

### Metadata Classes

| Class | Location | Purpose |
|-------|----------|---------|
| [BaseEntityMetadata](../../../../common/commons-jpa/src/main/java/yurykorzun/art/universe/common/persistence/entity/BaseEntityMetadata.java) | commons-jpa | Base metadata with entity type and table name |
| [MasterEntityMetadata](../../src/main/java/yurykorzun/art/universe/music/data/master/entity/MasterEntityMetadata.java) | master module | Adds binding table and field name helpers |

### Request/Response DTOs

| DTO | Purpose |
|-----|---------|
| [LookupRequestDTO](../../../../common/commons-web/src/main/java/yurykorzun/art/universe/common/dto/LookupRequestDTO.java) | Basic lookup request (search term + limit) |
| [ArtistRelatedLookupRequestDTO](../../src/main/java/yurykorzun/art/universe/music/data/master/dto/ArtistRelatedLookupRequestDTO.java) | Artist-filtered lookup request (adds artist IDs) |
| [BatchLookupRequestDTO](../../../../common/commons-web/src/main/java/yurykorzun/art/universe/common/dto/BatchLookupRequestDTO.java) | Generic batch lookup request |
| [ArtistRelatedBatchLookupRequestDTO](../../src/main/java/yurykorzun/art/universe/music/data/master/dto/ArtistRelatedBatchLookupRequestDTO.java) | Artist-filtered batch lookup request |
| [LookupResultDTO](../../../../common/commons-web/src/main/java/yurykorzun/art/universe/common/dto/LookupResultDTO.java) | Lookup result (id + name) |
| [BatchLookupResponseDTO](../../../../common/commons-web/src/main/java/yurykorzun/art/universe/common/dto/BatchLookupResponseDTO.java) | Batch lookup response (map of search � results) |

### Enums

| Enum | Purpose |
|------|---------|
| [MasterEntityType](../../src/main/java/yurykorzun/art/universe/music/data/master/entity/MasterEntityType.java) | Entity type enum (ARTIST, ALBUM, TRACK, CATEGORY, DIMENSION) |
| [DataSource](../../src/main/java/yurykorzun/art/universe/music/data/master/entity/DataSource.java) | External data source enum (LASTFM, SPOTIFY, MUSICBRAINZ) |


## See Also

- [Master Data API Reference](../api.md) - API endpoints using lookup services
- [Search vs Lookup Pattern](../../../../../docs/kb/patterns/backend/api/search-and-lookup.md) - Distinction between search and lookup operations
