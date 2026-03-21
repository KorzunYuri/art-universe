package yurykorzun.art.universe.music.data.raw.spotify.task.response.process.processor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.data.raw.spotify.enums.SpotifyEntityType;
import yurykorzun.art.universe.music.data.raw.spotify.enums.SpotifyRelationType;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCall;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiResponse;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.StagingIteration;
import yurykorzun.art.universe.music.data.raw.spotify.integration.dto.SpotifyPagingDto;
import yurykorzun.art.universe.music.data.raw.spotify.integration.dto.SpotifySimplifiedArtistDto;
import yurykorzun.art.universe.music.data.raw.spotify.integration.dto.SpotifyTrackDto;
import yurykorzun.art.universe.music.data.raw.spotify.staging.StagingWriter;
import yurykorzun.art.universe.music.data.raw.spotify.staging.SyntheticIdResolutionService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpotifyAlbumTracksResponseProcessorTest {

    @Mock private ObjectMapper objectMapper;
    @Mock private StagingWriter stagingWriter;
    @Mock private SyntheticIdResolutionService idResolutionService;

    @InjectMocks
    private SpotifyAlbumTracksResponseProcessor processor;

    private SpotifyApiResponse buildResponse(String albumSpotifyId) {
        SpotifyApiCall apiCall = mock(SpotifyApiCall.class);
        lenient().when(apiCall.getSpotifyId()).thenReturn(albumSpotifyId);
        SpotifyApiResponse response = mock(SpotifyApiResponse.class);
        lenient().when(response.getId()).thenReturn(100L);
        when(response.getResponseBody()).thenReturn("{}");
        lenient().when(response.getApiCall()).thenReturn(apiCall);
        return response;
    }

    private StagingIteration buildIteration() {
        StagingIteration iteration = mock(StagingIteration.class);
        lenient().when(iteration.getId()).thenReturn(42L);
        return iteration;
    }

    @SuppressWarnings("unchecked")
    @Test
    void process_shouldReturnZero_whenTracksListIsEmpty() throws IOException {
        SpotifyPagingDto<SpotifyTrackDto> paging = new SpotifyPagingDto<>(
            List.of(), 0, 50, 0, null, null);
        when(objectMapper.readValue(anyString(), any(TypeReference.class))).thenReturn(paging);

        int count = processor.process(buildResponse("album-x"), buildIteration());

        assertThat(count).isZero();
        verifyNoInteractions(stagingWriter, idResolutionService);
    }

    @SuppressWarnings("unchecked")
    @Test
    void process_shouldStageTracksAndRelations() throws IOException {
        SpotifySimplifiedArtistDto primaryArtist = new SpotifySimplifiedArtistDto(
            "artist-1", "Drake", "uri", Map.of());
        SpotifyTrackDto track = new SpotifyTrackDto("track-1", "Song", "uri", 180000, 1, 1,
            false, true, List.of(primaryArtist), null, Map.of(), null);
        SpotifyPagingDto<SpotifyTrackDto> paging = new SpotifyPagingDto<>(
            List.of(track), 1, 50, 0, null, null);

        when(objectMapper.readValue(anyString(), any(TypeReference.class))).thenReturn(paging);
        when(idResolutionService.resolveId(SpotifyEntityType.ALBUM, "album-1")).thenReturn(50L);
        when(idResolutionService.resolveId(SpotifyEntityType.TRACK, "track-1")).thenReturn(60L);
        when(idResolutionService.resolveId(SpotifyEntityType.ARTIST, "artist-1")).thenReturn(10L);

        int count = processor.process(buildResponse("album-1"), buildIteration());

        // 1 track + 1 relation = 2
        assertThat(count).isEqualTo(2);
        verify(stagingWriter).insertTrack(42L, 100L, track, 60L, 10L, "artist-1", 50L, "album-1");
        verify(stagingWriter).insertEntityRelation(42L, 100L,
            SpotifyEntityType.ALBUM.getCode(), 50L,
            SpotifyEntityType.TRACK.getCode(), 60L,
            SpotifyRelationType.ALBUM_TRACK.getCode());
    }

    @SuppressWarnings("unchecked")
    @Test
    void process_shouldStageFeaturedArtistRelations() throws IOException {
        SpotifySimplifiedArtistDto primary = new SpotifySimplifiedArtistDto("a-1", "Drake", "uri", Map.of());
        SpotifySimplifiedArtistDto featured = new SpotifySimplifiedArtistDto("a-2", "Future", "uri", Map.of());
        SpotifyTrackDto track = new SpotifyTrackDto("track-1", "Song", "uri", 180000, 1, 1,
            false, true, List.of(primary, featured), null, Map.of(), null);
        SpotifyPagingDto<SpotifyTrackDto> paging = new SpotifyPagingDto<>(
            List.of(track), 1, 50, 0, null, null);

        when(objectMapper.readValue(anyString(), any(TypeReference.class))).thenReturn(paging);
        when(idResolutionService.resolveId(SpotifyEntityType.ALBUM, "album-1")).thenReturn(50L);
        when(idResolutionService.resolveId(SpotifyEntityType.TRACK, "track-1")).thenReturn(60L);
        when(idResolutionService.resolveId(SpotifyEntityType.ARTIST, "a-1")).thenReturn(10L);
        when(idResolutionService.resolveId(SpotifyEntityType.ARTIST, "a-2")).thenReturn(11L);

        int count = processor.process(buildResponse("album-1"), buildIteration());

        // 1 track + 1 album_track relation + 1 track_artist relation = 3
        assertThat(count).isEqualTo(3);
        verify(stagingWriter).insertEntityRelation(42L, 100L,
            SpotifyEntityType.TRACK.getCode(), 60L,
            SpotifyEntityType.ARTIST.getCode(), 11L,
            SpotifyRelationType.TRACK_ARTIST.getCode());
    }

    @Test
    void getApiCallType_shouldReturnAlbumTracks() {
        assertThat(processor.getApiCallType().name()).isEqualTo("ALBUM_TRACKS");
    }
}
