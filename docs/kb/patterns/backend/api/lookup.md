# Lookup Pattern

## Purpose

Minimal-data entity lookup optimized for form controls, dropdowns, and quick selection with fast, lightweight queries.


## When to Use

- Form dropdowns (`<select>` elements)
- Autocomplete fields (type-ahead search)
- Quick entity selection
- Client-side filtering (small datasets)
- Related entity selection in forms


## Endpoint Pattern

```
GET /api/v1/{entities}/lookup?name={term}&limit={limit}
```

### Query Parameters

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `name` | string | No | - | Search term (partial, case-insensitive) |
| `limit` | integer | No | 20 | Maximum results (max: 100) |

### Response Format

Http status code: 200 OK

Content-Type: application/json

Body: List<[LookupResultDTO](../../../../../common/commons-jpa/src/main/java/yurykorzun/art/universe/common/dto/lookup/LookupResultDTO.java)>


## Implementation

### Controller

```java
@GetMapping("/lookup")
public List<LookupResultDTO> lookup(
    @RequestParam(required = false) String name,
    @RequestParam(defaultValue = "20") int limit
) {
    return artistService.lookup(name, Math.min(limit, 100));
}
```

### Service Layer

```java
@Transactional(readOnly = true)
public List<LookupResultDTO> lookup(String name, int limit) {
    List<Artist> artists;

    if (name == null || name.isBlank()) {
        artists = artistRepository.findTop20ByOrderByNameAsc();
    } else {
        artists = artistRepository.findByNameContainingIgnoreCaseOrderByNameAsc(name)
            .stream()
            .limit(limit)
            .collect(Collectors.toList());
    }

    return artists.stream()
        .map(a -> new LookupResultDTO(a.getId(), a.getName()))
        .collect(Collectors.toList());
}
```

### Repository Query

```java
List<Artist> findTop20ByOrderByNameAsc();

@Query("SELECT a FROM Artist a WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :name, '%')) ORDER BY a.name ASC")
List<Artist> findByNameContainingIgnoreCaseOrderByNameAsc(@Param("name") String name);
```

## Frontend Integration

### Dropdown Example

```typescript
// React component for form dropdown
const ArtistSelector = ({ value, onChange }) => {
  const [searchTerm, setSearchTerm] = useState('');

  const { data: artists } = useQuery({
    queryKey: ['artists', 'lookup', searchTerm],
    queryFn: () => api.lookupArtists(searchTerm, 20)
  });

  return (
    <Autocomplete
      value={value}
      onChange={onChange}
      options={artists || []}
      getOptionLabel={(option) => option.name}
      renderInput={(params) => (
        <TextField
          {...params}
          onChange={(e) => setSearchTerm(e.target.value)}
        />
      )}
    />
  );
};
```


## See Also

- [Search Pattern](search.md) - Full-featured variant for management interfaces
- [API Conventions](conventions.md) - Standard API conventions
