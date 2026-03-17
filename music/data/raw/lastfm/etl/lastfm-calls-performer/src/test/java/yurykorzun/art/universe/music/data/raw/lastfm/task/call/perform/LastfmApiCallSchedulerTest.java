package yurykorzun.art.universe.music.data.raw.lastfm.task.call.perform;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LastfmApiCallSchedulerTest {

    @Mock
    private LastfmApiCallsOrchestrator orchestrator;

    private LastfmApiCallExecutionScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new LastfmApiCallExecutionScheduler(orchestrator);
    }

    @Test
    void executeWork_shouldCallOrchestrator() {
        scheduler.executeWork();

        verify(orchestrator).orchestrateApiCalls();
    }

    @Test
    void executeWork_shouldNotPropagateException() {
        doThrow(new RuntimeException("orchestrator error")).when(orchestrator).orchestrateApiCalls();

        scheduler.executeWork();

        verify(orchestrator).orchestrateApiCalls();
    }
}
