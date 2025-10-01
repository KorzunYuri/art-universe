package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.scheduling;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiResponseService;
import yurykorzun.art.universe.music.data.raw.lastfm.maintenance.service.TaskCoordinator;



import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doAnswer;

@ExtendWith(MockitoExtension.class)
class LastfmApiResponseProcessingSchedulerTest {

    @Mock
    private LastfmApiResponseService apiResponseService;

    @Mock
    private TaskCoordinator coordinator;

    private LastfmApiResponseProcessingScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new LastfmApiResponseProcessingScheduler(apiResponseService, coordinator);
    }

    @Test
    void triggerResponsesProcessing_shouldExecuteTaskThroughCoordinator_whenCalled() {
        // Given
        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            return null;
        }).when(coordinator).executeIfAllowed(any(Runnable.class), eq(LastfmApiResponseProcessingScheduler.TASK_NAME_API_RESPONSES_PROCESSING));

        // When
        scheduler.triggerResponsesProcessing();

        // Then
        verify(coordinator).executeIfAllowed(any(Runnable.class), eq(LastfmApiResponseProcessingScheduler.TASK_NAME_API_RESPONSES_PROCESSING));
        verify(apiResponseService).processResponses();
    }

    @Test
    void triggerResponsesProcessing_shouldNotCallService_whenCoordinatorBlocksExecution() {
        // Given
        doAnswer(invocation -> null).when(coordinator).executeIfAllowed(any(Runnable.class), eq(LastfmApiResponseProcessingScheduler.TASK_NAME_API_RESPONSES_PROCESSING));

        // When
        scheduler.triggerResponsesProcessing();

        // Then
        verify(coordinator).executeIfAllowed(any(Runnable.class), eq(LastfmApiResponseProcessingScheduler.TASK_NAME_API_RESPONSES_PROCESSING));
        // Service should not be called when coordinator blocks execution
    }
}
