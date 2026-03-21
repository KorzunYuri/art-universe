package yurykorzun.art.universe.music.data.raw.spotify.task.call.perform;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.common.pgnotify.PgNotifyEventPublisher;
import yurykorzun.art.universe.data.raw.common.integration.AdaptiveRateLimiter;
import yurykorzun.art.universe.music.data.raw.spotify.common.SpotifyConstants;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCall;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCallType;
import yurykorzun.art.universe.music.data.raw.spotify.etl.service.SpotifyApiCallService;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpotifyCallsOrchestratorTest {

    @Mock
    private SpotifyApiCallService apiCallService;
    @Mock
    private SpotifyApiCallExecutor executor;
    @Mock
    private AdaptiveRateLimiter rateLimiter;
    @Mock
    private PgNotifyEventPublisher pgNotifyEventPublisher;

    private SpotifyCallsOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        orchestrator = new SpotifyCallsOrchestrator(apiCallService, executor, rateLimiter, pgNotifyEventPublisher);
    }

    private SpotifyApiCall createApiCall(long id) {
        return SpotifyApiCall.builder()
            .type(SpotifyApiCallType.ARTIST_GET)
            .spotifyId("spotify-id-" + id)
            .dueDttm(Instant.now().plus(Duration.ofDays(1)))
            .build();
    }

    @Test
    void orchestrateApiCalls_shouldNotifyAfterCommit_whenAtLeastOneCallSucceeds() throws InterruptedException {
        // given
        SpotifyApiCall call1 = createApiCall(1L);
        SpotifyApiCall call2 = createApiCall(2L);
        when(apiCallService.findAllCreatedUnexpired()).thenReturn(List.of(call1, call2));
        when(executor.execute(call1)).thenReturn(false);
        when(executor.execute(call2)).thenReturn(true);

        // when
        orchestrator.orchestrateApiCalls();

        // then
        verify(pgNotifyEventPublisher).notifyAfterCommit(SpotifyConstants.NOTIFY_RESPONSES_READY);
    }

    @Test
    void orchestrateApiCalls_shouldNotNotify_whenNoCallsSucceed() throws InterruptedException {
        // given
        SpotifyApiCall call1 = createApiCall(1L);
        SpotifyApiCall call2 = createApiCall(2L);
        when(apiCallService.findAllCreatedUnexpired()).thenReturn(List.of(call1, call2));
        when(executor.execute(any())).thenReturn(false);

        // when
        orchestrator.orchestrateApiCalls();

        // then
        verifyNoInteractions(pgNotifyEventPublisher);
    }

    @Test
    void orchestrateApiCalls_shouldNotNotify_whenNoCallsExist() {
        // given
        when(apiCallService.findAllCreatedUnexpired()).thenReturn(List.of());

        // when
        orchestrator.orchestrateApiCalls();

        // then
        verifyNoInteractions(pgNotifyEventPublisher);
    }

    @Test
    void orchestrateApiCalls_shouldBreakLoop_whenInterrupted() throws InterruptedException {
        // given
        SpotifyApiCall call1 = createApiCall(1L);
        SpotifyApiCall call2 = createApiCall(2L);
        when(apiCallService.findAllCreatedUnexpired()).thenReturn(List.of(call1, call2));
        doThrow(new InterruptedException("interrupted")).when(rateLimiter).acquire();

        // when
        orchestrator.orchestrateApiCalls();

        // then — loop broke after first call, so executor never ran for any call
        verify(executor, never()).execute(any());
        verifyNoInteractions(pgNotifyEventPublisher);
        // thread interrupt flag should be re-set
        assert Thread.currentThread().isInterrupted();
        Thread.interrupted(); // clear for other tests
    }

    @Test
    void orchestrateApiCalls_shouldContinueToNextCall_whenExecutorThrowsException() throws InterruptedException {
        // given
        SpotifyApiCall call1 = createApiCall(1L);
        SpotifyApiCall call2 = createApiCall(2L);
        when(apiCallService.findAllCreatedUnexpired()).thenReturn(List.of(call1, call2));
        when(executor.execute(call1)).thenThrow(new RuntimeException("network error"));
        when(executor.execute(call2)).thenReturn(true);

        // when
        orchestrator.orchestrateApiCalls();

        // then — second call succeeded, so notify is sent
        verify(executor).execute(call1);
        verify(executor).execute(call2);
        verify(pgNotifyEventPublisher).notifyAfterCommit(SpotifyConstants.NOTIFY_RESPONSES_READY);
    }
}
