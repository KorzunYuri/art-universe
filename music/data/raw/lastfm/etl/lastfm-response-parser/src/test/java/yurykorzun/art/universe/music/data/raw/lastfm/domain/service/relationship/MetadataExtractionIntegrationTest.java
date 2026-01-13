package yurykorzun.art.universe.music.data.raw.lastfm.domain.service.relationship;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.relationship.LastfmArtistAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.relationship.LastfmArtistTag;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.relationship.LastfmArtistsRelation;
import yurykorzun.art.universe.music.data.raw.lastfm.test.domain.repository.relationship.TestLastfmArtistTagRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.relationship.impl.LastfmArtistAlbumServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.relationship.impl.LastfmArtistTagServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.relationship.impl.LastfmArtistsRelationServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.test.domain.entity.EntityCreationHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.test.archetypes.LastfmJpaTestHelper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Import({
    LastfmArtistTagServiceImpl.class,
    LastfmArtistsRelationServiceImpl.class,
    LastfmArtistAlbumServiceImpl.class,
})
class MetadataExtractionIntegrationTest extends LastfmJpaTestHelper {

    @Autowired
    private LastfmArtistTagService artistTagService;
    
    @Autowired
    private LastfmArtistsRelationService artistsRelationService;
    
    @Autowired
    private LastfmArtistAlbumService artistAlbumService;

    @Autowired
    private TestLastfmArtistTagRepository artistTagRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void upsertAll_shouldThrowException_whenDataIntegrityViolated() {
        // Given - create valid entity
        LastfmArtistTag validEntity = consistencyHelper.createArtistTagForPersistence();
        
        // Create invalid entity with non-existent IDs but valid structure
        LastfmApiCall apiCall = consistencyHelper.createAndSaveApiCall();
        
        // Create entities with valid structure but non-existent IDs
        LastfmArtist fakeArtist = EntityCreationHelper.createArtist(builder -> 
            builder.id(999999L).apiCall(apiCall));
        LastfmTag fakeTag = EntityCreationHelper.createTag(builder -> 
            builder.id(999999L).apiCall(apiCall));
        
        LastfmArtistTag invalidEntity = EntityCreationHelper.createArtistTag(builder -> 
            builder.artist(fakeArtist)
                  .tag(fakeTag)
                  .apiCall(apiCall));

        // When & Then
        assertThrows(Exception.class, () -> {
            artistTagService.upsertAll(List.of(validEntity, invalidEntity));
        });
    }

    @Test
    void upsertAll_shouldBeTransactional() {
        // Given
        List<LastfmArtistTag> batch = List.of(
            consistencyHelper.createArtistTagForPersistence(),
            consistencyHelper.createArtistTagForPersistence(),
            consistencyHelper.createArtistTagForPersistence()
        );

        // When
        artistTagService.upsertAll(batch);

        // Then - all should be saved atomically
        assertEquals(3, artistTagRepository.count());
    }

    // Metadata validation tests
    @Test
    void initializeMetadata_shouldWorkForAllServiceTypes() {
        // Given & When - services should initialize without errors
        assertDoesNotThrow(() -> {
            LastfmArtistTagServiceImpl artistTagService = new LastfmArtistTagServiceImpl(null, null);
            artistTagService.initializeMetadata();
        }, "ArtistTag service metadata initialization should not throw");

        assertDoesNotThrow(() -> {
            LastfmArtistsRelationServiceImpl artistsRelationService = new LastfmArtistsRelationServiceImpl(null, null);
            artistsRelationService.initializeMetadata();
        }, "ArtistsRelation service metadata initialization should not throw");

        assertDoesNotThrow(() -> {
            LastfmArtistAlbumServiceImpl artistAlbumService = new LastfmArtistAlbumServiceImpl(null, null);
            artistAlbumService.initializeMetadata();
        }, "ArtistAlbum service metadata initialization should not throw");
    }

    @Test
    void initializeMetadata_shouldHandleInheritance_correctly() {
        // Given - services with inherited fields
        LastfmArtistTagServiceImpl service = new LastfmArtistTagServiceImpl(null, null);
        
        // When
        assertDoesNotThrow(() -> service.initializeMetadata());
        
        // Then - should be able to generate SQL with inherited fields
        LastfmArtistTag sampleEntity = consistencyHelper.createArtistTagForPersistence();
        
        assertDoesNotThrow(() -> {
            String sql = service.buildUpsertSql(sampleEntity);
            
            // Should include inherited fields from BaseLastfmEntityRelation
            assertTrue(sql.contains("approval_status"), "Should include inherited approval_status field");
            assertTrue(sql.contains("api_call_id"), "Should include inherited api_call_id field");
            assertTrue(sql.contains("created_at"), "Should include inherited created_at field");
            assertTrue(sql.contains("updated_at"), "Should include inherited updated_at field");
        });
    }

    @Test
    void metadataExtraction_shouldHandleDifferentEntityPatterns() {
        // Test different entity relationship patterns
        
        // 1. Different entity types (Artist-Tag)
        LastfmArtistTag artistTag = consistencyHelper.createArtistTagForPersistence();
        String artistTagSql = ((LastfmArtistTagServiceImpl) artistTagService).buildUpsertSql(artistTag);
        assertTrue(artistTagSql.contains("ON CONFLICT (artist_id, tag_id)"), 
            "Different entity types should use simple conflict columns");

        // 2. Same entity types (Artist-Artist)
        LastfmArtistsRelation artistsRelation = consistencyHelper.createArtistsRelationForPersistence();
        String artistsRelationSql = ((LastfmArtistsRelationServiceImpl) artistsRelationService).buildUpsertSql(artistsRelation);
        assertTrue(artistsRelationSql.contains("ON CONFLICT (source_artist_id, target_artist_id, relation_type)"), 
            "Same entity types should use source/target prefixes and include relation_type");

        // 3. No updatable fields (Artist-Album)
        LastfmArtistAlbum artistAlbum = consistencyHelper.createArtistAlbumForPersistence();
        String artistAlbumSql = ((LastfmArtistAlbumServiceImpl) artistAlbumService).buildUpsertSql(artistAlbum);
        assertTrue(artistAlbumSql.contains("DO NOTHING"), 
            "Entities with no updatable fields should use DO NOTHING");
    }

    @Test
    void metadataExtraction_shouldValidateAnnotations() {
        // Given
        LastfmArtistTagServiceImpl service = new LastfmArtistTagServiceImpl(null, null);
        service.initializeMetadata();
        
        // When
        LastfmArtistTag sampleEntity = consistencyHelper.createArtistTagForPersistence();
        String sql = ((LastfmArtistTagServiceImpl) artistTagService).buildUpsertSql(sampleEntity);
        
        // Then - should correctly extract from annotations
        assertTrue(sql.contains("INSERT INTO artist_tag"), 
            "Should extract table name from @Entity annotation");
        assertTrue(sql.contains("artist_id"), 
            "Should extract column name from @JoinColumn annotation");
        assertTrue(sql.contains("tag_id"), 
            "Should extract column name from @JoinColumn annotation");
        assertTrue(sql.contains("usage_count"), 
            "Should extract column name from @Column annotation");
        
        // Should NOT include @Id field in insert columns
        assertFalse(sql.matches(".*\\(.*\\bid\\b.*\\).*VALUES.*"), 
            "Should not include @Id field in insert columns");
    }

    @Test
    void metadataExtraction_shouldHandleConverters() {
        // Given - entity with converter (approval_status uses ApprovalStatusConverter)
        LastfmArtistTag sampleEntity = consistencyHelper.createArtistTagForPersistence();
        
        // When & Then - should handle converter without errors
        assertDoesNotThrow(() -> {
             artistTagService.upsertAll(List.of(sampleEntity));
            entityManager.flush();
            assertEquals(1, artistTagRepository.count());
        }, "Should handle fields with @Convert annotation");
    }

    @Test
    void parameterMapper_shouldHandleComplexFields() {
        // Given
        LastfmArtistTag entity = consistencyHelper.createArtistTagForPersistence();
        
        // When & Then - should handle JoinColumn fields (extracting IDs from referenced entities)
        assertDoesNotThrow(() -> {
            artistTagService.upsertAll(List.of(entity));
            entityManager.flush();
            
            List<LastfmArtistTag> saved = artistTagRepository.findAll();
            assertEquals(1, saved.size());
            
            // Verify that foreign key relationships are properly handled
            assertNotNull(saved.get(0).getArtist());
            assertNotNull(saved.get(0).getTag());
            assertNotNull(saved.get(0).getApiCall());
        }, "Parameter mapper should handle @JoinColumn fields correctly");
    }
}
