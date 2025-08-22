package yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityRelationType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.entity.LastfmArtistAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.entity.LastfmArtistTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.entity.LastfmArtistsRelation;
import yurykorzun.art.universe.music.data.raw.lastfm.common.EntityCreationHelper;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for metadata extraction functionality
 */
@ExtendWith(MockitoExtension.class)
class MetadataExtractionUnitTest {

    @Mock
    private JdbcTemplate jdbcTemplate;
    
    @Mock
    private EntityManager entityManager;

    @Test
    void extractTableName_shouldWorkForAllServices() {

        // ArtistTag
        LastfmArtistTagServiceImpl artistTagService = new LastfmArtistTagServiceImpl(jdbcTemplate, entityManager);
        artistTagService.initializeMetadata();
        String artistTagSql = artistTagService.buildUpsertSql(
            LastfmArtistTag.builder()
                .apiCall(EntityCreationHelper.createApiCall())
                .artist(EntityCreationHelper.createArtist())
                .tag(EntityCreationHelper.createTag())
                .build());
        assertTrue(artistTagSql.contains("INSERT INTO artist_tag"), "ArtistTag table name should be 'artist_tag'");

        // ArtistsRelation
        LastfmArtistsRelationServiceImpl artistsRelationService = new LastfmArtistsRelationServiceImpl(jdbcTemplate, entityManager);
        artistsRelationService.initializeMetadata();
        String artistsRelationSql = artistsRelationService.buildUpsertSql(
            LastfmArtistsRelation.builder()
                .apiCall(EntityCreationHelper.createApiCall())
                .sourceArtist(EntityCreationHelper.createArtist())
                .targetArtist(EntityCreationHelper.createArtist())
                .relationType(LastfmEntityRelationType.SIMILARITY)
                .build());
        assertTrue(artistsRelationSql.contains("INSERT INTO artist_artist"), "ArtistsRelation table name should be 'artist_artist'");

        // ArtistAlbum
        LastfmArtistAlbumServiceImpl artistAlbumService = new LastfmArtistAlbumServiceImpl(jdbcTemplate, entityManager);
        artistAlbumService.initializeMetadata();
        String artistAlbumSql = artistAlbumService.buildUpsertSql(
            LastfmArtistAlbum.builder()
                .apiCall(EntityCreationHelper.createApiCall())
                .artist(EntityCreationHelper.createArtist())
                .album(EntityCreationHelper.createAlbum())
                .build());
        assertTrue(artistAlbumSql.contains("INSERT INTO artist_album"), "ArtistAlbum table name should be 'artist_album'");
    }

    @Test
    void extractConflictColumns_shouldHandleDifferentEntityTypes() {
        // Given - ArtistTag (different entity types)
        LastfmArtistTagServiceImpl service = new LastfmArtistTagServiceImpl(jdbcTemplate, entityManager);
        service.initializeMetadata();

        String sql = service.buildUpsertSql(
            LastfmArtistTag.builder()
                .apiCall(EntityCreationHelper.createApiCall())
                .artist(EntityCreationHelper.createArtist())
                .tag(EntityCreationHelper.createTag())
                .build());

        // Then - should use simple entity names
        assertTrue(sql.contains("ON CONFLICT (artist_id, tag_id)"),
            "Different entity types should use simple entity_id pattern");
    }

    @Test
    void extractConflictColumns_shouldHandleSameEntityTypes() {
        // Given - ArtistsRelation (same entity types)
        LastfmArtistsRelationServiceImpl service = new LastfmArtistsRelationServiceImpl(jdbcTemplate, entityManager);
        service.initializeMetadata();

        String sql = service.buildUpsertSql(
            LastfmArtistsRelation.builder()
                .apiCall(EntityCreationHelper.createApiCall())
                .sourceArtist(EntityCreationHelper.createArtist())
                .targetArtist(EntityCreationHelper.createArtist())
                .relationType(LastfmEntityRelationType.SIMILARITY)
                .build());

        // Then - should use source/target prefixes and include relation_type
        assertTrue(sql.contains("ON CONFLICT (source_artist_id, target_artist_id, relation_type)"),
            "Same entity types should use source_/target_ prefixes and include relation_type");
    }

    @Test
    void extractInsertColumns_shouldIncludeAllRelevantFields() {
        // Given
        LastfmArtistTagServiceImpl service = new LastfmArtistTagServiceImpl(jdbcTemplate, entityManager);
        service.initializeMetadata();

        String sql = service.buildUpsertSql(
            LastfmArtistTag.builder()
                .apiCall(EntityCreationHelper.createApiCall())
                .artist(EntityCreationHelper.createArtist())
                .tag(EntityCreationHelper.createTag())
                .build());

        // Then - should include @JoinColumn fields
        assertTrue(sql.contains("artist_id"), "Should include @JoinColumn field artist_id");
        assertTrue(sql.contains("tag_id"), "Should include @JoinColumn field tag_id");

        // Should include @Column fields
        assertTrue(sql.contains("usage_count"), "Should include @Column field usage_count");
        assertTrue(sql.contains("api_call_id"), "Should include @Column field api_call_id");
        assertTrue(sql.contains("created_at"), "Should include @Column field created_at");
        assertTrue(sql.contains("updated_at"), "Should include @Column field updated_at");

        // Should NOT include @Id field
        assertFalse(sql.contains("(id,") || sql.contains(" id,") || sql.contains(", id)"), "Should not include @Id field in insert columns");
    }

    @Test
    void parameterMapper_shouldBeBuiltAutomatically() {
        // Given
        LastfmArtistTagServiceImpl service = new LastfmArtistTagServiceImpl(jdbcTemplate, entityManager);
        service.initializeMetadata();

        // When - create a sample entity
        var sampleEntity = LastfmArtistTag.builder()
            .apiCall(EntityCreationHelper.createApiCall())
            .artist(EntityCreationHelper.createArtist())
            .tag(EntityCreationHelper.createTag())
            .build();

        // Then - parameter mapper should be available and work without throwing exceptions
        assertDoesNotThrow(() -> {
            String sql = service.buildUpsertSql(sampleEntity);
            assertNotNull(sql, "SQL should be generated successfully");
        }, "Parameter mapper should be built automatically without errors");
    }

    @Test
    void sameEntityRelation_shouldExtractCorrectTypes() {
        // Given - ArtistsRelation extends BaseLastfmSameEntityRelation<LastfmArtist>
        LastfmArtistsRelationServiceImpl service = new LastfmArtistsRelationServiceImpl(jdbcTemplate, entityManager);
        service.initializeMetadata();

        // When
        String sql = service.buildUpsertSql(
            LastfmArtistsRelation.builder()
                .apiCall(EntityCreationHelper.createApiCall())
                .sourceArtist(EntityCreationHelper.createArtist())
                .targetArtist(EntityCreationHelper.createArtist())
                .relationType(LastfmEntityRelationType.SIMILARITY)
                .build());

        // Then - should correctly identify that source and target are the same type (artist)
        assertTrue(sql.contains("source_artist_id"), "Should extract source entity as 'artist'");
        assertTrue(sql.contains("target_artist_id"), "Should extract target entity as 'artist'");
        assertTrue(sql.contains("relation_type"), "Should include relation_type for same entity relations");
    }
}
