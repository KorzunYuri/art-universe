package yurykorzun.art.universe.music.data.raw.spotify.staging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.common.pgnotify.PgNotifyEventPublisher;
import yurykorzun.art.universe.music.data.raw.spotify.common.SpotifyConstants;
import yurykorzun.art.universe.music.data.raw.spotify.config.SpotifyParserProperty;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.StagingIteration;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.StagingIterationStatus;
import yurykorzun.art.universe.music.data.raw.spotify.etl.repository.SpotifyStagingIterationRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StagingIterationServiceImplTest {

    @Mock
    private SpotifyStagingIterationRepository repository;

    @Mock
    private StagingWriter stagingWriter;

    @Mock
    private ConfigPropertyHolder configPropertyHolder;

    @Mock
    private PgNotifyEventPublisher pgNotifyEventPublisher;

    @InjectMocks
    private StagingIterationServiceImpl service;

    @Test
    void getOrCreateOpenIteration_shouldReturnExisting_whenOpenIterationExists() {
        // given
        StagingIteration existing = StagingIteration.builder().build();
        when(repository.findFirstByStatusOrderByOpenedAtAsc(StagingIterationStatus.OPEN))
            .thenReturn(Optional.of(existing));

        // when
        StagingIteration result = service.getOrCreateOpenIteration();

        // then
        assertSame(existing, result);
        verify(repository, never()).save(any());
        verify(stagingWriter, never()).createTablesForIteration(anyLong());
    }

    @Test
    void getOrCreateOpenIteration_shouldCreateNew_whenNoOpenIterationExists() {
        // given
        StagingIteration saved = StagingIteration.builder().build();
        when(repository.findFirstByStatusOrderByOpenedAtAsc(StagingIterationStatus.OPEN))
            .thenReturn(Optional.empty());
        when(repository.save(any())).thenReturn(saved);

        // when
        StagingIteration result = service.getOrCreateOpenIteration();

        // then
        assertNotNull(result);
        verify(repository).save(any(StagingIteration.class));
        verify(stagingWriter).createTablesForIteration(saved.getId());
    }

    @Test
    void seal_shouldSetStatusToSealed_andPublishNotification() {
        // given
        StagingIteration iteration = StagingIteration.builder()
            .recordsStaged(5L)
            .build();
        when(repository.save(any())).thenReturn(iteration);

        // when
        service.seal(iteration);

        // then
        assertEquals(StagingIterationStatus.SEALED, iteration.getStatus());
        assertNotNull(iteration.getSealedAt());
        verify(repository).save(iteration);
        verify(pgNotifyEventPublisher).notifyAfterCommit(SpotifyConstants.NOTIFY_ITERATIONS_SEALED);
    }

    @Test
    void shouldSeal_shouldReturnFalse_whenNoRecordsStaged() {
        // given
        StagingIteration iteration = StagingIteration.builder()
            .recordsStaged(0L)
            .build();

        // when
        boolean result = service.shouldSeal(iteration);

        // then
        assertFalse(result);
        verifyNoInteractions(configPropertyHolder);
    }

    @Test
    void shouldSeal_shouldReturnTrue_whenRecordsStagedExceedsMax() {
        // given
        StagingIteration iteration = StagingIteration.builder()
            .recordsStaged(100L)
            .build();
        when(configPropertyHolder.getInt(SpotifyParserProperty.STAGING_ITERATION_MAX_RECORDS))
            .thenReturn(50);

        // when
        boolean result = service.shouldSeal(iteration);

        // then
        assertTrue(result);
    }

    @Test
    void shouldSeal_shouldReturnFalse_whenBelowMaxRecordsAndNotExpired() {
        // given
        StagingIteration iteration = StagingIteration.builder()
            .recordsStaged(10L)
            .openedAt(Instant.now())
            .build();
        when(configPropertyHolder.getInt(SpotifyParserProperty.STAGING_ITERATION_MAX_RECORDS))
            .thenReturn(100);
        when(configPropertyHolder.getInt(SpotifyParserProperty.STAGING_ITERATION_MAX_OPEN_MINUTES))
            .thenReturn(60);

        // when
        boolean result = service.shouldSeal(iteration);

        // then
        assertFalse(result);
    }

    @Test
    void shouldSeal_shouldReturnTrue_whenMaxOpenTimeExceeded() {
        // given
        StagingIteration iteration = StagingIteration.builder()
            .recordsStaged(1L)
            .openedAt(Instant.now().minus(2, ChronoUnit.HOURS))
            .build();
        when(configPropertyHolder.getInt(SpotifyParserProperty.STAGING_ITERATION_MAX_RECORDS))
            .thenReturn(1000);
        when(configPropertyHolder.getInt(SpotifyParserProperty.STAGING_ITERATION_MAX_OPEN_MINUTES))
            .thenReturn(60);

        // when
        boolean result = service.shouldSeal(iteration);

        // then
        assertTrue(result);
    }

    @Test
    void incrementRecordsStaged_shouldAddCountToExistingValue() {
        // given
        StagingIteration iteration = StagingIteration.builder()
            .recordsStaged(5L)
            .build();
        when(repository.save(any())).thenReturn(iteration);

        // when
        service.incrementRecordsStaged(iteration, 3);

        // then
        assertEquals(8L, iteration.getRecordsStaged());
        verify(repository).save(iteration);
    }
}
