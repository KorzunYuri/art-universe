package yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository;

import jakarta.persistence.Entity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.relationship.LastfmAlbumTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.relationship.LastfmArtistTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.relationship.LastfmTrackTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.dto.EntityTagDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaOnlyTest;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LastfmTagRepositoryCustomImplTest extends JpaOnlyTest {

    @Autowired
    private LastfmTagRepositoryCustomImpl customRepository;
    
    @Autowired
    private DbConsistencyHelper dbHelper;

    @BeforeEach
    void setUp() throws Exception {
        // Force re-initialization of the repository
        customRepository.init();
        dbHelper.cleanup();
    }

    @Test
    void init_shouldPopulateTableNameCaches() throws Exception {
        // Use reflection to access private fields
        Field relationTableNamesField = LastfmTagRepositoryCustomImpl.class.getDeclaredField("relationTableNames");
        relationTableNamesField.setAccessible(true);
        
        Field entityFieldNamesField = LastfmTagRepositoryCustomImpl.class.getDeclaredField("entityFieldNames");
        entityFieldNamesField.setAccessible(true);
        
        @SuppressWarnings("unchecked")
        Map<LastfmEntityType, String> relationTableNames = 
            (Map<LastfmEntityType, String>) relationTableNamesField.get(customRepository);
        
        @SuppressWarnings("unchecked")
        Map<LastfmEntityType, String> entityFieldNames = 
            (Map<LastfmEntityType, String>) entityFieldNamesField.get(customRepository);
        
        // Verify that caches are populated
        assertFalse(relationTableNames.isEmpty());
        assertFalse(entityFieldNames.isEmpty());
        
        // Verify correct table names from @Entity annotations
        assertEquals(getEntityName(LastfmArtistTag.class), relationTableNames.get(LastfmEntityType.ARTIST));
        assertEquals(getEntityName(LastfmAlbumTag.class), relationTableNames.get(LastfmEntityType.ALBUM));
        assertEquals(getEntityName(LastfmTrackTag.class), relationTableNames.get(LastfmEntityType.TRACK));
        
        // Verify correct field names
        assertEquals("artist", entityFieldNames.get(LastfmEntityType.ARTIST));
        assertEquals("album", entityFieldNames.get(LastfmEntityType.ALBUM));
        assertEquals("track", entityFieldNames.get(LastfmEntityType.TRACK));
    }
    
    @Test
    void getSortField_shouldReturnCorrectFieldReference() throws Exception {
        // Use reflection to access private method
        java.lang.reflect.Method getSortFieldMethod = 
            LastfmTagRepositoryCustomImpl.class.getDeclaredMethod("getSortField", String.class);
        getSortFieldMethod.setAccessible(true);
        
        // Test tag properties
        assertEquals("t.name", getSortFieldMethod.invoke(customRepository, "name"));
        assertEquals("t.id", getSortFieldMethod.invoke(customRepository, "id"));
        assertEquals("t.url", getSortFieldMethod.invoke(customRepository, "url"));
        assertEquals("t.approvalStatus", getSortFieldMethod.invoke(customRepository, "approvalStatus"));
        
        // Test relation property
        assertEquals("rel.usageCount", getSortFieldMethod.invoke(customRepository, "usageCount"));
        
        // Test entity properties
        assertEquals("e.approvalStatus", getSortFieldMethod.invoke(customRepository, "entityApprovalStatus"));
        assertEquals("t.approvalStatus", getSortFieldMethod.invoke(customRepository, "tagApprovalStatus"));
        
        // Test unknown property (should default to t.name)
        assertEquals("t.name", getSortFieldMethod.invoke(customRepository, "unknownProperty"));
    }
    
    @Test
    void findTagsByEntityWithFilters_shouldReturnTagsWithEntityData() {
        // Given
        LastfmArtist artist = dbHelper.createAndSaveArtist(builder -> builder
            .approvalStatus(ApprovalStatus.APPROVED));
        
        LastfmTag tag1 = dbHelper.createAndSaveTag(builder -> builder
            .name("rock")
            .approvalStatus(ApprovalStatus.PENDING));
        
        LastfmTag tag2 = dbHelper.createAndSaveTag(builder -> builder
            .name("pop")
            .approvalStatus(ApprovalStatus.APPROVED));
        
        // Create entity relations with usage counts
        dbHelper.createAndSaveArtistTag(builder -> builder
            .artist(artist)
            .tag(tag1)
            .usageCount(50));
        
        dbHelper.createAndSaveArtistTag(builder -> builder
            .artist(artist)
            .tag(tag2)
            .usageCount(100));
        
        // When
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "usageCount"));
        List<EntityTagDto> result = customRepository.findTagsByEntityWithFilters(
            LastfmEntityType.ARTIST, artist.getId(), null, null, pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        
        // First tag should be "pop" with usage count 100
        assertEquals("pop", result.get(0).name());
        assertEquals(Integer.valueOf(100), result.get(0).usageCount());
        assertEquals(ApprovalStatus.APPROVED.getCode(), result.get(0).entityApprovalStatus());
        assertEquals(ApprovalStatus.APPROVED.getCode(), result.get(0).tagApprovalStatus());
        
        // Second tag should be "rock" with usage count 50
        assertEquals("rock", result.get(1).name());
        assertEquals(Integer.valueOf(50), result.get(1).usageCount());
        assertEquals(ApprovalStatus.APPROVED.getCode(), result.get(1).entityApprovalStatus());
        assertEquals(ApprovalStatus.PENDING.getCode(), result.get(1).tagApprovalStatus());
    }
    
    @Test
    void findTagsByEntityWithFilters_shouldFilterByMinUsageCount() {
        // Given
        LastfmArtist artist = dbHelper.createAndSaveArtist();
        
        LastfmTag tag1 = dbHelper.createAndSaveTag(builder -> builder.name("tag1"));
        LastfmTag tag2 = dbHelper.createAndSaveTag(builder -> builder.name("tag2"));
        LastfmTag tag3 = dbHelper.createAndSaveTag(builder -> builder.name("tag3"));
        
        // Create relations with different usage counts
        dbHelper.createAndSaveArtistTag(builder -> builder
            .artist(artist)
            .tag(tag1)
            .usageCount(10));
        
        dbHelper.createAndSaveArtistTag(builder -> builder
            .artist(artist)
            .tag(tag2)
            .usageCount(30));
        
        dbHelper.createAndSaveArtistTag(builder -> builder
            .artist(artist)
            .tag(tag3)
            .usageCount(20));
        
        // When - filter by min usage count
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"));
        List<EntityTagDto> result = customRepository.findTagsByEntityWithFilters(
            LastfmEntityType.ARTIST, artist.getId(), 20, null, pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        
        // Only tags with usage count >= 20 should be returned
        assertTrue(result.stream().anyMatch(dto -> dto.name().equals("tag2")));
        assertTrue(result.stream().anyMatch(dto -> dto.name().equals("tag3")));
        assertFalse(result.stream().anyMatch(dto -> dto.name().equals("tag1")));
        
        // Verify usage counts are correctly returned
        for (EntityTagDto dto : result) {
            if (dto.name().equals("tag2")) {
                assertEquals(Integer.valueOf(30), dto.usageCount());
            } else if (dto.name().equals("tag3")) {
                assertEquals(Integer.valueOf(20), dto.usageCount());
            }
        }
    }
    
    @Test
    void findTagsByEntityWithFilters_shouldSortByEntityApprovalStatus() {
        // Given
        LastfmArtist artist1 = dbHelper.createAndSaveArtist(builder -> builder
            .approvalStatus(ApprovalStatus.PENDING));
        
        LastfmArtist artist2 = dbHelper.createAndSaveArtist(builder -> builder
            .approvalStatus(ApprovalStatus.APPROVED));
        
        LastfmTag tag = dbHelper.createAndSaveTag();
        
        // Create entity relations
        dbHelper.createAndSaveArtistTag(artist1, tag);
        dbHelper.createAndSaveArtistTag(artist2, tag);
        
        // When - sort by entity approval status ascending
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "entityApprovalStatus"));
        List<EntityTagDto> result1 = customRepository.findTagsByEntityWithFilters(
            LastfmEntityType.ARTIST, artist1.getId(), null, null, pageable);
        
        List<EntityTagDto> result2 = customRepository.findTagsByEntityWithFilters(
            LastfmEntityType.ARTIST, artist2.getId(), null, null, pageable);
        
        // Then
        assertNotNull(result1);
        assertEquals(1, result1.size());
        assertEquals(ApprovalStatus.PENDING.getCode(), result1.get(0).entityApprovalStatus());
        
        assertNotNull(result2);
        assertEquals(1, result2.size());
        assertEquals(ApprovalStatus.APPROVED.getCode(), result2.get(0).entityApprovalStatus());
    }
    
    @Test
    void findTagsByEntityWithFilters_shouldHandleNullUsageCount() {
        // Given
        LastfmArtist artist = dbHelper.createAndSaveArtist();
        LastfmTag tag = dbHelper.createAndSaveTag(builder -> builder.name("nullUsageTag"));
        
        // Create relation with null usage count
        LastfmArtistTag artistTag = dbHelper.createAndSaveArtistTag(builder -> builder
            .artist(artist)
            .tag(tag)
            .usageCount(null));
        
        // When
        Pageable pageable = PageRequest.of(0, 10);
        List<EntityTagDto> result = customRepository.findTagsByEntityWithFilters(
            LastfmEntityType.ARTIST, artist.getId(), null, null, pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("nullUsageTag", result.get(0).name());
        // Should return 0 instead of null due to COALESCE
        assertEquals(Integer.valueOf(0), result.get(0).usageCount());
    }
    
    private String getEntityName(Class<?> entityClass) {
        Entity entityAnnotation = entityClass.getAnnotation(Entity.class);
        return entityAnnotation != null ? entityAnnotation.name() : entityClass.getSimpleName().toLowerCase();
    }
}
