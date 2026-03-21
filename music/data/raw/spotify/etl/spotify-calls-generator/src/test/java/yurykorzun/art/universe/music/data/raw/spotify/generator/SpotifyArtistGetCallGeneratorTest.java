package yurykorzun.art.universe.music.data.raw.spotify.generator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.music.data.raw.spotify.config.SpotifyGeneratorProperty;
import yurykorzun.art.universe.music.data.raw.spotify.domain.entity.SpotifyArtist;
import yurykorzun.art.universe.music.data.raw.spotify.etl.dto.SpotifyApiCallCreateRequest;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCallType;
import yurykorzun.art.universe.music.data.raw.spotify.etl.service.SpotifyApiCallService;
import yurykorzun.art.universe.music.data.raw.spotify.task.call.generate.SpotifyApiCallEntityService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpotifyArtistGetCallGeneratorTest {

    @Mock
    private SpotifyApiCallEntityService entityService;

    @Mock
    private SpotifyApiCallService apiCallService;

    @Mock
    private ConfigPropertyHolder configPropertyHolder;

    @InjectMocks
    private SpotifyArtistGetCallGenerator generator;

    @Test
    void createApiCalls_shouldSkip_whenNoArtistsNeedCalls() {
        when(entityService.findAllWithoutActiveCalls(SpotifyArtist.class, SpotifyApiCallType.ARTIST_GET))
            .thenReturn(List.of());

        generator.createApiCalls();

        verify(apiCallService, never()).createApiCalls(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void createApiCalls_shouldCreateCallForEachArtist() {
        SpotifyArtist artist1 = mock(SpotifyArtist.class);
        when(artist1.getId()).thenReturn(1L);
        when(artist1.getSpotifyId()).thenReturn("artist-spotify-1");

        SpotifyArtist artist2 = mock(SpotifyArtist.class);
        when(artist2.getId()).thenReturn(2L);
        when(artist2.getSpotifyId()).thenReturn("artist-spotify-2");

        when(entityService.findAllWithoutActiveCalls(SpotifyArtist.class, SpotifyApiCallType.ARTIST_GET))
            .thenReturn(List.of(artist1, artist2));
        when(configPropertyHolder.getInt(SpotifyGeneratorProperty.DUE_DURATION_ARTIST_GET)).thenReturn(7);

        generator.createApiCalls();

        ArgumentCaptor<List<SpotifyApiCallCreateRequest>> captor = ArgumentCaptor.forClass(List.class);
        verify(apiCallService).createApiCalls(captor.capture());

        List<SpotifyApiCallCreateRequest> requests = captor.getValue();
        assertThat(requests).hasSize(2);
        assertThat(requests.get(0).getType()).isEqualTo(SpotifyApiCallType.ARTIST_GET);
        assertThat(requests.get(0).getSpotifyId()).isEqualTo("artist-spotify-1");
        assertThat(requests.get(0).getEntityId()).isEqualTo(1L);
        assertThat(requests.get(1).getSpotifyId()).isEqualTo("artist-spotify-2");
    }
}
