# Endpoint Changes Required for UI Refactoring

This document lists all backend endpoint changes needed to support the refactored music-ui.
Organized by module, with: **ADD** (new endpoints), **MODIFY** (changes to existing), **REMOVE** (deprecated).

---

## 1. `music:data:master`

### 1.1 Endpoints to ADD

#### AlbumController — CRUD endpoints

The current `AlbumController` only has binding/lookup/unbind endpoints.
The UI needs full CRUD to power the Albums table and AlbumDetail page.

| # | Method | URL | Description |
|---|--------|-----|-------------|
| 1 | `GET` | `/api/v1/albums` | Paginated list of albums |
| 2 | `GET` | `/api/v1/albums/{id}` | Get single album by ID |
| 3 | `POST` | `/api/v1/albums` | Create or update album |
| 4 | `DELETE` | `/api/v1/albums/{id}` | Delete album |

**`GET /api/v1/albums`**
- Query params: `search` (String, optional), `page` (int), `size` (int), `sort` (String, e.g. `"name,asc"`)
- Response: `Page<AlbumDto>`
- Notes: Follow the same `Pageable` pattern as `ArtistController.getAll()`. The UI calls this via `fetchMasterEntities('album', params)`.

**`GET /api/v1/albums/{id}`**
- Path variable: `id` (Long)
- Response: `AlbumDto`
- Notes: The UI calls this via `fetchMasterEntity('album', id)` and `useMasterEntity('album', id)`.

**`POST /api/v1/albums`**
- Request body: `AlbumSaveRequestDTO`
- Response: `AlbumDto`
- Notes: Upsert — if `id` is null, create; if `id` is present, update. Same pattern as `ArtistController.saveArtist()`. The UI calls this from both `createAlbum()` and `saveAlbum()`.

**`DELETE /api/v1/albums/{id}`**
- Path variable: `id` (Long)
- Response: `boolean`
- Notes: Should cascade-unbind raw entity bindings. Same pattern as `ArtistController.deleteArtist()`.

---

#### TrackController — CRUD endpoints

The current `TrackController` only has binding/lookup/unbind endpoints.
The UI needs full CRUD to power the Tracks table and TrackDetail page.

| # | Method | URL | Description |
|---|--------|-----|-------------|
| 5 | `GET` | `/api/v1/tracks` | Paginated list of tracks |
| 6 | `GET` | `/api/v1/tracks/{id}` | Get single track by ID |
| 7 | `POST` | `/api/v1/tracks` | Create or update track |
| 8 | `DELETE` | `/api/v1/tracks/{id}` | Delete track |

**`GET /api/v1/tracks`**
- Query params: `search` (String, optional), `page` (int), `size` (int), `sort` (String)
- Response: `Page<TrackDto>`

**`GET /api/v1/tracks/{id}`**
- Path variable: `id` (Long)
- Response: `TrackDto`

**`POST /api/v1/tracks`**
- Request body: `TrackSaveRequestDTO`
- Response: `TrackDto`
- Notes: Upsert — if `id` is null, create; if `id` is present, update.

**`DELETE /api/v1/tracks/{id}`**
- Path variable: `id` (Long)
- Response: `boolean`

---

#### Album & Track Category Management

| # | Method | URL | Description |
|---|--------|-----|-------------|
| 9 | `POST` | `/api/v1/albums/{albumId}/categories/{categoryId}` | Bind album to category |
| 10 | `DELETE` | `/api/v1/albums/{albumId}/categories/{categoryId}` | Unbind album from category |
| 11 | `GET` | `/api/v1/albums/with-categories` | Paginated albums with categories |
| 12 | `GET` | `/api/v1/albums/{id}/with-categories` | Single album with categories |
| 13 | `POST` | `/api/v1/tracks/{trackId}/categories/{categoryId}` | Bind track to category |
| 14 | `DELETE` | `/api/v1/tracks/{trackId}/categories/{categoryId}` | Unbind track from category |
| 15 | `GET` | `/api/v1/tracks/with-categories` | Paginated tracks with categories |
| 16 | `GET` | `/api/v1/tracks/{id}/with-categories` | Single track with categories |

These follow the same pattern as `ArtistController` category endpoints.
The UI's `fetchMasterEntitiesWithRelations` already routes album/track through the `with-categories` variants.
AlbumDetail and TrackDetail pages have full category add/remove sections (MasterEntityPanel + MasterEntityPicker).
AlbumsTable and TracksTable have inline Categories and Add Category columns.

---

### 1.2 Endpoints to MODIFY

#### RelationController — `GET /api/v1/relations/{src}/{srcId}/{tgt}`

**Current response DTO** (`RelatedEntityDTO`):
```java
public class RelatedEntityDTO extends BaseEntityDto {
    // inherited: Long id, String name
    private MasterEntityType entityType;
    private Long relationTypeId;
    private String relationTypeName;
}
```

**Required change**: Add `relationId` field — the ID of the relation record itself (e.g., `artist_artist.id`, `artist_album.id`).

The UI needs `relationId` to call `DELETE /api/v1/relations/internal/{relationId}` for removing individual relations from the RelatedEntitiesSection.

**Updated DTO**:
```java
public class RelatedEntityDTO extends BaseEntityDto {
    // inherited: Long id, String name
    private MasterEntityType entityType;
    private Long relationId;         // <-- NEW: the relation record ID
    private Long relationTypeId;
    private String relationTypeName;
}
```

**Frontend interface** (already expects this field):
```typescript
export interface RelatedEntityDTO {
    id: number;              // master entity ID
    name: string;            // master entity name
    entityType: string;
    relationId: number;      // relation record ID (used for DELETE)
    relationTypeId: number | null;
    relationTypeName: string | null;
}
```

---

### 1.3 Endpoints to REMOVE

None. All existing endpoints remain in use.

---

### 1.4 New DTOs

#### `AlbumDto`

Response DTO for album CRUD endpoints.

```java
@Data
@EqualsAndHashCode(callSuper = false)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AlbumDto extends BaseEntityDto {
    // inherited: Long id, String name
    private Long primaryArtistId;
}
```

Frontend counterpart (already exists in `music-data-albums.ts`):
```typescript
export interface AlbumDto extends BaseMasterEntityDto {
    primaryArtistId: number;
}
```

---

#### `AlbumSaveRequestDTO`

Request DTO for `POST /api/v1/albums` (create/update).

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlbumSaveRequestDTO {
    private Long id;                    // null for create, present for update
    @NotBlank private String name;
    @NotNull private Long primaryArtistId;
}
```

Frontend counterpart (already exists):
```typescript
export interface AlbumCreateRequest {
    name: string;
    primaryArtistId: number;
}

export interface AlbumSaveRequest {
    id?: number;
    name: string;
    primaryArtistId?: number;
}
```

Note: Both `AlbumCreateRequest` and `AlbumSaveRequest` map to the same backend DTO.
On create `primaryArtistId` is required; on update it's optional (only sent if changed).
The backend should treat null `primaryArtistId` on update as "don't change".

---

#### `TrackDto`

Response DTO for track CRUD endpoints.

```java
@Data
@EqualsAndHashCode(callSuper = false)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TrackDto extends BaseEntityDto {
    // inherited: Long id, String name
    private Long primaryArtistId;
}
```

Frontend counterpart (already exists in `music-data-tracks.ts`):
```typescript
export interface TrackDto extends BaseMasterEntityDto {
    primaryArtistId: number;
}
```

---

#### `TrackSaveRequestDTO`

Request DTO for `POST /api/v1/tracks` (create/update).

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrackSaveRequestDTO {
    private Long id;                    // null for create, present for update
    @NotBlank private String name;
    @NotNull private Long primaryArtistId;
}
```

Frontend counterpart (already exists):
```typescript
export interface TrackCreateRequest {
    name: string;
    primaryArtistId: number;
}

export interface TrackSaveRequest {
    id?: number;
    name: string;
    primaryArtistId?: number;
}
```

---

#### `AlbumWithCategoriesDto`

```java
@Data
@EqualsAndHashCode(callSuper = false)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AlbumWithCategoriesDto extends AlbumDto {
    // inherited: Long id, String name, Long primaryArtistId
    private List<CategoryDto> categories;
}
```

---

#### `TrackWithCategoriesDto`

```java
@Data
@EqualsAndHashCode(callSuper = false)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TrackWithCategoriesDto extends TrackDto {
    // inherited: Long id, String name, Long primaryArtistId
    private List<CategoryDto> categories;
}
```

---

### 1.5 Modified DTOs

#### `RelatedEntityDTO` — add `relationId`

See section 1.2 above for full details.

```diff
 public class RelatedEntityDTO extends BaseEntityDto {
     private MasterEntityType entityType;
+    private Long relationId;
     private Long relationTypeId;
     private String relationTypeName;
 }
```

---

## 2. `music:data:raw:lastfm:lastfm-rest-api`

### 2.1 Endpoints to ADD

No immediate additions required — all UI-called endpoints exist.

The following are **recommended future additions** (not currently called by the UI but would support planned features):

| # | Method | URL | Description | Use case |
|---|--------|-----|-------------|----------|
| 1 | `GET` | `/api/v1/albums/lookup` | Album name search | MasterEntityPicker for lastfm albums |
| 2 | `GET` | `/api/v1/tracks/lookup` | Track name search | MasterEntityPicker for lastfm tracks |
| 3 | `GET` | `/api/v1/albums/{albumId}/tracks` | Tracks belonging to an album | Album detail page showing track list |

**`GET /api/v1/albums/lookup`**
- Query params: `search` (String, required), `limit` (int, optional, default 10)
- Response: `List<LookupResultDTO>`
- Notes: Same pattern as existing `LastfmArtistController.lookup()` and `LastfmTagController.lookup()`.

**`GET /api/v1/tracks/lookup`**
- Query params: `search` (String, required), `limit` (int, optional, default 10)
- Response: `List<LookupResultDTO>`

**`GET /api/v1/albums/{albumId}/tracks`**
- Path variable: `albumId` (Long)
- Query params: `page`, `size`, `sort` (Pageable), optionally `search`, `approvalStatuses`
- Response: `Page<LastfmTrackResponseDto>`
- Notes: Returns lastfm tracks associated with the given album. Enables the album detail page to show its contained tracks.

### 2.2 Endpoints to MODIFY

None.

### 2.3 Endpoints to REMOVE

None.

### 2.4 DTO Changes

None — all existing lastfm response DTOs are sufficient:
- `LastfmArtistResponseDto` — `{ id, name, url, mbid?, approvalStatus, playCount?, listenersCount? }`
- `LastfmAlbumResponseDto` — `{ id, name, url, mbid?, approvalStatus, playCount?, listenersCount?, publishTs?, artist? }`
- `LastfmTrackResponseDto` — `{ id, name, url, mbid?, approvalStatus, playCount?, listenersCount?, artist? }`
- `LastfmTagResponseDto` — `{ id, name, url?, approvalStatus, usageCount?, usageUsersCount? }`

---

## 3. `music:data:raw:lastfm:etl:lastfm-etl-rest-api`

### 3.1 Endpoints to ADD

None.

### 3.2 Endpoints to MODIFY

None.

### 3.3 Endpoints to REMOVE

None.

All ETL endpoints (approval PATCH for artists/albums/tracks/tags, artist search POST, maintenance trigger) are complete.

---

## Summary

### Blocking changes (UI already calls these, will fail without backend)

| Priority | Module | Change | Detail |
|----------|--------|--------|--------|
| **P0** | master | ADD 4 album CRUD endpoints | `GET /albums`, `GET /albums/{id}`, `POST /albums`, `DELETE /albums/{id}` |
| **P0** | master | ADD 4 track CRUD endpoints | `GET /tracks`, `GET /tracks/{id}`, `POST /tracks`, `DELETE /tracks/{id}` |
| **P0** | master | ADD `AlbumDto` | `{ id, name, primaryArtistId }` |
| **P0** | master | ADD `AlbumSaveRequestDTO` | `{ id?, name, primaryArtistId }` |
| **P0** | master | ADD `TrackDto` | `{ id, name, primaryArtistId }` |
| **P0** | master | ADD `TrackSaveRequestDTO` | `{ id?, name, primaryArtistId }` |
| **P0** | master | MODIFY `RelatedEntityDTO` | Add `relationId` field |
| **P0** | master | ADD 8 album/track category endpoints | Following artist pattern: bind/unbind category, with-categories variants |
| **P0** | master | ADD `AlbumWithCategoriesDto` | `{ id, name, primaryArtistId, categories[] }` |
| **P0** | master | ADD `TrackWithCategoriesDto` | `{ id, name, primaryArtistId, categories[] }` |

### Non-blocking changes (future features, UI not yet calling)

| Priority | Module | Change | Detail |
|----------|--------|--------|--------|
| **P2** | lastfm-rest-api | ADD album lookup | `GET /albums/lookup` |
| **P2** | lastfm-rest-api | ADD track lookup | `GET /tracks/lookup` |
| **P2** | lastfm-rest-api | ADD album tracks | `GET /albums/{albumId}/tracks` |
