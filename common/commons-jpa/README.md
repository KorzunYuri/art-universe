# Commons JPA

The module provides JPA utilities, base entities, and common persistence patterns used across all Spring Boot application modules in the project. 

## Key Features

### BaseEntity

**Purpose**: Abstract base class for all JPA entities in the project

**Components**: [BaseEntity.java](src/main/java/yurykorzun/art/universe/common/persistence/entity/BaseEntity.java)

**Features**:
- `id` (Long) - Primary key with auto-generation
- `code` (String) - Unique business identifier (indexed)
- `createdAt` (Instant) - Audit timestamp
- `updatedAt` (Instant) - Audit timestamp
- Implements `equals()` and `hashCode()` based on `id`

### Coded Enum Pattern

**See**: [Coded Enum Pattern](../../docs/kb/patterns/backend/entities/coded-enums.md)

**Purpose**: Store enums as integer codes in database while maintaining type safety

**Components**:
- [Coded.java](src/main/java/yurykorzun/art/universe/common/Coded.java) interface - Marker for coded enums
- [CodedRegistry.java](src/main/java/yurykorzun/art/universe/common/CodedRegistry.java) - Central registry for coded enum instances
- [CodedConverter.java](src/main/java/yurykorzun/art/universe/common/CodedConverter.java) - JPA converter for coded enums
- [CodedAutoregistrator.java](src/main/java/yurykorzun/art/universe/common/CodedAutoregistrator.java) - Automatic registration using reflection
- [TransitionAware.java](src/main/java/yurykorzun/art/universe/common/TransitionAware.java) - Support for state transitions

### Base Lookup Functionality

**Purpose**: Standard pattern for entity lookup operations

**Features**:
- Lookup by single code
- Batch lookup by multiple codes
- Extensible for custom lookup criteria

**Location**: `service/lookup/`, `dto/lookup/`

**Components**:
- [BaseLookupService.java](src/main/java/yurykorzun/art/universe/common/service/lookup/BaseLookupService.java) - Abstract base for entity lookup by code
- [SqlQueryBuilder.java](src/main/java/yurykorzun/art/universe/common/service/lookup/SqlQueryBuilder.java) - SQL query builder for complex lookups
- DTOs: [LookupRequestDTO.java](src/main/java/yurykorzun/art/universe/common/dto/lookup/LookupRequestDTO.java), [LookupResultDTO.java](src/main/java/yurykorzun/art/universe/common/dto/lookup/LookupResultDTO.java), [BatchLookupRequestDTO.java](src/main/java/yurykorzun/art/universe/common/dto/lookup/BatchLookupRequestDTO.java), [BatchLookupResponseDTO.java](src/main/java/yurykorzun/art/universe/common/dto/lookup/BatchLookupResponseDTO.java)

[BaseLookupService.java](src/main/java/yurykorzun/art/universe/common/service/lookup/BaseLookupService.java) defines the lookup algorithm with customization points:
- `isValidSearchRequest()` - Request validation
- `buildQuery()` - SQL query construction
- `mapResultsToDto()` - Result formatting
- `filterAndPrepareRequests()` - Batch request processing

Subclasses override these methods to customize behavior.


### Database Utilities

**Purpose**: Utility methods for database operations and metadata access

**Location**: `persistence/util/`

**Components**:
- [DatabaseUtils.java](src/main/java/yurykorzun/art/universe/common/persistence/util/DatabaseUtils.java) - Common database operations
- [DbObjectMetadata.java](src/main/java/yurykorzun/art/universe/common/persistence/util/DbObjectMetadata.java) - Database object metadata extraction

### JPA Converters

**Purpose**: control the conversion of entity fields between database and Java.

**Location**: `persistence/converter/`

**Components**:
- [GzipBase64StringConverter.java](src/main/java/yurykorzun/art/universe/common/persistence/converter/GzipBase64StringConverter.java) - Compress/decompress large text fields
- [MapConverter.java](src/main/java/yurykorzun/art/universe/common/persistence/converter/MapConverter.java) - Store Map<String, Object> as JSON

**Usage**:
```java
@Entity
public class ApiResponse extends BaseEntity {
    @Convert(converter = GzipBase64StringConverter.class)
    private String largeJsonResponse;

    @Convert(converter = MapConverter.class)
    private Map<String, Object> metadata;
}
```

### Base Entity Dto

**Purpose**: base class for all DTOs used together with entities

- [BaseEntityDto.java](src/main/java/yurykorzun/art/universe/common/dto/BaseEntityDto.java) - Base DTO matching BaseEntity structure

### Auto-Configuration 

**Purpose**: adds Spring Boot auto-configuration, providing common JPA-related beans.

**Class**: [CommonJpaAutoConfiguration.java](src/main/java/yurykorzun/art/universe/common/config/CommonJpaAutoConfiguration.java)

**Beans Provided**:
- [CodedRegistry.java](src/main/java/yurykorzun/art/universe/common/CodedRegistry.java) - Singleton coded enum registry
- [CodedRegistrySynchronizer.java](src/main/java/yurykorzun/art/universe/common/persistence/CodedRegistrySynchronizer.java) - Synchronizes coded enums with database
- [CodedAutoregistrator.java](src/main/java/yurykorzun/art/universe/common/CodedAutoregistrator.java) - Auto-registers coded enums at startup

**Activation**: Automatically loaded when commons-jpa is on classpath

## Patterns Used/implemented

- **[Coded Enum Pattern](../../docs/kb/patterns/backend/entities/coded-enums.md)** - Integer-based enum storage
- **[BaseEntity Pattern](../../docs/kb/patterns/backend/entities/overview.md)** - Standard entity structure with audit fields
- **[Lookup Service Pattern](../../docs/kb/patterns/backend/api/conventions.md)** - Entity lookup by code

## Testing

The module includes:
- Unit tests for coded enum registry
- Tests for JPA converters
- Validation tests for BaseEntity
