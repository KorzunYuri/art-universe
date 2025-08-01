# Entity Relations in Music Universe

## Binding External Relations to Internal Relations

### General Concept

In the Music Universe system, there are two types of entities:
1. **Master entities** (internal) - stored in tables like `artist`, `track`, `album`, `category`, etc.
2. **External entities** - data from external sources (LastFM, Spotify, MusicBrainz)

Relations between entities also come in two types:
1. **Internal relations** - relations between master entities (e.g., relation between an artist and a category)
2. **External relations** - relations between external entities (e.g., relation between a LastFM artist and a LastFM tag)

The most complex case is binding an external relation to an internal relation. For example, when we need to bind the "LastFM artist - LastFM tag" relation to the internal "artist - category" relation.

### Table Structure for Relations

```
+----------------+       +-------------------+       +------------------+
| artist         |       | artist_category   |       | category         |
+----------------+       +-------------------+       +------------------+
| id             |<----->| id                |<----->| id               |
| name           |       | artist_id         |       | name             |
| ...            |       | category_id       |       | ...              |
+----------------+       +-------------------+       +------------------+
       ^                         ^                          ^
       |                         |                          |
+----------------+       +-------------------+       +------------------+
| artist_binding |       | artist_category_  |       | category_binding |
+----------------+       | binding           |       +------------------+
| id             |       +-------------------+       | id               |
| master_id      |       | id                |       | master_id        |
| data_source_id |       | master_id         |       | data_source_id   |
| external_id    |       | data_source_id    |       | external_id      |
+----------------+       | external_artist_id|       +------------------+
                         | external_category_|
                         | id                |
                         +-------------------+
```

### Example: Binding a LastFM Artist with a LastFM Tag

#### Initial Data

Let's assume we have:

1. External entities in LastFM:
   - Artist "Nirvana" with ID 123 in LastFM
   - Tag "rock" with ID 456 in LastFM
   - A relation between them in LastFM

2. Internal entities in our database:
   - Artist "Nirvana" with ID 10
   - Category "Rock" with ID 20

#### Binding Process

##### Step 1: Binding External Entities to Internal Entities

First, we need to bind external entities to internal ones:

```
artist_binding:
| id | master_id | data_source_id | external_id |
|----|-----------|---------------|------------|
| 1  | 10        | 1 (LASTFM)    | 123        |

category_binding:
| id | master_id | data_source_id | external_id |
|----|-----------|---------------|------------|
| 1  | 20        | 1 (LASTFM)    | 456        |
```

##### Step 2: Creating an Internal Relation (if it doesn't exist)

If there's no relation between the artist and category yet, we create it:

```
artist_category:
| id | artist_id | category_id |
|----|-----------|------------|
| 30 | 10        | 20         |
```

##### Step 3: Binding the External Relation to the Internal Relation

Now we bind the external relation to the internal one:

```
artist_category_binding:
| id | master_id | data_source_id | external_artist_id | external_category_id |
|----|-----------|---------------|-------------------|---------------------|
| 1  | 30        | 1 (LASTFM)    | 123               | 456                 |
```

#### Unbinding Process

##### Unbinding an External Relation

1. Delete the record from the `artist_category_binding` table:

```sql
DELETE FROM artist_category_binding 
WHERE data_source_id = 1 AND external_artist_id = 123 AND external_category_id = 456;
```

2. The internal relation in the `artist_category` table remains untouched, as it may be used by other bindings or created manually.

##### Complete Unbinding (including the internal relation)

If we also need to delete the internal relation:

```sql
-- First, delete all bindings to this relation
DELETE FROM artist_category_binding WHERE master_id = 30;

-- Then delete the relation itself
DELETE FROM artist_category WHERE id = 30;
```

### ASCII Diagram of the Binding Process

```
EXTERNAL ENTITIES (LastFM)                 INTERNAL ENTITIES (Music Universe)
+----------------+                         +----------------+
| Artist         |                         | Artist         |
| "Nirvana"      |                         | "Nirvana"      |
| ID: 123        |                         | ID: 10         |
+----------------+                         +----------------+
        |                                          |
        | relation in LastFM                       | internal relation
        v                                          v
+----------------+                         +----------------+
| Tag            |                         | Category       |
| "rock"         |                         | "Rock"         |
| ID: 456        |                         | ID: 20         |
+----------------+                         +----------------+

                    BINDINGS
          +-------------------------+
          | artist_binding          |
          | master_id: 10           |
          | data_source_id: LASTFM  |
          | external_id: 123        |
          +-------------------------+
                      |
                      v
          +-------------------------+
          | artist_category         |
          | id: 30                  |
          | artist_id: 10           |
          | category_id: 20         |
          +-------------------------+
                      |
                      v
          +-------------------------+
          | artist_category_binding |
          | master_id: 30           |
          | data_source_id: LASTFM  |
          | external_artist_id: 123 |
          | external_category_id: 456|
          +-------------------------+
                      |
                      v
          +-------------------------+
          | category_binding        |
          | master_id: 20           |
          | data_source_id: LASTFM  |
          | external_id: 456        |
          +-------------------------+
```

### Important Points for Frontend Implementation

1. **Operation Sequence**:
   - First, bind individual entities (artist, category)
   - Then bind relations between them

2. **Checks Before Binding a Relation**:
   - Ensure both external entities are already bound to internal ones
   - Check if the internal relation exists or create it

3. **Interface Display**:
   - Show binding status for each entity and relation
   - Provide the ability to create an internal relation when binding an external one

4. **Error Handling**:
   - If one of the entities is not bound, suggest binding it first
   - If the internal relation doesn't exist, suggest creating it

## API Methods for Working with Entity Relations

### Binding an External Relation to an Internal One

```
POST /api/v1/relations/bind/{dataSource}/{sourceEntityType}/{sourceExternalEntityId}/{targetEntityType}/{targetExternalEntityId}
```

**Request Example:**
```
POST /api/v1/relations/bind/lastfm/artist/123/category/456
```

**Response Example:**
```json
{
  "success": true,
  "message": null,
  "data": {
    "id": 1,
    "masterId": 30,
    "dataSource": "LASTFM",
    "externalArtistId": 123,
    "externalCategoryId": 456
  }
}
```

### Unbinding an External Relation

```
DELETE /api/v1/relations/unbind/{dataSource}/{sourceEntityType}/{sourceExternalEntityId}/{targetEntityType}/{targetExternalEntityId}
```

**Request Example:**
```
DELETE /api/v1/relations/unbind/lastfm/artist/123/category/456
```

**Response Example:**
```json
{
  "success": true,
  "message": null,
  "data": true
}
```

### Getting Bound Relations

```
GET /api/v1/relations/bound/{dataSource}/{sourceEntityType}/{sourceExternalEntityId}/{targetEntityType}?ids=[targetExternalEntityIds]
```

**Request Example:**
```
GET /api/v1/relations/bound/lastfm/artist/123/category?ids=456,789
```

**Response Example:**
```json
{
  "success": true,
  "message": null,
  "data": {
    "sourceEntityId": 10,
    "boundTargetEntities": [
      {
        "externalId": 456,
        "masterId": 20,
        "relationId": 30
      }
    ],
    "unboundTargetEntities": [789]
  }
}
```

### Getting Related Entities

```
GET /api/v1/relations/{sourceEntityType}/{sourceEntityId}/{targetEntityType}
```

**Request Example:**
```
GET /api/v1/relations/artist/10/category
```

**Response Example:**
```json
{
  "success": true,
  "message": null,
  "data": [
    {
      "id": 20,
      "name": "Rock",
      "relationId": 30
    },
    {
      "id": 21,
      "name": "Grunge",
      "relationId": 31
    }
  ]
}
```

### Creating an Internal Relation

```
POST /api/v1/relations/internal/{sourceEntityType}/{sourceEntityId}/{targetEntityType}/{targetEntityId}
```

**Request Example:**
```
POST /api/v1/relations/internal/artist/10/category/20
```

**Response Example:**
```json
{
  "success": true,
  "message": null,
  "data": 30
}
```

### Deleting an Internal Relation by Entity Types and IDs

```
DELETE /api/v1/relations/internal/{sourceEntityType}/{sourceEntityId}/{targetEntityType}/{targetEntityId}
```

**Request Example:**
```
DELETE /api/v1/relations/internal/artist/10/category/20
```

**Response Example:**
```json
{
  "success": true,
  "message": null,
  "data": true
}
```

### Deleting an Internal Relation by Relation ID

```
DELETE /api/v1/relations/internal/{relationId}
```

**Request Example:**
```
DELETE /api/v1/relations/internal/30
```

**Response Example:**
```json
{
  "success": true,
  "message": null,
  "data": true
}
```
