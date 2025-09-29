package yurykorzun.art.universe.music.data.raw.lastfm.api.client.scheduling;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.maintenance.service.TaskCoordinator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LastfmApiCallSchedulerTest {

    @Mock
    private LastfmApiCallService lastfmApiCallService;
    @Mock
    private TaskCoordinator coordinator;

    private LastfmApiCallScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new LastfmApiCallScheduler(lastfmApiCallService, coordinator);
    }

    @Test
    void triggerApiCalls_shouldExecuteService_whenCoordinatorAllows() {
        // given
        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            return null;
        }).when(coordinator).executeIfAllowed(any(Runnable.class), eq(LastfmApiCallScheduler.TASK_NAME_API_CALLS_EXECUTION));

        // when
        scheduler.triggerApiCalls();

        // then
        verify(lastfmApiCallService).triggerApiCalls();
        verify(coordinator).executeIfAllowed(any(Runnable.class), eq(LastfmApiCallScheduler.TASK_NAME_API_CALLS_EXECUTION));
    }

    @Test
    void triggerApiCalls_shouldSkipExecution_whenCoordinatorBlocks() {
        // given
        doNothing().when(coordinator).executeIfAllowed(any(Runnable.class), eq(LastfmApiCallScheduler.TASK_NAME_API_CALLS_EXECUTION));

        // when
        scheduler.triggerApiCalls();

        // then
        verify(lastfmApiCallService, never()).triggerApiCalls();
        verify(coordinator).executeIfAllowed(any(Runnable.class), eq(LastfmApiCallScheduler.TASK_NAME_API_CALLS_EXECUTION));
    }
}
