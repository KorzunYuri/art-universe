# Coded Enum Pattern

## Purpose

Store enum values as integers in the database while maintaining type safety and readability in Java code. This pattern provides the best of both worlds: compact integer storage with compile-time type checking.

## When to Use

Use the Coded Enum pattern for:

- **Status fields**: PENDING, APPROVED, DECLINED, AUTOAPPROVED
- **Type fields**: ARTIST, ALBUM, TRACK
- **Category fields**: Any field with a fixed set of values
- **Any enum that maps to integer in database**

Do NOT use for:
- String enums stored as VARCHAR (use standard JPA `@Enumerated(EnumType.STRING)`)
- Dynamic values that change frequently
- Large sets of values (>50 options)


### Infrastructure
- [Coded Interface](../../../../../common/commons-jpa/src/main/java/yurykorzun/art/universe/common/Coded.java) - Base interface
- [CodedConverter](../../../../../common/commons-jpa/src/main/java/yurykorzun/art/universe/common/persistence/converter/CodedConverter.java) - JPA converter base
- [CodedRegistry](../../../../../common/commons-jpa/src/main/java/yurykorzun/art/universe/common/CodedRegistry.java) - Enum registry


## Implementation Steps

### Step 1: Define Enum Implementing Coded

Create your enum implementing the `Coded` interface.

**Example**: [ExecutionStatus](../../../../../music/quiz/src/main/java/yurykorzun/art/universe/music/quiz/entity/ExecutionStatus.java)

**Key Points**:
- Each enum value has a unique integer code
- Codes should be stable (never change once in use)
- Gaps in numbering are OK (e.g., 1, 2, 5, 10)
- Constructor stores the code
- `getCode()` method returns the code for database storage

### Step 2: Create Converter

Create a JPA converter by extending `CodedConverter<YourEnum>`.

**Example**: [ExecutionStatusConverter](../../../../../music/quiz/src/main/java/yurykorzun/art/universe/music/quiz/entity/ExecutionStatusConverter.java)

**Key Points**:
- Extend `CodedConverter<YourEnum>`
- Pass enum class to super constructor
- No additional code needed - base class handles conversion

### Step 3: Register in CodedRegistry
`
Register the enum in the CodedRegistry, like this:

```java
public enum ExecutionStatus implements Coded {
    
    static {
        CodedRegistry.register(Arrays.asList(values()), ExecutionStatus.class);
    }
```

**Example**: See quiz module or master data module configuration

**Key Points**:
- Registration happens once at application startup
- All coded enums must be registered
- Registry enables validation and lookup

### Step 4: Use in Entity

Apply the enum in your entity with `@Convert` annotation.

**Example**: See entities using [ExecutionStatus](../../../../../music/quiz/src/main/java/yurykorzun/art/universe/music/quiz/entity/ExecutionStatus.java) or [DataSource](../../../../../music/data/master/music-master-rest-api/src/main/java/yurykorzun/art/universe/music/data/master/entity/DataSource.java)

**Key Points**:
- Use `@Convert` annotation with your converter
- Column name explicitly specified (optional but recommended)
- Can provide default value
- Stored as INTEGER in database


## Database Schema

### Table Definition

**Key Points**:
- Column type is `INTEGER` (not VARCHAR)
- NOT NULL recommended (use default enum value in Java)
- No foreign key to enum dictionary (optional pattern)

**Example**: See migration files in quiz or master data modules for table definitions using coded enums


### Optional: Enum Dictionary Table

For reference and validation (optional):

**When to use**:
- Documentation purposes
- Database-level validation
- Reporting queries

**When to skip**:
- Adds maintenance burden
- Not required for functionality
- JPA handles validation in code


## How It Works

### Database to Java (Reading)

```
Database: status = 2
    ↓
CodedConverter reads integer
    ↓
Looks up enum with code 2
    ↓
Java: status = ExecutionStatus.STARTED
```

### Java to Database (Writing)

```
Java: status = ExecutionStatus.COMPLETED
    ↓
CodedConverter calls getCode()
    ↓
Returns integer 3
    ↓
Database: status = 3
```


## Testing

**Key Test Scenario**: Verify integer storage in database

**Complete Test Examples**: See test files in codebase:
- Quiz module: Test files for execution status
- Master data module: Test files for data source enums


## Examples in Codebase

### Base Infrastructure (Common Module)

- [Coded Interface](../../../../../common/commons-jpa/src/main/java/yurykorzun/art/universe/common/Coded.java) - Interface all coded enums must implement
- [CodedConverter](../../../../../common/commons-jpa/src/main/java/yurykorzun/art/universe/common/persistence/converter/CodedConverter.java) - Base converter class
- [CodedRegistry](../../../../../common/commons-jpa/src/main/java/yurykorzun/art/universe/common/CodedRegistry.java) - Registry for enum lookup

### Execution Status (Quiz Module)

**Location**: [ExecutionStatus](../../../../../music/quiz/src/main/java/yurykorzun/art/universe/music/quiz/entity/ExecutionStatus.java)

**Values**:
- PENDING (1)
- STARTED (2)
- COMPLETED (3)
- FAILED (4)

**Converter**: [ExecutionStatusConverter](../../../../../music/quiz/src/main/java/yurykorzun/art/universe/music/quiz/entity/ExecutionStatusConverter.java)

### Generation Status (Quiz Module)

**Location**: [GenerationStatus](../../../../../music/quiz/src/main/java/yurykorzun/art/universe/music/quiz/entity/GenerationStatus.java)

**Converter**: [GenerationStatusConverter](../../../../../music/quiz/src/main/java/yurykorzun/art/universe/music/quiz/entity/GenerationStatusConverter.java)

### Data Source (Master Data Module)

**Location**: [DataSource](../../../../../music/data/master/music-master-rest-api/src/main/java/yurykorzun/art/universe/music/data/master/entity/DataSource.java)

**Values**: Enumerates external data sources (LastFM, future sources)

**Converter**: [DataSourceConverter](../../../../../music/data/master/music-master-rest-api/src/main/java/yurykorzun/art/universe/music/data/master/entity/DataSourceConverter.java)

### Master Entity Type (Common Module)

**Location**: [MasterEntityType](../../../../../common/commons-jpa/src/main/java/yurykorzun/art/universe/common/domain/entity/MasterEntityType.java)

**Values**: ARTIST(1), ALBUM(2), TRACK(3), CATEGORY(4), PERSON(101)

**Converter**: [EntityTypeConverter](../../../../../common/commons-jpa/src/main/java/yurykorzun/art/universe/common/persistence/converter/EntityTypeConverter.java) (`@Converter(autoApply = true)`)

**Note**: This is a cross-domain enum shared by all modules. It lives in
`common:commons-jpa` rather than in any specific domain module. Because
`@SpringBootApplication` only scans its own package tree, modules using this
enum must add `@EntityScan` to include the common packages — otherwise JPA
will not discover the auto-applied converter and will fall back to ordinal
storage. Code ranges: 1–99 for music domain, 101–199 for art/cross-domain
(PERSON), 201+ reserved.


## Common Enhancements

**Art Universe enums often include helper methods for business logic and state transition validation**.

**See complete implementations**:
- [ExecutionStatus](../../../../../music/quiz/src/main/java/yurykorzun/art/universe/music/quiz/entity/ExecutionStatus.java) - Includes transition validation
- [GenerationStatus](../../../../../music/quiz/src/main/java/yurykorzun/art/universe/music/quiz/entity/GenerationStatus.java) - Includes state machine logic


## Best Practices

### DO:
✅ Use stable integer codes (never change once in production)
✅ Start codes from 1 (not 0) for clarity
✅ Leave gaps for future values (1, 10, 20, 30...)
✅ Register all enums in CodedRegistry
✅ Test both directions (Java→DB, DB→Java)
✅ Document what each code means

### DON'T:
❌ Reuse codes for different values
❌ Change codes after deployment
❌ Use 0 as a meaningful value (can be confused with null)
❌ Forget to register enum in CodedRegistry
❌ Mix coded enums with string enums in same entity
❌ Place a shared `@Converter(autoApply = true)` in a library package without
   adding that package to `@EntityScan` in consuming applications — JPA silently
   falls back to ordinal storage


## See Also

### Related Patterns
- [Entity Patterns Overview](overview.md) - All entity patterns index
- [Base Entity Pattern](base-entity.md) - Entity base class
- [Strategy Registry Pattern](../strategy-registry.md) - Different registry pattern for Spring bean strategies (not enums)
