package yurykorzun.art.universe.music.data.raw.lastfm.task.call.perform;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.common.data.raw.etl.entity.ApiCallStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.LastfmApiCallService;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LastfmCallsOrchestratorTest {

    @Mock
    private LastfmApiCallService apiCallService;
    @Mock
    private LastfmApiCallExecutor executor;

    private LastfmCallsOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        orchestrator = new LastfmCallsOrchestrator(
            apiCallService,
            executor,
            5.0 // limiter constant
        );
    }

    private static final Instant dueDttm = Instant.now().plus(Duration.ofDays(1));

    private LastfmApiCall createApiCall(Long id) {
        return LastfmApiCall.builder()
                .id(id)
                .type(LastfmApiCallType.TAG_TOP_TAGS)
                .dataSnapshotId(1L)
                .dueDttm(dueDttm)
                .status(ApiCallStatus.PENDING)
                .build();
    }

    @Test
    void orchestrateApiCalls_shouldFetchUnprocessedCalls_whenCalled() {
        // given
        when(apiCallService.findAllUnprocessedUnexpired()).thenReturn(List.of());

        // when
        orchestrator.orchestrateApiCalls();

        // then
        verify(apiCallService).findAllUnprocessedUnexpired();
    }

    @Test
    void orchestrateApiCalls_shouldExecuteEachCall_whenCallsExist() {
        // given
        LastfmApiCall call1 = createApiCall(1L);
        LastfmApiCall call2 = createApiCall(2L);
        LastfmApiCall call3 = createApiCall(3L);
        when(apiCallService.findAllUnprocessedUnexpired()).thenReturn(List.of(call1, call2, call3));

        // when
        orchestrator.orchestrateApiCalls();

        // then
        verify(executor).execute(call1);
        verify(executor).execute(call2);
        verify(executor).execute(call3);
    }

    @Test
    void orchestrateApiCalls_shouldNotExecuteAnyCalls_whenNoCallsExist() {
        // given
        when(apiCallService.findAllUnprocessedUnexpired()).thenReturn(List.of());

        // when
        orchestrator.orchestrateApiCalls();

        // then
        verify(executor, never()).execute(any());
    }

    @Test
    void orchestrateApiCalls_shouldContinueWithOtherCalls_whenOneCallFails() {
        // given
        LastfmApiCall call1 = createApiCall(1L);
        LastfmApiCall call2 = createApiCall(2L);
        LastfmApiCall call3 = createApiCall(3L);
        when(apiCallService.findAllUnprocessedUnexpired()).thenReturn(List.of(call1, call2, call3));
        doThrow(new RuntimeException("API error")).when(executor).execute(call2);

        // when
        orchestrator.orchestrateApiCalls();

        // then
        verify(executor).execute(call1);
        verify(executor).execute(call2);
        verify(executor).execute(call3);
    }
}
