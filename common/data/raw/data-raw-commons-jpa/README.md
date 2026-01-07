# Data Raw Commons JPA

The module extends [:common:commons-jpa](../../../../common/commons-jpa/README.md) with patterns tailored for ETL pipelines that collect data from external APIs.

## Key Components

### API Call Tracking Entities

**Location**: `api/client/entity/`

| Entity | Purpose |
|--------|---------|
| `ApiCall` | Tracks API call tasks (base entity for ETL pipeline) |
| `ApiResponse` | Stores raw API responses |

**ApiCall Fields**:
- `callType` (ApiCallType enum) - Type of API method
- `status` (ApiCallStatus enum) - Call execution status
- `scheduledAt` - When to execute
- `executedAt` - When executed

**ApiCallStatus** (coded enum):
- `PENDING` - Waiting to be executed
- `IN_PROGRESS` - Currently executing
- `COMPLETED` - Successfully executed
- `FAILED` - Execution failed

**ApiResponse Fields**:
- `apiCall` - Reference to ApiCall
- `status` (ApiResponseStatus enum) - Processing status
- `rawResponse` - Raw JSON response (compressed)
- `parsedAt` - When parsed

**ApiResponseStatus** (coded enum):
- `PENDING` - Waiting to be parsed
- `PARSED` - Successfully parsed
- `FAILED` - Parsing failed

### Collectable Entity Pattern

**Location**: `entity/`

| Component | Purpose |
|-----------|---------|
| `BaseCollectableEntity` | Base class for collectable entities (extends BaseEntity) |
| `Approvable` | Interface for entities with approval workflow |
| `ApprovalStatus` | Coded enum for approval states |

**ApprovalStatus** (coded enum):
- `PENDING` - Awaiting review
- `APPROVED` - Approved for use
- `REJECTED` - Rejected

**Pattern**: Raw data entities (Artist, Album, etc.) extend BaseCollectableEntity and get automatic approval status tracking.

### API Processing

**Location**: `api/methods/common/`

| Component | Purpose |
|-----------|---------|
| `BaseApiResponseProcessor` | Abstract base for processing API responses |

**Purpose**: Standard pattern for parsing raw JSON responses into entities.

### DTOs

**Location**: `api/client/dto/`

| DTO | Purpose |
|-----|---------|
| `ApiCallCreateRequest` | DTO for creating API call tasks |
| `ApiResponseCreateRequest` | DTO for storing API responses |

## Patterns Provided

### ETL Pipeline Pattern

This module establishes the standard ETL pipeline pattern used across all raw data collection systems:

```
1. Generate API Calls (ApiCall entities with PENDING status)
2. Execute API Calls (update status to IN_PROGRESS → COMPLETED)
3. Store Responses (ApiResponse entities with raw JSON)
4. Parse Responses (extract entities, update ApiResponse status to PARSED)
```

### Approval Workflow Pattern

Collectable entities can be automatically or manually approved based on configurable thresholds.
