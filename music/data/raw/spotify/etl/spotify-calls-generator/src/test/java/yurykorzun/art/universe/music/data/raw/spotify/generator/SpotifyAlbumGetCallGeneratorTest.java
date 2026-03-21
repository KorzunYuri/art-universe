package yurykorzun.art.universe.music.data.raw.spotify.generator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.music.data.raw.spotify.config.SpotifyGeneratorProperty;
import yurykorzun.art.universe.music.data.raw.spotify.domain.entity.SpotifyAlbum;
import yurykorzun.art.universe.music.data.raw.spotify.enums.SpotifyEntityType;
import yurykorzun.art.universe.music.data.raw.spotify.etl.dto.SpotifyApiCallCreateRequest;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCallType;
import yurykorzun.art.universe.music.data.raw.spotify.etl.service.SpotifyApiCallService;
import yurykorzun.art.universe.music.data.raw.spotify.task.call.generate.SpotifyApiCallEntityService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpotifyAlbumGetCallGeneratorTest {

    @Mock private SpotifyApiCallEntityService entityService;
    @Mock private SpotifyApiCallService apiCallService;
    @Mock private ConfigPropertyHolder configPropertyHolder;

    @InjectMocks
    private SpotifyAlbumGetCallGenerator generator;

    @Test
    void createApiCalls_shouldSkip_whenNoAlbumsNeedCalls() {
        when(entityService.findAllWithoutActiveCalls(SpotifyAlbum.class, SpotifyApiCallType.ALBUM_GET))
            .thenReturn(List.of());

        generator.createApiCalls();

        verify(apiCallService, never()).createApiCalls(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void createApiCalls_shouldCreateCallForEachAlbum() {
        SpotifyAlbum album1 = mock(SpotifyAlbum.class);
        when(album1.getId()).thenReturn(1L);
        when(album1.getSpotifyId()).thenReturn("album-spotify-1");

        SpotifyAlbum album2 = mock(SpotifyAlbum.class);
        when(album2.getId()).thenReturn(2L);
        when(album2.getSpotifyId()).thenReturn("album-spotify-2");

        when(entityService.findAllWithoutActiveCalls(SpotifyAlbum.class, SpotifyApiCallType.ALBUM_GET))
            .thenReturn(List.of(album1, album2));
        when(configPropertyHolder.getInt(SpotifyGeneratorProperty.DUE_DURATION_ALBUM_GET)).thenReturn(7);

        generator.createApiCalls();

        ArgumentCaptor<List<SpotifyApiCallCreateRequest>> captor = ArgumentCaptor.forClass(List.class);
        verify(apiCallService).createApiCalls(captor.capture());

        List<SpotifyApiCallCreateRequest> requests = captor.getValue();
        assertThat(requests).hasSize(2);
        assertThat(requests.get(0).getType()).isEqualTo(SpotifyApiCallType.ALBUM_GET);
        assertThat(requests.get(0).getEntityType()).isEqualTo(SpotifyEntityType.ALBUM);
        assertThat(requests.get(0).getSpotifyId()).isEqualTo("album-spotify-1");
        assertThat(requests.get(0).getEntityId()).isEqualTo(1L);
        assertThat(requests.get(1).getSpotifyId()).isEqualTo("album-spotify-2");
    }

    @Test
    void getApiCallType_shouldReturnAlbumGet() {
        assertThat(generator.getApiCallType()).isEqualTo(SpotifyApiCallType.ALBUM_GET);
    }
}
