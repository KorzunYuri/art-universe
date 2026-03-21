package yurykorzun.art.universe.music.data.raw.spotify.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.music.data.raw.spotify.config.SpotifyApplicatorProperty;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.StagingIteration;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.StagingIterationStatus;
import yurykorzun.art.universe.music.data.raw.spotify.etl.repository.SpotifyStagingIterationRepository;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StagingCleanupServiceTest {

    @Mock private SpotifyStagingIterationRepository iterationRepository;
    @Mock private JdbcTemplate jdbc;
    @Mock private ThreadPoolTaskScheduler taskScheduler;
    @Mock private ConfigPropertyHolder configPropertyHolder;

    @InjectMocks
    private StagingCleanupService cleanupService;

    @Test
    void start_shouldScheduleTask() {
        when(configPropertyHolder.getInt(SpotifyApplicatorProperty.STAGING_CLEANUP_DELAY_SECS)).thenReturn(60);

        cleanupService.start();

        verify(taskScheduler).schedule(any(Runnable.class), any(Instant.class));
    }

    @Test
    void stop_shouldPreventRescheduling() {
        when(configPropertyHolder.getInt(SpotifyApplicatorProperty.STAGING_CLEANUP_DELAY_SECS)).thenReturn(60);
        when(configPropertyHolder.getInt(SpotifyApplicatorProperty.STAGING_CLEANUP_RETENTION_HOURS)).thenReturn(240);
        when(iterationRepository.findAllByStatusInAndSealedAtBefore(anyList(), any(Instant.class))).thenReturn(List.of());

        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        cleanupService.start();
        verify(taskScheduler).schedule(runnableCaptor.capture(), any(Instant.class));

        cleanupService.stop();
        // Run the captured task — scheduleNext() should bail out because running=false
        runnableCaptor.getValue().run();

        // Only the initial schedule from start(); no reschedule after stop
        verify(taskScheduler, times(1)).schedule(any(Runnable.class), any(Instant.class));
    }

    @Test
    void cleanupOldStagingTables_shouldDoNothing_whenNoOldIterations() {
        when(configPropertyHolder.getInt(SpotifyApplicatorProperty.STAGING_CLEANUP_RETENTION_HOURS)).thenReturn(240);
        when(iterationRepository.findAllByStatusInAndSealedAtBefore(anyList(), any(Instant.class)))
            .thenReturn(List.of());

        cleanupService.cleanupOldStagingTables();

        verify(jdbc, never()).execute(anyString());
    }

    @Test
    void cleanupOldStagingTables_shouldDropFourTablesPerIteration() {
        when(configPropertyHolder.getInt(SpotifyApplicatorProperty.STAGING_CLEANUP_RETENTION_HOURS)).thenReturn(240);
        StagingIteration iter = mock(StagingIteration.class);
        when(iter.getId()).thenReturn(42L);
        when(iterationRepository.findAllByStatusInAndSealedAtBefore(
            eq(List.of(StagingIterationStatus.COMPLETED, StagingIterationStatus.FAILED)),
            any(Instant.class)
        )).thenReturn(List.of(iter));

        cleanupService.cleanupOldStagingTables();

        verify(jdbc, times(4)).execute(anyString());
        verify(jdbc).execute(contains("stg_artist_42"));
        verify(jdbc).execute(contains("stg_album_42"));
        verify(jdbc).execute(contains("stg_track_42"));
        verify(jdbc).execute(contains("stg_entity_relation_42"));
    }
}
