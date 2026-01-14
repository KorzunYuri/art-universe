package yurykorzun.art.universe.music.data.raw.lastfm.task.call.perform;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LastfmApiCallSchedulerTest {

    @Mock
    private LastfmCallsOrchestrator orchestrator;

    private LastfmApiCallExecutionScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new LastfmApiCallExecutionScheduler(orchestrator);
    }

    @Test
    void triggerApiCalls_shouldCallOrchestrator() {
        // when
        scheduler.triggerApiCallsExecution();

        // then
        verify(orchestrator).orchestrateApiCalls();
    }

    @Test
    void triggerApiCalls_shouldThrow_whenOrchestratorThrows() {
        // Given
        final var expectedMessage = "test";
        doThrow(new IllegalArgumentException(expectedMessage))
            .when(orchestrator).orchestrateApiCalls();

        // When
        IllegalArgumentException actualException = assertThrows(
            IllegalArgumentException.class,
            () -> scheduler.triggerApiCallsExecution());

        // Then
        assertEquals(expectedMessage, actualException.getMessage());
    }
}
