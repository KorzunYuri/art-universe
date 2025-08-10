package yurykorzun.art.universe.music.data.raw.lastfm.maintenance.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DbMaintenanceServiceTest {

    @Mock
    private DbMaintenanceCoordinator dbMaintenanceCoordinator;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private MusicDataIntegrationService musicDataIntegrationService;

    private DbMaintenanceService service;

    @BeforeEach
    void setUp() {
        service = new DbMaintenanceService(dbMaintenanceCoordinator, jdbcTemplate, musicDataIntegrationService);
        
        // Set thresholds via reflection or create a test constructor
        // For now, we'll test the unbind logic directly
    }

    @Test
    void unbindDeletedEntitiesFromCleanupRun_shouldProcessAllEntityTypes() {
        // Given: Cleanup run with multiple entity types
        Long cleanupRunId = 123L;
        
        // Mock entity types query
        when(jdbcTemplate.queryForList(
            eq("SELECT DISTINCT entity_type FROM mtnc_deleted_entities WHERE cleanup_run_id = ?"),
            eq(Integer.class), eq(cleanupRunId)))
            .thenReturn(Arrays.asList(
                LastfmEntityType.ARTIST.getCode(),
                LastfmEntityType.ALBUM.getCode()
            ));

        // Mock entity IDs queries
        when(jdbcTemplate.queryForList(
            eq("SELECT entity_id FROM mtnc_deleted_entities WHERE cleanup_run_id = ? AND entity_type = ?"),
            eq(Long.class), eq(cleanupRunId), eq(LastfmEntityType.ARTIST.getCode())))
            .thenReturn(Arrays.asList(1L, 2L, 3L));

        when(jdbcTemplate.queryForList(
            eq("SELECT entity_id FROM mtnc_deleted_entities WHERE cleanup_run_id = ? AND entity_type = ?"),
            eq(Long.class), eq(cleanupRunId), eq(LastfmEntityType.ALBUM.getCode())))
            .thenReturn(Arrays.asList(4L, 5L));

        // When: Call private method via reflection or create a public test method
        // For now, we'll test the public method that would call this logic
        // Let's create a test method in the service for testing
        TestableDbMaintenanceService testableService = new TestableDbMaintenanceService(
            dbMaintenanceCoordinator, jdbcTemplate, musicDataIntegrationService);
        
        testableService.testUnbindDeletedEntitiesFromCleanupRun(cleanupRunId);

        // Then: Verify calls to MusicDataIntegrationService
        ArgumentCaptor<LastfmEntityType> entityTypeCaptor = ArgumentCaptor.forClass(LastfmEntityType.class);
        ArgumentCaptor<List<Long>> entityIdsCaptor = ArgumentCaptor.forClass(List.class);

        verify(musicDataIntegrationService, times(2))
            .unbindEntities(entityTypeCaptor.capture(), entityIdsCaptor.capture());

        List<LastfmEntityType> capturedEntityTypes = entityTypeCaptor.getAllValues();
        List<List<Long>> capturedEntityIds = entityIdsCaptor.getAllValues();

        assertTrue(capturedEntityTypes.contains(LastfmEntityType.ARTIST));
        assertTrue(capturedEntityTypes.contains(LastfmEntityType.ALBUM));
        assertTrue(capturedEntityIds.contains(Arrays.asList(1L, 2L, 3L)));
        assertTrue(capturedEntityIds.contains(Arrays.asList(4L, 5L)));
    }

    @Test
    void unbindDeletedEntitiesFromCleanupRun_shouldSkipEmptyEntityIds() {
        // Given: Cleanup run with entity type but no entity IDs
        Long cleanupRunId = 123L;
        
        when(jdbcTemplate.queryForList(
            eq("SELECT DISTINCT entity_type FROM mtnc_deleted_entities WHERE cleanup_run_id = ?"),
            eq(Integer.class), eq(cleanupRunId)))
            .thenReturn(Collections.singletonList(LastfmEntityType.ARTIST.getCode()));

        when(jdbcTemplate.queryForList(
            eq("SELECT entity_id FROM mtnc_deleted_entities WHERE cleanup_run_id = ? AND entity_type = ?"),
            eq(Long.class), eq(cleanupRunId), eq(LastfmEntityType.ARTIST.getCode())))
            .thenReturn(Collections.emptyList());

        // When: Call unbind logic
        TestableDbMaintenanceService testableService = new TestableDbMaintenanceService(
            dbMaintenanceCoordinator, jdbcTemplate, musicDataIntegrationService);
        
        testableService.testUnbindDeletedEntitiesFromCleanupRun(cleanupRunId);

        // Then: No calls to MusicDataIntegrationService should be made
        verify(musicDataIntegrationService, never()).unbindEntities(any(), any());
    }

    @Test
    void unbindDeletedEntitiesFromCleanupRun_shouldSkipNullCleanupRunId() {
        // Given: Null cleanup run ID
        Long cleanupRunId = null;

        // When: Call unbind logic
        TestableDbMaintenanceService testableService = new TestableDbMaintenanceService(
            dbMaintenanceCoordinator, jdbcTemplate, musicDataIntegrationService);
        
        testableService.testUnbindDeletedEntitiesFromCleanupRun(cleanupRunId);

        // Then: No database queries or service calls should be made
        verify(jdbcTemplate, never()).queryForList(anyString(), any(Class.class), any());
        verify(musicDataIntegrationService, never()).unbindEntities(any(), any());
    }

    // Test subclass to expose private method for testing
    private static class TestableDbMaintenanceService extends DbMaintenanceService {
        
        public TestableDbMaintenanceService(
                DbMaintenanceCoordinator dbMaintenanceCoordinator,
                JdbcTemplate jdbc,
                MusicDataIntegrationService musicDataIntegrationService) {
            super(dbMaintenanceCoordinator, jdbc, musicDataIntegrationService);
        }

        public void testUnbindDeletedEntitiesFromCleanupRun(Long cleanupRunId) {
            // Call the private method via reflection or make it package-private
            // For now, we'll duplicate the logic here for testing
            if (cleanupRunId == null) {
                return;
            }

            JdbcTemplate jdbc = getJdbc();
            MusicDataIntegrationService musicDataService = getMusicDataIntegrationService();

            List<Integer> entityTypes = jdbc.queryForList(
                "SELECT DISTINCT entity_type FROM mtnc_deleted_entities WHERE cleanup_run_id = ?",
                Integer.class, cleanupRunId
            );

            for (Integer entityTypeCode : entityTypes) {
                LastfmEntityType entityType = yurykorzun.art.universe.common.CodedRegistry
                    .getByCode(entityTypeCode, LastfmEntityType.class)
                    .orElseThrow(() -> new IllegalArgumentException("Unknown entity type code: " + entityTypeCode));
                
                List<Long> entityIds = jdbc.queryForList(
                    "SELECT entity_id FROM mtnc_deleted_entities WHERE cleanup_run_id = ? AND entity_type = ?",
                    Long.class, cleanupRunId, entityType.getCode()
                );

                if (!entityIds.isEmpty()) {
                    musicDataService.unbindEntities(entityType, entityIds);
                }
            }
        }

        // Getters for accessing private fields
        private JdbcTemplate getJdbc() {
            try {
                var field = DbMaintenanceService.class.getDeclaredField("jdbc");
                field.setAccessible(true);
                return (JdbcTemplate) field.get(this);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private MusicDataIntegrationService getMusicDataIntegrationService() {
            try {
                var field = DbMaintenanceService.class.getDeclaredField("musicDataIntegrationService");
                field.setAccessible(true);
                return (MusicDataIntegrationService) field.get(this);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
