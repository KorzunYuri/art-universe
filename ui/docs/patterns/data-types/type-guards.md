# Type Guard Functions

## What It Is

Runtime type checking functions that narrow TypeScript types, enabling safe access to type-specific properties and methods.

## Why It Exists

Provides runtime type validation for entities, enables type-safe conditional logic, and supports discriminated union patterns.

## Location

[src/music/shared/types/entities.ts](../../src/music/shared/types/entities.ts)

## Key Type Guard: isArtistRelatedEntity

**Purpose:** Determine if raw entity is artist-related (album or track)

**Signature:**
- Input: `entity: RawEntity<T>`
- Output: `entity is ArtistRelatedRawEntity<T>` (type predicate)

**Logic:**
1. Check entity type is 'track' or 'album'
2. Check `getExternalArtistId` method exists
3. Check `getMasterArtistId` method exists
4. All checks must pass

**Returns:** Boolean + type narrowing

## Type Narrowing

**Before Type Guard:**
- Entity type: `RawEntity<T>`
- Can't access `getExternalArtistId()` (TypeScript error)

**After Type Guard:**
- Entity type: `ArtistRelatedRawEntity<T>`
- Can safely access `getExternalArtistId()`
- Can safely access `getMasterArtistId()`

## Usage in Context Factory

Location: [RawEntityLookupContextFactory](../../src/music/data/raw/shared/types/lookup-context.ts)

**Pattern:**
- Check if entity is artist-related using type guard
- If true: Create artist-related context with IDs
- If false: Create basic context

**Benefit:** Type-safe context creation without hardcoding entity types

## Type Predicate Pattern

**Syntax:** `function isType(value): value is Type`

**Effect:** TypeScript narrows type in conditional blocks

**Example:**
- `if (isArtistRelatedEntity(entity)) { ... }`
- Inside block: TypeScript knows `entity` is `ArtistRelatedRawEntity`

## Additional Type Guards (Potential)

### isMasterEntity

**Purpose:** Check if entity is master entity

**Logic:** Check for `getMasterEntity()` returning self

### isRawEntity

**Purpose:** Check if entity is raw entity

**Logic:** Check for `getDataSource()` method

### isBoundEntity

**Purpose:** Check if raw entity is bound to master

**Logic:** Check if `masterEntity` field is present

## Type Guard Benefits

**Type Safety:**
- Prevent runtime errors from invalid property access
- Compile-time checking of narrowed types

**Code Clarity:**
- Explicit type checks in code
- Self-documenting type requirements

**Flexibility:**
- Generic code that adapts to entity type
- Reusable across different entity combinations

## Implementation Pattern

**Structure:**
- Check discriminator fields (e.g., entity type)
- Check method existence (e.g., `'method' in object`)
- Check type of method (e.g., `typeof obj.method === 'function'`)
- Return boolean with type predicate

## Related Patterns

- [Entity Types](./entity-types.md) - Entity type system
- [Lookup Context](../lookup/lookup-context.md) - Uses type guards
- [Context Factory](../lookup/context-factory.md) - Uses type guards

## Related Documentation

- [Binding Workflow](../../flows/binding-workflow.md) - Type guards in binding logic
