package yurykorzun.art.universe.music.data.raw.spotify.task.call.perform;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpotifyApiCallExecutionSchedulerTest {

    @Mock private SpotifyCallsOrchestrator orchestrator;

    @InjectMocks
    private SpotifyApiCallExecutionScheduler scheduler;

    @Test
    void executeWork_shouldDelegateToOrchestrator() {
        scheduler.executeWork();

        verify(orchestrator).orchestrateApiCalls();
    }

    @Test
    void executeWork_shouldNotPropagateException_whenOrchestratorThrows() {
        doThrow(new RuntimeException("unexpected error")).when(orchestrator).orchestrateApiCalls();

        assertThatNoException().isThrownBy(() -> scheduler.executeWork());
        verify(orchestrator).orchestrateApiCalls();
    }
}
