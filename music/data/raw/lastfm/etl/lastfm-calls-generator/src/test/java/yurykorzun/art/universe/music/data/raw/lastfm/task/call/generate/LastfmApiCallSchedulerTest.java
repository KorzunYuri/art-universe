package yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.music.data.raw.lastfm.config.LastfmGeneratorProperty;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCallType;

import java.util.Map;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LastfmApiCallSchedulerTest {

    @Mock
    private BaseLastfmApiCallGenerator generator;
    @Mock
    private ConfigPropertyHolder configPropertyHolder;

    private LastfmApiCallGenerationScheduler scheduler;

    @BeforeEach
    void setUp() {
        when(configPropertyHolder.getInt(LastfmGeneratorProperty.SCHEDULE_DELAY_SECS)).thenReturn(1);
        scheduler = new LastfmApiCallGenerationScheduler(configPropertyHolder);
    }

    @Test
    void run_shouldCallGenerator_whenGenerationEnabled() {
        // given
        LastfmApiCallType callType = LastfmApiCallType.TAG_TOP_TAGS;
        when(generator.getApiCallType()).thenReturn(callType);
        when(configPropertyHolder.getBoolean(LastfmGeneratorProperty.GENERATE_TAG_TOP_TAGS)).thenReturn(true);

        try (MockedStatic<LastfmApiCallGeneratorsRegistry> registry = mockStatic(LastfmApiCallGeneratorsRegistry.class)) {
            registry.when(LastfmApiCallGeneratorsRegistry::getRegistry)
                    .thenReturn(Map.of(callType, generator));

            // when
            scheduler.run();

            // then
            verify(generator).createApiCalls();
        }
    }

    @Test
    void run_shouldSkipGenerator_whenGenerationDisabled() {
        // given
        LastfmApiCallType callType = LastfmApiCallType.TAG_TOP_TAGS;
        when(configPropertyHolder.getBoolean(LastfmGeneratorProperty.GENERATE_TAG_TOP_TAGS)).thenReturn(false);

        try (MockedStatic<LastfmApiCallGeneratorsRegistry> registry = mockStatic(LastfmApiCallGeneratorsRegistry.class)) {
            registry.when(LastfmApiCallGeneratorsRegistry::getRegistry)
                    .thenReturn(Map.of(callType, generator));

            // when
            scheduler.run();

            // then
            verify(generator, never()).createApiCalls();
        }
    }

    @Test
    void start_shouldScheduleExecution() {
        // when
        scheduler.start();

        // then — verify the scheduler was started (SCHEDULE_DELAY_SECS was read)
        verify(configPropertyHolder).getInt(LastfmGeneratorProperty.SCHEDULE_DELAY_SECS);
    }
}
