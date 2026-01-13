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

import java.util.Map;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LastfmApiCallSchedulerTest {

    @Mock
    private BaseLastfmApiCallGenerator generator;

    private LastfmApiCallGenerationScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new LastfmApiCallGenerationScheduler();
    }

    @Test
    void generateApiCalls_shouldCallAllGenerators() {
        // given
        LastfmApiCallType apiCallType = mock(LastfmApiCallType.class);
        when(apiCallType.getMethod()).thenReturn("test.method");
        when(generator.getApiCallType()).thenReturn(apiCallType);

        try (MockedStatic<LastfmApiCallGeneratorsRegistry> registry = mockStatic(LastfmApiCallGeneratorsRegistry.class)) {
            registry.when(LastfmApiCallGeneratorsRegistry::getRegistry)
                    .thenReturn(Map.of(apiCallType, generator));

            // when
            scheduler.generateApiCalls();

            // then
            verify(generator).createApiCalls();
        }
    }

}
