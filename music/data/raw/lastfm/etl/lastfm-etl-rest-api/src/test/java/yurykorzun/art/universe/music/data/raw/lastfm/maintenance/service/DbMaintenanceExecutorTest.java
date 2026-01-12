package yurykorzun.art.universe.music.data.raw.lastfm.maintenance.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import yurykorzun.art.universe.common.observability.util.ObservabilityService;
import yurykorzun.art.universe.common.observability.util.NoOpObservabilityService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.LastfmEntityType;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DbMaintenanceExecutorTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private MusicDataIntegrationService musicDataIntegrationService;

    private DbMaintenanceExecutor executor;

    @BeforeEach
    void setUp() {
        executor = new DbMaintenanceExecutor(jdbcTemplate, musicDataIntegrationService, NoOpObservabilityService.getInstance());

        // Set thresholds via reflection
        ReflectionTestUtils.setField(executor, "artistThreshold", 1000);
        ReflectionTestUtils.setField(executor, "albumThreshold", 10000);
        ReflectionTestUtils.setField(executor, "trackThreshold", 10000);
        ReflectionTestUtils.setField(executor, "tagThreshold", 1000);

    }

    @Test
    void executeMaintenanceTasks_shouldPerformAllCleanupSteps() {
        // Given: Mock cleanup responses for all entity types
        Map<String, Object> artistCleanupResult = Map.of("cleanup_run_id", 100L, "message", "Artist cleanup");
        Map<String, Object> albumCleanupResult = Map.of("cleanup_run_id", 101L, "message", "Album cleanup");
        Map<String, Object> trackCleanupResult = Map.of("cleanup_run_id", 102L, "message", "Track cleanup");
        Map<String, Object> tagCleanupResult = Map.of("cleanup_run_id", 103L, "message", "Tag cleanup");

        when(jdbcTemplate.queryForList(
            eq("SELECT * FROM mtnc_cleanup_entity(?, ?, ?)"),
            eq(LastfmEntityType.ARTIST.getCode()), eq(1000), eq(false)))
            .thenReturn(List.of(artistCleanupResult));

        when(jdbcTemplate.queryForList(
            eq("SELECT * FROM mtnc_cleanup_entity(?, ?, ?)"),
            eq(LastfmEntityType.ALBUM.getCode()), eq(10000), eq(false)))
            .thenReturn(List.of(albumCleanupResult));

        when(jdbcTemplate.queryForList(
            eq("SELECT * FROM mtnc_cleanup_entity(?, ?, ?)"),
            eq(LastfmEntityType.TRACK.getCode()), eq(10000), eq(false)))
            .thenReturn(List.of(trackCleanupResult));

        when(jdbcTemplate.queryForList(
            eq("SELECT * FROM mtnc_cleanup_entity(?, ?, ?)"),
            eq(LastfmEntityType.TAG.getCode()), eq(1000), eq(false)))
            .thenReturn(List.of(tagCleanupResult));

        // Mock entity types and IDs for unbind operations
        when(jdbcTemplate.queryForList(
            eq("SELECT DISTINCT entity_type FROM mtnc_deleted_entities WHERE cleanup_run_id = ?"),
            eq(Integer.class), anyLong()))
            .thenReturn(Collections.emptyList());

        // When: Execute maintenance tasks
        executor.performMaintenance();

        // Then: Verify all cleanup operations were called
        verify(jdbcTemplate).queryForList(
            "SELECT * FROM mtnc_cleanup_entity(?, ?, ?)",
            LastfmEntityType.ARTIST.getCode(), 1000, false);
        verify(jdbcTemplate).queryForList(
            "SELECT * FROM mtnc_cleanup_entity(?, ?, ?)",
            LastfmEntityType.ALBUM.getCode(), 10000, false);
        verify(jdbcTemplate).queryForList(
            "SELECT * FROM mtnc_cleanup_entity(?, ?, ?)",
            LastfmEntityType.TRACK.getCode(), 10000, false);
        verify(jdbcTemplate).queryForList(
            "SELECT * FROM mtnc_cleanup_entity(?, ?, ?)",
            LastfmEntityType.TAG.getCode(), 1000, false);

        // Verify database optimization was called
        verify(jdbcTemplate).execute("ANALYZE");
    }

    @Test
    void unbindDeletedEntitiesFromCleanupRun_shouldProcessAllEntityTypes() {
        // Given: Cleanup run with multiple entity types
        List<LastfmEntityType> entityTypes = List.of(
            LastfmEntityType.ARTIST,
            LastfmEntityType.ALBUM,
            LastfmEntityType.TRACK,
            LastfmEntityType.TAG
        );

        for (int i = 0; i < entityTypes.size(); i++) {
            Long cleanupRunId = i + 1L;
            Map<String, Object> cleanupResult = Map.of("cleanup_run_id", cleanupRunId, "message", "Cleanup");
            LastfmEntityType entityType = entityTypes.get(i);

            // mock cleanup function query
            when(jdbcTemplate.queryForList(
                eq("SELECT * FROM mtnc_cleanup_entity(?, ?, ?)"),
                eq(entityType.getCode()), anyInt(), eq(false)))
                .thenReturn(List.of(cleanupResult));

            // Mock entity types query
            when(jdbcTemplate.queryForList(
                eq("SELECT DISTINCT entity_type FROM mtnc_deleted_entities WHERE cleanup_run_id = ?"),
                eq(Integer.class), eq(cleanupRunId)))
                .thenReturn(Collections.singletonList(entityType.getCode()));

            // Mock entity IDs queries
            final int base = i * 1000;
            List<Long> ids = Stream.of(1L, 2L, 3L)
                    .map(id -> base + id)
                    .toList();
            when(jdbcTemplate.queryForList(
                eq("SELECT entity_id FROM mtnc_deleted_entities WHERE cleanup_run_id = ? AND entity_type = ?"),
                eq(Long.class), eq(cleanupRunId), eq(entityType.getCode())))
                .thenReturn(ids);
        }

        // When: Execute maintenance (which triggers unbind)
        executor.performMaintenance();

        // Then: Verify calls to MusicDataIntegrationService
        ArgumentCaptor<LastfmEntityType> entityTypeCaptor = ArgumentCaptor.forClass(LastfmEntityType.class);
        ArgumentCaptor<List<Long>> entityIdsCaptor = ArgumentCaptor.forClass(List.class);

        verify(musicDataIntegrationService, times(entityTypes.size()))
            .unbindEntities(entityTypeCaptor.capture(), entityIdsCaptor.capture());

        List<LastfmEntityType> capturedEntityTypes = entityTypeCaptor.getAllValues();
        List<List<Long>> capturedEntityIds = entityIdsCaptor.getAllValues();

        for (int i = 0; i < entityTypes.size(); i++) {
            assertTrue(capturedEntityTypes.contains(entityTypes.get(i)));
            int base = i * 1000;
            List<Long> ids = Stream.of(1L, 2L, 3L)
                .map(id -> base + id)
                .toList();
            assertTrue(capturedEntityIds.contains(ids));
        }
    }

    @Test
    void unbindDeletedEntitiesFromCleanupRun_shouldSkipEmptyEntityIds() {
        // Given: Cleanup run with entity type but no entity IDs
        Long cleanupRunId = 123L;

        Map<String, Object> cleanupResult = Map.of("cleanup_run_id", cleanupRunId, "message", "Cleanup");
        when(jdbcTemplate.queryForList(
            eq("SELECT * FROM mtnc_cleanup_entity(?, ?, ?)"),
            anyInt(), anyInt(), eq(false)))
            .thenReturn(List.of(cleanupResult));

        when(jdbcTemplate.queryForList(
            eq("SELECT DISTINCT entity_type FROM mtnc_deleted_entities WHERE cleanup_run_id = ?"),
            eq(Integer.class), eq(cleanupRunId)))
            .thenReturn(Collections.singletonList(LastfmEntityType.ARTIST.getCode()));

        when(jdbcTemplate.queryForList(
            eq("SELECT entity_id FROM mtnc_deleted_entities WHERE cleanup_run_id = ? AND entity_type = ?"),
            eq(Long.class), eq(cleanupRunId), eq(LastfmEntityType.ARTIST.getCode())))
            .thenReturn(Collections.emptyList());

        // When: Execute maintenance
        executor.performMaintenance();

        // Then: No calls to MusicDataIntegrationService should be made
        verify(musicDataIntegrationService, never()).unbindEntities(any(), any());
    }

    @Test
    void unbindDeletedEntitiesFromCleanupRun_shouldSkipNullCleanupRunId() {
        // Given: Cleanup returns empty results (null cleanup run ID)
        when(jdbcTemplate.queryForList(
            eq("SELECT * FROM mtnc_cleanup_entity(?, ?, ?)"),
            anyInt(), anyInt(), eq(false)))
            .thenReturn(Collections.emptyList());

        // When: Execute maintenance
        executor.performMaintenance();

        // Then: No unbind queries should be made
        verify(jdbcTemplate, never()).queryForList(
            eq("SELECT DISTINCT entity_type FROM mtnc_deleted_entities WHERE cleanup_run_id = ?"),
            any(Class.class), anyLong());
        verify(musicDataIntegrationService, never()).unbindEntities(any(), any());
    }
}
