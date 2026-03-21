package yurykorzun.art.universe.music.data.raw.spotify.generator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.music.data.raw.spotify.config.SpotifyGeneratorProperty;
import yurykorzun.art.universe.music.data.raw.spotify.domain.entity.SpotifyTrack;
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
class SpotifyTrackGetCallGeneratorTest {

    @Mock private SpotifyApiCallEntityService entityService;
    @Mock private SpotifyApiCallService apiCallService;
    @Mock private ConfigPropertyHolder configPropertyHolder;

    @InjectMocks
    private SpotifyTrackGetCallGenerator generator;

    @Test
    void createApiCalls_shouldSkip_whenNoTracksNeedCalls() {
        when(entityService.findAllWithoutActiveCalls(SpotifyTrack.class, SpotifyApiCallType.TRACK_GET))
            .thenReturn(List.of());

        generator.createApiCalls();

        verify(apiCallService, never()).createApiCalls(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void createApiCalls_shouldCreateCallForEachTrack() {
        SpotifyTrack track1 = mock(SpotifyTrack.class);
        when(track1.getId()).thenReturn(1L);
        when(track1.getSpotifyId()).thenReturn("track-spotify-1");

        SpotifyTrack track2 = mock(SpotifyTrack.class);
        when(track2.getId()).thenReturn(2L);
        when(track2.getSpotifyId()).thenReturn("track-spotify-2");

        when(entityService.findAllWithoutActiveCalls(SpotifyTrack.class, SpotifyApiCallType.TRACK_GET))
            .thenReturn(List.of(track1, track2));
        when(configPropertyHolder.getInt(SpotifyGeneratorProperty.DUE_DURATION_TRACK_GET)).thenReturn(7);

        generator.createApiCalls();

        ArgumentCaptor<List<SpotifyApiCallCreateRequest>> captor = ArgumentCaptor.forClass(List.class);
        verify(apiCallService).createApiCalls(captor.capture());

        List<SpotifyApiCallCreateRequest> requests = captor.getValue();
        assertThat(requests).hasSize(2);
        assertThat(requests.get(0).getType()).isEqualTo(SpotifyApiCallType.TRACK_GET);
        assertThat(requests.get(0).getEntityType()).isEqualTo(SpotifyEntityType.TRACK);
        assertThat(requests.get(0).getSpotifyId()).isEqualTo("track-spotify-1");
        assertThat(requests.get(0).getEntityId()).isEqualTo(1L);
        assertThat(requests.get(1).getSpotifyId()).isEqualTo("track-spotify-2");
    }

    @Test
    void getApiCallType_shouldReturnTrackGet() {
        assertThat(generator.getApiCallType()).isEqualTo(SpotifyApiCallType.TRACK_GET);
    }
}
