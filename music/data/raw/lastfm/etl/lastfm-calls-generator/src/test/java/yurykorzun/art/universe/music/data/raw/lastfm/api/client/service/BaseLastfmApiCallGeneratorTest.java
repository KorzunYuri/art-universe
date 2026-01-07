package yurykorzun.art.universe.music.data.raw.lastfm.api.client.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BaseLastfmApiCallGeneratorTest {

    @Test
    void constructor_shouldRegisterInRegistry() {
        try (MockedStatic<LastfmApiCallGeneratorsRegistry> registry = mockStatic(LastfmApiCallGeneratorsRegistry.class)) {
            // when
            TestableGenerator generator = new TestableGenerator();

            // then
            registry.verify(() -> LastfmApiCallGeneratorsRegistry.register(LastfmApiCallType.ARTIST_GET_INFO, TestableGenerator.class));
        }
    }

    private static class TestableGenerator extends BaseLastfmApiCallGenerator {
        @Override
        public LastfmApiCallType getApiCallType() {
            return LastfmApiCallType.ARTIST_GET_INFO;
        }

        @Override
        public void createApiCalls() {
            // Test implementation
        }
    }
}
