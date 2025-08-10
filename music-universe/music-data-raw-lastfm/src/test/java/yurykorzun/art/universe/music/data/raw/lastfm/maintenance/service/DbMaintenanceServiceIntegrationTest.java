package yurykorzun.art.universe.music.data.raw.lastfm.maintenance.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.entity.LastfmAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.entity.LastfmTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaOnlyTest;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
public class DbMaintenanceServiceIntegrationTest extends JpaOnlyTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DbConsistencyHelper dbHelper;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        dbHelper.cleanup();
    }

    @Test
    void mtnc_cleanup_entity_shouldPerformDryRun_whenDryRunIsTrue() {
        // Given: Create test entities with low popularity scores
        LastfmArtist lowPopularityArtist = dbHelper.createAndSaveArtist(builder -> builder
            .name("Low Popularity Artist")
            .listenersCount(500)); // Below threshold of 1000

        LastfmArtist highPopularityArtist = dbHelper.createAndSaveArtist(builder -> builder
            .name("High Popularity Artist")
            .listenersCount(5000)); // Above threshold

        // Create some relationships
        LastfmTag tag = dbHelper.createAndSaveTag();
        dbHelper.createAndSaveArtistTag(lowPopularityArtist, tag);
        dbHelper.createAndSaveArtistTag(highPopularityArtist, tag);

        // Flush to ensure entities are persisted to database
        entityManager.flush();

        // When: Execute dry run cleanup
        List<Map<String, Object>> results = jdbcTemplate.queryForList(
            "SELECT * FROM mtnc_cleanup_entity(?, ?, ?)",
            LastfmEntityType.ARTIST.getCode(), 1000, true
        );

        // Then: Verify dry run results
        assertFalse(results.isEmpty(), "Dry run should return cleanup history");
        
        // Verify entities still exist (dry run shouldn't delete anything)
        assertTrue(jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM artist WHERE id = ?", 
            Integer.class, lowPopularityArtist.getId()) > 0);
        assertTrue(jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM artist WHERE id = ?", 
            Integer.class, highPopularityArtist.getId()) > 0);

        // Verify cleanup run was created
        Integer runCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM mtnc_cleanup_run WHERE dry_run = true", 
            Integer.class);
        assertEquals(1, runCount);

        // Verify no deleted entities were recorded (dry run)
        Integer deletedCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM mtnc_deleted_entities", 
            Integer.class);
        assertEquals(0, deletedCount);
    }

    @Test
    void mtnc_cleanup_entity_shouldDeleteLowPopularityEntities_whenDryRunIsFalse() {
        // Given: Create test entities
        LastfmArtist lowPopularityArtist = dbHelper.createAndSaveArtist(builder -> builder
            .name("Low Popularity Artist")
            .listenersCount(500)); // Below threshold

        LastfmArtist highPopularityArtist = dbHelper.createAndSaveArtist(builder -> builder
            .name("High Popularity Artist")
            .listenersCount(5000)); // Above threshold

        // Create relationships that should be cleaned up via CASCADE
        LastfmTag tag = dbHelper.createAndSaveTag();
        dbHelper.createAndSaveArtistTag(lowPopularityArtist, tag);
        dbHelper.createAndSaveArtistTag(highPopularityArtist, tag);

        Long lowPopularityArtistId = lowPopularityArtist.getId();
        Long highPopularityArtistId = highPopularityArtist.getId();

        // Flush to ensure entities are persisted to database
        entityManager.flush();

        // When: Execute actual cleanup
        List<Map<String, Object>> results = jdbcTemplate.queryForList(
            "SELECT * FROM mtnc_cleanup_entity(?, ?, ?)",
            LastfmEntityType.ARTIST.getCode(), 1000, false
        );

        // Then: Verify cleanup results
        assertFalse(results.isEmpty(), "Cleanup should return history");

        // Verify low popularity artist was deleted
        Integer lowPopularityCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM artist WHERE id = ?", 
            Integer.class, lowPopularityArtistId);
        assertEquals(0, lowPopularityCount, "Low popularity artist should be deleted");

        // Verify high popularity artist still exists
        Integer highPopularityCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM artist WHERE id = ?", 
            Integer.class, highPopularityArtistId);
        assertEquals(1, highPopularityCount, "High popularity artist should remain");

        // Verify relationships were cleaned up via CASCADE
        Integer relationshipCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM artist_tag WHERE artist_id = ?", 
            Integer.class, lowPopularityArtistId);
        assertEquals(0, relationshipCount, "Relationships should be cleaned up via CASCADE");

        // Verify cleanup run was created
        Integer runCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM mtnc_cleanup_run WHERE dry_run = false", 
            Integer.class);
        assertEquals(1, runCount);

        // Verify deleted entities were recorded
        Integer deletedCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM mtnc_deleted_entities WHERE entity_type = ? AND entity_id = ?", 
            Integer.class, LastfmEntityType.ARTIST.getCode(), lowPopularityArtistId);
        assertEquals(1, deletedCount, "Deleted entity should be recorded");
    }

    @Test
    void mtnc_cleanup_entity_shouldHandleMultipleEntityTypes() {
        // Given: Create entities of different types with low popularity
        LastfmArtist lowPopularityArtist = dbHelper.createAndSaveArtist(builder -> builder
            .listenersCount(500)); // Below threshold 1000

        LastfmAlbum lowPopularityAlbum = dbHelper.createAndSaveAlbum(builder -> builder
            .playCount(5000L)); // Below threshold 10000

        LastfmTrack lowPopularityTrack = dbHelper.createAndSaveTrack(builder -> builder
            .playCount(5000L)); // Below threshold 10000

        LastfmTag lowPopularityTag = dbHelper.createAndSaveTag(builder -> builder
            .usageCount(500)); // Below threshold 1000

        // Flush to ensure entities are persisted to database
        entityManager.flush();

        // Verify entities are created with PENDING status (default)
        Integer artistStatus = jdbcTemplate.queryForObject(
            "SELECT approval_status FROM artist WHERE id = ?", 
            Integer.class, lowPopularityArtist.getId());
        assertEquals(1, artistStatus, "Artist should have PENDING status");
        
        Integer albumStatus = jdbcTemplate.queryForObject(
            "SELECT approval_status FROM album WHERE id = ?", 
            Integer.class, lowPopularityAlbum.getId());
        assertEquals(1, albumStatus, "Album should have PENDING status");
        
        Integer trackStatus = jdbcTemplate.queryForObject(
            "SELECT approval_status FROM track WHERE id = ?", 
            Integer.class, lowPopularityTrack.getId());
        assertEquals(1, trackStatus, "Track should have PENDING status");
        
        Integer tagStatus = jdbcTemplate.queryForObject(
            "SELECT approval_status FROM tag WHERE id = ?", 
            Integer.class, lowPopularityTag.getId());
        assertEquals(1, tagStatus, "Tag should have PENDING status");

        // When & Then: Test each entity type cleanup separately
        // Each cleanup should be done in isolation to avoid interference
        testEntityTypeCleanup(LastfmEntityType.ARTIST, 1000, lowPopularityArtist.getId());
        testEntityTypeCleanup(LastfmEntityType.ALBUM, 10000, lowPopularityAlbum.getId());
        testEntityTypeCleanup(LastfmEntityType.TRACK, 10000, lowPopularityTrack.getId());
        testEntityTypeCleanup(LastfmEntityType.TAG, 1000, lowPopularityTag.getId());
    }

    @Test
    void mtnc_cleanup_entity_shouldOnlyDeletePendingEntities() {
        // Given: Create artists with different approval statuses
        LastfmArtist pendingArtist = dbHelper.createAndSaveArtist(builder -> builder
            .listenersCount(500)); // Below threshold, PENDING status

        // Flush to ensure entity is persisted to database
        entityManager.flush();

        // Verify it's created with PENDING status
        Integer initialStatus = jdbcTemplate.queryForObject(
            "SELECT approval_status FROM artist WHERE id = ?", 
            Integer.class, pendingArtist.getId());
        assertEquals(1, initialStatus, "Artist should be created with PENDING status");

        // Manually update approval status to APPROVED
        int updatedRows = jdbcTemplate.update(
            "UPDATE artist SET approval_status = 2 WHERE id = ?", 
            pendingArtist.getId());
        assertEquals(1, updatedRows, "Should update exactly one row");

        // Verify it's now APPROVED
        Integer updatedStatus = jdbcTemplate.queryForObject(
            "SELECT approval_status FROM artist WHERE id = ?", 
            Integer.class, pendingArtist.getId());
        assertEquals(2, updatedStatus, "Artist should now have APPROVED status");

        // When: Execute cleanup
        jdbcTemplate.queryForList(
            "SELECT * FROM mtnc_cleanup_entity(?, ?, ?)",
            LastfmEntityType.ARTIST.getCode(), 1000, false
        );

        // Then: Verify approved artist was not deleted
        Integer artistCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM artist WHERE id = ?", 
            Integer.class, pendingArtist.getId());
        assertEquals(1, artistCount, "Approved artist should not be deleted even if below threshold");

        // Verify no deleted entities were recorded
        Integer deletedCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM mtnc_deleted_entities WHERE entity_id = ?", 
            Integer.class, pendingArtist.getId());
        assertEquals(0, deletedCount);
    }

    @Test
    void mtnc_cleanup_entity_shouldHandleDivisionByZero_whenNoDataExists() {
        // Given: Empty database (no entities, no attribute_history, etc.)
        dbHelper.cleanup();

        // When: Execute dry run cleanup (should not fail with division by zero)
        assertDoesNotThrow(() -> {
            List<Map<String, Object>> results = jdbcTemplate.queryForList(
                "SELECT * FROM mtnc_cleanup_entity(?, ?, ?)",
                LastfmEntityType.ARTIST.getCode(), 1000, true
            );

            // Then: Should return results without error
            assertFalse(results.isEmpty(), "Should return cleanup history even with empty database");
            
            // Verify cleanup run was created
            Integer runCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM mtnc_cleanup_run WHERE dry_run = true", 
                Integer.class);
            assertTrue(runCount > 0, "Cleanup run should be created");
        });
    }

    @Test
    void mtnc_cleanup_entity_shouldReturnCleanupHistory() {
        // Given: Create test data
        dbHelper.createAndSaveArtist(builder -> builder.listenersCount(500));

        // When: Execute cleanup
        List<Map<String, Object>> results = jdbcTemplate.queryForList(
            "SELECT cleanup_run_id, message FROM mtnc_cleanup_entity(?, ?, ?) ORDER BY ts",
            LastfmEntityType.ARTIST.getCode(), 1000, false
        );

        // Then: Verify history contains expected messages
        assertFalse(results.isEmpty());
        
        List<String> messages = results.stream()
            .map(row -> (String) row.get("message"))
            .toList();

        assertTrue(messages.stream().anyMatch(msg -> msg.contains("entities to remove")));
        assertTrue(messages.stream().anyMatch(msg -> msg.contains("recorded") && msg.contains("entities for unbinding")));
        assertTrue(messages.stream().anyMatch(msg -> msg.contains("cleanup attribute_history started")));
        assertTrue(messages.stream().anyMatch(msg -> msg.contains("cleanup api_call started")));
        assertTrue(messages.stream().anyMatch(msg -> msg.contains("cleanup root table started")));

        // Verify all messages have the same cleanup_run_id
        Long firstRunId = (Long) results.get(0).get("cleanup_run_id");
        assertTrue(results.stream().allMatch(row -> 
            firstRunId.equals(row.get("cleanup_run_id"))));
    }

    private void testEntityTypeCleanup(LastfmEntityType entityType, int threshold, Long entityId) {
        // Execute cleanup
        jdbcTemplate.queryForList(
            "SELECT * FROM mtnc_cleanup_entity(?, ?, ?)",
            entityType.getCode(), threshold, false
        );

        // Verify entity was deleted
        String tableName = getTableNameForEntityType(entityType);
        Integer entityCount = jdbcTemplate.queryForObject(
            String.format("SELECT COUNT(*) FROM %s WHERE id = ?", tableName), 
            Integer.class, entityId);
        assertEquals(0, entityCount, 
            String.format("Low popularity %s should be deleted", entityType.name().toLowerCase()));

        // Verify deleted entity was recorded
        Integer deletedCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM mtnc_deleted_entities WHERE entity_type = ? AND entity_id = ?", 
            Integer.class, entityType.getCode(), entityId);
        assertEquals(1, deletedCount, 
            String.format("Deleted %s should be recorded", entityType.name().toLowerCase()));
    }

    private String getTableNameForEntityType(LastfmEntityType entityType) {
        return switch (entityType) {
            case ARTIST -> "artist";
            case ALBUM -> "album";
            case TRACK -> "track";
            case TAG -> "tag";
        };
    }
}
