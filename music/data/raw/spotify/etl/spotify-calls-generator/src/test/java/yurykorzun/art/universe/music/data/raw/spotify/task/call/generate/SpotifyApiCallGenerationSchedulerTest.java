package yurykorzun.art.universe.music.data.raw.spotify.task.call.generate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.music.data.raw.spotify.config.SpotifyGeneratorProperty;
import yurykorzun.art.universe.music.data.raw.spotify.generator.SpotifyArtistGetCallGenerator;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpotifyApiCallGenerationSchedulerTest {

    @Mock private ThreadPoolTaskScheduler taskScheduler;
    @Mock private ConfigPropertyHolder configPropertyHolder;
    @Mock private ApplicationContext applicationContext;

    @InjectMocks
    private SpotifyApiCallGenerationScheduler scheduler;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        // Initialize static applicationContext for the registry so getRegistry() doesn't NPE
        new SpotifyApiCallGeneratorsRegistry(applicationContext);
        lenient().when(applicationContext.getBean(any(Class.class)))
            .thenReturn(mock(SpotifyArtistGetCallGenerator.class));
    }

    @Test
    void start_shouldScheduleTaskWithConfiguredDelay() {
        when(configPropertyHolder.getInt(SpotifyGeneratorProperty.SCHEDULE_DELAY_SECS)).thenReturn(60);

        scheduler.start();

        verify(taskScheduler).schedule(any(Runnable.class), any(Instant.class));
    }

    @Test
    void stop_shouldPreventTaskScheduling() {
        scheduler.stop();
        scheduler.start();

        verifyNoInteractions(taskScheduler);
    }

    @Test
    void executeAndReschedule_shouldAlwaysReschedule_afterSuccessfulRun() {
        when(configPropertyHolder.getInt(SpotifyGeneratorProperty.SCHEDULE_DELAY_SECS)).thenReturn(0);
        lenient().when(configPropertyHolder.getBoolean(any())).thenReturn(false);

        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        scheduler.start();
        verify(taskScheduler).schedule(runnableCaptor.capture(), any(Instant.class));

        runnableCaptor.getValue().run();

        verify(taskScheduler, times(2)).schedule(any(Runnable.class), any(Instant.class));
    }

    @Test
    void executeAndReschedule_shouldReschedule_evenWhenExceptionOccurs() {
        when(configPropertyHolder.getInt(SpotifyGeneratorProperty.SCHEDULE_DELAY_SECS)).thenReturn(0);
        // Force exception during generation — throws before any generator runs
        lenient().when(applicationContext.getBean(any(Class.class)))
            .thenThrow(new RuntimeException("context error"));

        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        scheduler.start();
        verify(taskScheduler).schedule(runnableCaptor.capture(), any(Instant.class));

        assertThatNoException().isThrownBy(() -> runnableCaptor.getValue().run());
        // scheduleNext() is called in finally block even after exception
        verify(taskScheduler, times(2)).schedule(any(Runnable.class), any(Instant.class));
    }
}
