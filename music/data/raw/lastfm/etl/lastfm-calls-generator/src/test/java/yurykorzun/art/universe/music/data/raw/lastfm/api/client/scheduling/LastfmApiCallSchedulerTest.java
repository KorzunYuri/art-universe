package yurykorzun.art.universe.music.data.raw.lastfm.api.client.scheduling;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.BaseLastfmApiCallGenerator;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallGeneratorsRegistry;
import yurykorzun.art.universe.music.data.raw.lastfm.maintenance.service.TaskCoordinator;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LastfmApiCallSchedulerTest {

    @Mock
    private TaskCoordinator coordinator;
    @Mock
    private BaseLastfmApiCallGenerator generator;

    private LastfmApiCallGenerationScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new LastfmApiCallGenerationScheduler(coordinator);
    }

    @Test
    void generateApiCalls_shouldExecuteGenerators_whenCoordinatorAllows() {
        // given
        LastfmApiCallType apiCallType = mock(LastfmApiCallType.class);
        when(apiCallType.getMethod()).thenReturn("test.method");
        when(generator.getApiCallType()).thenReturn(apiCallType);
        
        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            return null;
        }).when(coordinator).executeIfAllowed(any(Runnable.class), eq(LastfmApiCallGenerationScheduler.TASK_NAME_API_CALLS_GENERATION));

        try (MockedStatic<LastfmApiCallGeneratorsRegistry> registry = mockStatic(LastfmApiCallGeneratorsRegistry.class)) {
            registry.when(LastfmApiCallGeneratorsRegistry::getRegistry)
                    .thenReturn(Map.of(apiCallType, generator));

            // when
            scheduler.generateApiCalls();

            // then
            verify(generator).createApiCalls();
            verify(coordinator).executeIfAllowed(any(Runnable.class), eq(LastfmApiCallGenerationScheduler.TASK_NAME_API_CALLS_GENERATION));
        }
    }

    @Test
    void generateApiCalls_shouldSkipExecution_whenCoordinatorBlocks() {
        // given
        doNothing().when(coordinator).executeIfAllowed(any(Runnable.class), eq(LastfmApiCallGenerationScheduler.TASK_NAME_API_CALLS_GENERATION));

        try (MockedStatic<LastfmApiCallGeneratorsRegistry> registry = mockStatic(LastfmApiCallGeneratorsRegistry.class)) {
            // when
            scheduler.generateApiCalls();

            // then
            registry.verifyNoInteractions();
            verify(coordinator).executeIfAllowed(any(Runnable.class), eq(LastfmApiCallGenerationScheduler.TASK_NAME_API_CALLS_GENERATION));
        }
    }
}
