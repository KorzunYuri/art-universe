# LastFM Data Collector - Architecture

## System Overview

The LastFM Data Collector is a Spring Boot application that systematically collects music metadata from the LastFM API. It implements a sophisticated data collection pipeline with quality control, relationship mapping, and change tracking capabilities.

## Architecture Layers

### 1. API Client Layer
- **`LastfmApiClientImpl`** - HTTP client for LastFM API with rate limiting (1 call/sec)
- **`LastfmApiCall`** - Tracks API call requests with due dates and parameters
- **`LastfmApiResponse`** - Stores API responses with processing status
- **`LastfmApiCallGenerator`** - Generates API calls based on entity state and business logic

### 2. Data Processing Layer
- **`LastfmApiResponseProcessor`** - Processes API responses into domain entities
- **`EntityFactory`** - Maps DTOs to domain entities with validation
- **`EntityAttributeHandler`** - Manages attribute history tracking (SCD2 pattern)
- **`LastfmApiDtoProcessingService`** - Orchestrates DTO processing workflow

### 3. Entity Layer
- **Core Entities**: `LastfmArtist`, `LastfmAlbum`, `LastfmTrack`, `LastfmTag`
- **Relationship Entities**: `LastfmArtistTag`, `LastfmArtistTrack`, `LastfmArtistAlbum`, etc.
- **Tracking Entities**: `LastfmAttributeHistoryRecord`, `LastfmDataSnapshot`
- **Approval System**: All entities support approval workflow (PENDING → APPROVED/DECLINED)

### 4. Scheduling Layer
- **`LastfmApiCallScheduler`** - Coordinates API call generation and execution
- **`LastfmApiResponseProcessingScheduler`** - Processes responses asynchronously
- **Configurable intervals** - Different refresh rates for different API methods

## Data Flow

```
1. API Call Generation
   ↓
2. API Execution (Rate Limited)
   ↓
3. Response Storage
   ↓
4. Response Processing
   ├── Entity Mapping
   ├── Relationship Creation
   └── Attribute History Update
   ↓
5. Quality Control & Approval
```

### Detailed Data Flow

1. **API Call Generation**
   - Based on entity state and business logic per method
   - Considers last API call due dates to avoid duplicates
   - Prioritizes approved entities and popular content
   - Applies MBID-based deduplication for artists

2. **API Execution**
   - Rate-limited calls to LastFM API (1 call/sec)
   - Exponential backoff retry on failures
   - Response storage with compression

3. **Response Processing**
   - Parse JSON responses into DTOs
   - Map DTOs to domain entities
   - Create relationships between entities
   - Track attribute changes with SCD2 pattern

4. **Quality Control**
   - Apply configurable quality thresholds
   - Manual approval workflow
   - Data consistency validation

## Entity Selection Strategy

### Priority-Based Selection (artist.getInfo)
1. **Priority 1**: Approved artists (highest priority)
2. **Priority 2**: Top artists missing statistics
3. **Priority 3**: Similar artists to top artists
4. **Priority 4**: Other pending artists missing data

### Standard Selection Pattern
- No pending API calls of the same type
- Ordered by popularity (listeners_count, usage_count)
- MBID-based deduplication for artists
- Batch processing with configurable limits

### Special Cases
- **tag.getTopTags**: Pagination-based (not entity-scoped)
- **artist.search**: Uses separate search request entities

## Quality Control System

### Filtering Thresholds
- **Artist similarity**: Match coefficient > 0.2
- **Track quality**: Listeners count ≥ 1,000
- **Album quality**: Play count ≥ 10,000
- **Tag relevance**: Usage count ≥ 10
- **Search relevance**: Name similarity > 0.5

### Approval Workflow
- **PENDING** (1): Default state from API collection
- **APPROVED** (2): Manually approved for use
- **DECLINED** (3): Manually rejected
- **PRE_APPROVED** (4): Automatically approved (temporary)

## Configuration System

### Environment Variables
- **`MURAW_LASTFM_API_KEY`** - LastFM API key
- **`MURAW_LASTFM_DB_*`** - Database connection parameters
- **`MURAW_LASTFM_APP_*`** - Application server configuration

### Key Settings
- **API Rate Limiting**: `lastfm.client.callsPerSec` (default: 1.0)
- **Retry Policy**: 3 attempts with exponential backoff
- **Quality Thresholds**: Method-specific minimums for various metrics
- **Refresh Intervals**: 7-28 days depending on data volatility

## Integration Points

### Upstream Dependencies
- **LastFM API** - Primary data source
- **PostgreSQL** - Persistent storage (`mu_raw_lastfm` schema)

### Downstream Consumers
- **Music Data Module** - Consumes approved entities for binding
- **Music Universe UI** - Provides management interface for approval workflow
- **Music Quiz Module** - Uses approved data for quiz generation

## Deployment Architecture

### Local Development
```bash
./scripts/run-module-dev.sh music-universe:music-data-raw-lastfm
# Runs on port 7081 with dev profile
```

### Docker Deployment
```bash
./env/docker/deploy.sh local   # Port 9081
./env/docker/deploy.sh prod    # Port 8081
```

### Database Schema
- **Schema**: `mu_raw_lastfm`
- **User**: `mu_raw_lastfm_dm` (data manager)
- **Migration**: Liquibase with XML changelogs

## Performance Characteristics

### API Rate Limiting
- **1 call/second** to respect LastFM API limits
- **Exponential backoff** on failures (500ms initial delay, 1.5x multiplier)
- **3 retry attempts** before marking as failed

### Batch Processing
- **Default batch size**: 10 entities per generation cycle
- **Configurable limits** per API method
- **Hibernate batch processing** for database operations

### Data Volume
- **Artists**: ~68K entities (97% with statistics)
- **Tracks**: ~851K entities (99.6% with statistics)
- **Albums**: ~41K entities (98.6% with play counts, 0% with listeners)
- **Tags**: Thousands of entities with usage statistics

## Monitoring and Observability

### API Call Tracking
- All API calls tracked with timestamps and status
- Due date management for refresh scheduling
- Success/failure rate monitoring

### Data Quality Metrics
- Entity approval rates by type
- Attribute completeness statistics
- Relationship coverage analysis

### Performance Monitoring
- API response times and success rates
- Database query performance
- Processing throughput metrics
