# Batch Lookup Optimization

## What It Is

Execute multiple lookup queries in single API request, reducing request count and improving performance.

## Why It Exists

Optimizes scenarios where multiple lookups needed simultaneously, reduces server round trips, and enables efficient parallel lookups.

## Location

[src/music/data/master/api/music-data-common-lookup.ts](../../src/music/data/master/api/music-data-common-lookup.ts)

## How It Works

### Single Lookup

**Request:**
- Search term: "Beatles"
- Limit: 10

**Response:**
- Array of matching entities

**Endpoint:** `POST /api/v1/{entity}/lookup`

### Batch Lookup

**Request:**
- Multiple search requests in array
- Each with own search term and params
- Single limit for all

**Response:**
- Map of search term to results
- All results in one response

**Endpoint:** `POST /api/v1/{entity}/lookup/batch`

## Request Structure

**Batch Lookup Request:**
- `searchRequests`: Array of lookup requests
  - Each request: Search term + optional filters
- `limit`: Max results per search

**Example:**
- Request 1: Search "Beatles"
- Request 2: Search "Stones"
- Request 3: Search "Zeppelin"
- Limit: 10
- Result: 3 searches in 1 API call

## Response Structure

**BatchLookupResponseDTO:**
- Map of source params to results
- Type-safe indexing by search params

## Use Cases

### Prefilling Form Fields

**Scenario:** Form with multiple entity selectors

**Benefit:**
- Single request loads all options
- Form renders faster
- Better user experience

### Related Entity Lookups

**Scenario:** Load albums for multiple artists

**Benefit:**
- Batch lookup all artists
- Faster than sequential lookups
- Reduced latency

### Cache Warming

**Scenario:** Preload common searches

**Benefit:**
- Single request populates cache
- Subsequent lookups hit cache
- Improved performance

## Performance Comparison

**Sequential Lookups (3 searches):**
- Requests: 3
- Latency: 3 × (request time + server time)
- Total: ~300-900ms

**Batch Lookup (3 searches):**
- Requests: 1
- Latency: 1 × (request time + server time)
- Total: ~100-300ms

**Improvement:** 3× faster for 3 searches

## Implementation Details

### Source Params Transformation

**Pattern:**
- Component provides array of high-level params
- Transform each to API request format
- Combine into batch request

### Response Processing

**Pattern:**
- Receive map of results
- Match results to original requests
- Return structured response

## Limitations

**Max Batch Size:**
- Practical limit: ~10-20 searches
- Large batches may timeout
- Consider splitting very large batches

**Same Entity Type:**
- All searches must be same entity type
- Can't mix artists and albums in one batch

## Related Patterns

- [API Functions](../api-integration/api-functions-patterns) - Lookup function patterns
- [Lookup Registry](./lookup-registry.md) - Single lookup pattern

## Related Documentation

- [DTO Mapping](../api-integration/api-dto-mapping) - Response mapping
