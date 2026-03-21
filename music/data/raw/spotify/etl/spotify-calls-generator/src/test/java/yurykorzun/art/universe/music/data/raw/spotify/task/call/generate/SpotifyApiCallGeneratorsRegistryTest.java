package yurykorzun.art.universe.music.data.raw.spotify.task.call.generate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCallType;
import yurykorzun.art.universe.music.data.raw.spotify.generator.SpotifyArtistGetCallGenerator;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpotifyApiCallGeneratorsRegistryTest {

    @Mock private ApplicationContext applicationContext;

    @Test
    void get_shouldReturnBeanFromApplicationContext_whenTypeIsRegistered() {
        new SpotifyApiCallGeneratorsRegistry(applicationContext);
        SpotifyApiCallGeneratorsRegistry.register(SpotifyApiCallType.ARTIST_GET, SpotifyArtistGetCallGenerator.class);

        SpotifyArtistGetCallGenerator mockGenerator = mock(SpotifyArtistGetCallGenerator.class);
        when(applicationContext.getBean(SpotifyArtistGetCallGenerator.class)).thenReturn(mockGenerator);

        BaseSpotifyApiCallGenerator result = SpotifyApiCallGeneratorsRegistry.get(SpotifyApiCallType.ARTIST_GET);

        assertThat(result).isSameAs(mockGenerator);
    }

    @Test
    void register_shouldNotOverwriteExistingEntry_whenSameTypeRegisteredAgain() {
        new SpotifyApiCallGeneratorsRegistry(applicationContext);
        SpotifyApiCallGeneratorsRegistry.register(SpotifyApiCallType.ARTIST_GET, SpotifyArtistGetCallGenerator.class);
        // Attempt to overwrite with a different class — putIfAbsent should ignore this
        SpotifyApiCallGeneratorsRegistry.register(SpotifyApiCallType.ARTIST_GET, SpotifyApiCallGeneratorsRegistryTest.DummyGenerator.class);

        SpotifyArtistGetCallGenerator mockGenerator = mock(SpotifyArtistGetCallGenerator.class);
        // If the original class is still mapped, this stub will be used
        when(applicationContext.getBean(SpotifyArtistGetCallGenerator.class)).thenReturn(mockGenerator);

        assertThat(SpotifyApiCallGeneratorsRegistry.get(SpotifyApiCallType.ARTIST_GET)).isSameAs(mockGenerator);
    }

    @Test
    @SuppressWarnings("unchecked")
    void getRegistry_shouldReturnUnmodifiableMap_containingRegisteredType() {
        new SpotifyApiCallGeneratorsRegistry(applicationContext);
        SpotifyApiCallGeneratorsRegistry.register(SpotifyApiCallType.ARTIST_GET, SpotifyArtistGetCallGenerator.class);
        lenient().when(applicationContext.getBean(any(Class.class)))
            .thenReturn(mock(SpotifyArtistGetCallGenerator.class));

        Map<SpotifyApiCallType, BaseSpotifyApiCallGenerator> registry = SpotifyApiCallGeneratorsRegistry.getRegistry();

        assertThat(registry).containsKey(SpotifyApiCallType.ARTIST_GET);
        assertThatThrownBy(() -> registry.put(SpotifyApiCallType.ALBUM_GET, null))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void size_shouldBePositive_afterRegistration() {
        SpotifyApiCallGeneratorsRegistry.register(SpotifyApiCallType.ARTIST_GET, SpotifyArtistGetCallGenerator.class);

        assertThat(SpotifyApiCallGeneratorsRegistry.size()).isPositive();
    }

    // Minimal concrete generator for testing register overwrite protection
    static class DummyGenerator extends BaseSpotifyApiCallGenerator {
        @Override public SpotifyApiCallType getApiCallType() { return SpotifyApiCallType.ARTIST_GET; }
        @Override public void createApiCalls() {}
    }
}
