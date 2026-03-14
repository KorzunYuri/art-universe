package yurykorzun.art.universe.music.data.raw.lastfm.task.call.perform;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.music.data.raw.lastfm.config.LastfmPerformerProperty;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LastfmApiCallSchedulerTest {

    @Mock
    private LastfmApiCallsOrchestrator orchestrator;
    @Mock
    private ThreadPoolTaskScheduler taskScheduler;
    @Mock
    private ConfigPropertyHolder configPropertyHolder;

    private LastfmApiCallExecutionScheduler scheduler;

    @BeforeEach
    void setUp() {
        when(configPropertyHolder.getInt(LastfmPerformerProperty.SCHEDULE_DELAY_SECS)).thenReturn(1);
        scheduler = new LastfmApiCallExecutionScheduler(orchestrator, taskScheduler, configPropertyHolder);
    }

    private Runnable captureScheduledRunnable() {
        ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        verify(taskScheduler, atLeastOnce()).schedule(captor.capture(), any(Instant.class));
        return captor.getValue();
    }

    @Test
    void start_shouldScheduleExecution() {
        // when
        scheduler.start();

        // then
        verify(taskScheduler).schedule(any(Runnable.class), any(Instant.class));
    }

    @Test
    void start_shouldCallOrchestrator_whenScheduledRunnableRuns() {
        // given
        scheduler.start();
        Runnable scheduledTask = captureScheduledRunnable();

        // when
        scheduledTask.run();

        // then
        verify(orchestrator).orchestrateApiCalls();
    }

    @Test
    void executeAndReschedule_shouldReschedule_afterEachRun() {
        // given
        scheduler.start();
        Runnable scheduledTask = captureScheduledRunnable();

        // when
        scheduledTask.run();

        // then — scheduled twice: once from start(), once from finally in executeAndReschedule()
        verify(taskScheduler, times(2)).schedule(any(Runnable.class), any(Instant.class));
    }

    @Test
    void executeAndReschedule_shouldReschedule_evenWhenOrchestratorThrows() {
        // given
        doThrow(new RuntimeException("orchestrator error")).when(orchestrator).orchestrateApiCalls();
        scheduler.start();
        Runnable scheduledTask = captureScheduledRunnable();

        // when — must not propagate the exception
        scheduledTask.run();

        // then — rescheduled despite the error
        verify(taskScheduler, times(2)).schedule(any(Runnable.class), any(Instant.class));
    }

    @Test
    void stop_shouldPreventFurtherScheduling() {
        // given
        scheduler.start();
        scheduler.stop();

        // Capture the runnable scheduled by start()
        ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        verify(taskScheduler).schedule(captor.capture(), any(Instant.class));

        // when — run it after stop() was called
        captor.getValue().run();

        // then — no rescheduling because running=false
        verify(taskScheduler, times(1)).schedule(any(Runnable.class), any(Instant.class));
    }
}
