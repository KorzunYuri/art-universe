package yurykorzun.art.universe.music.data.raw.spotify.task.response.process.processor;

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
import yurykorzun.art.universe.music.data.raw.spotify.integration.dto.SpotifyAlbumDto;
import yurykorzun.art.universe.music.data.raw.spotify.integration.dto.SpotifyPagingDto;
import yurykorzun.art.universe.music.data.raw.spotify.integration.dto.SpotifySearchResultDto;
import yurykorzun.art.universe.music.data.raw.spotify.integration.dto.SpotifySimplifiedArtistDto;
import yurykorzun.art.universe.music.data.raw.spotify.integration.dto.SpotifyTrackDto;
import yurykorzun.art.universe.music.data.raw.spotify.staging.SearchMatchScoringService;
import yurykorzun.art.universe.music.data.raw.spotify.staging.StagingWriter;
import yurykorzun.art.universe.music.data.raw.spotify.staging.SyntheticIdResolutionService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpotifySearchTrackResponseProcessorTest {

    @Mock private ObjectMapper objectMapper;
    @Mock private StagingWriter stagingWriter;
    @Mock private SyntheticIdResolutionService idResolutionService;
    @Mock private SearchMatchScoringService searchMatchScoringService;

    @InjectMocks
    private SpotifySearchTrackResponseProcessor processor;

    private SpotifyApiResponse buildResponse(long apiCallId) {
        SpotifyApiCall apiCall = mock(SpotifyApiCall.class);
        when(apiCall.getId()).thenReturn(apiCallId);
        SpotifyApiResponse response = mock(SpotifyApiResponse.class);
        lenient().when(response.getId()).thenReturn(100L);
        when(response.getResponseBody()).thenReturn("{}");
        when(response.getApiCall()).thenReturn(apiCall);
        return response;
    }

    private StagingIteration buildIteration() {
        StagingIteration iteration = mock(StagingIteration.class);
        lenient().when(iteration.getId()).thenReturn(42L);
        return iteration;
    }

    @Test
    void process_shouldReturnZeroAndCallScoring_whenTracksPagingIsNull() throws IOException {
        SpotifySearchResultDto result = new SpotifySearchResultDto(null, null, null);
        when(objectMapper.readValue(anyString(), eq(SpotifySearchResultDto.class))).thenReturn(result);

        int count = processor.process(buildResponse(9L), buildIteration());

        assertThat(count).isZero();
        verify(searchMatchScoringService).scoreAndUpdate(9L, List.of(), List.of());
        verifyNoInteractions(stagingWriter, idResolutionService);
    }

    @Test
    void process_shouldStageTrackOnly_whenNoAlbumNoFeaturedArtists() throws IOException {
        SpotifyTrackDto track = new SpotifyTrackDto("track-1", "Song", "uri", 200000, 1, 1,
            false, true, null, null, Map.of(), null);
        SpotifyPagingDto<SpotifyTrackDto> paging = new SpotifyPagingDto<>(
            List.of(track), 1, 50, 0, null, null);
        SpotifySearchResultDto result = new SpotifySearchResultDto(null, null, paging);

        when(objectMapper.readValue(anyString(), eq(SpotifySearchResultDto.class))).thenReturn(result);
        when(idResolutionService.resolveId(SpotifyEntityType.TRACK, "track-1")).thenReturn(60L);

        int count = processor.process(buildResponse(9L), buildIteration());

        assertThat(count).isEqualTo(1);
        verify(stagingWriter).insertTrack(42L, 100L, track, 60L, null, null, null, null);
        verify(searchMatchScoringService).scoreAndUpdate(9L, List.of("Song"), List.of("track-1"));
    }

    @Test
    void process_shouldStageTrackAndEmbeddedAlbum() throws IOException {
        SpotifyAlbumDto album = new SpotifyAlbumDto("alb-1", "Album", "uri", "album", 10,
            "2020", "year", null, Map.of(), null);
        SpotifyTrackDto track = new SpotifyTrackDto("track-1", "Song", "uri", 200000, 1, 1,
            false, true, null, album, Map.of(), null);
        SpotifyPagingDto<SpotifyTrackDto> paging = new SpotifyPagingDto<>(
            List.of(track), 1, 50, 0, null, null);
        SpotifySearchResultDto result = new SpotifySearchResultDto(null, null, paging);

        when(objectMapper.readValue(anyString(), eq(SpotifySearchResultDto.class))).thenReturn(result);
        when(idResolutionService.resolveId(SpotifyEntityType.TRACK, "track-1")).thenReturn(60L);
        when(idResolutionService.resolveId(SpotifyEntityType.ALBUM, "alb-1")).thenReturn(50L);

        int count = processor.process(buildResponse(9L), buildIteration());

        // 1 track + 1 album + 1 album_track relation = 3
        assertThat(count).isEqualTo(3);
        verify(stagingWriter).insertTrack(42L, 100L, track, 60L, null, null, 50L, "alb-1");
        verify(stagingWriter).insertAlbum(42L, 100L, album, 50L, null, null);
        verify(stagingWriter).insertEntityRelation(42L, 100L,
            SpotifyEntityType.ALBUM.getCode(), 50L,
            SpotifyEntityType.TRACK.getCode(), 60L,
            SpotifyRelationType.ALBUM_TRACK.getCode());
    }

    @Test
    void process_shouldStageFeaturedArtistsAndRelations() throws IOException {
        SpotifySimplifiedArtistDto primary = new SpotifySimplifiedArtistDto("a-1", "Drake", "uri", Map.of());
        SpotifySimplifiedArtistDto featured = new SpotifySimplifiedArtistDto("a-2", "Future", "uri", Map.of());
        SpotifyTrackDto track = new SpotifyTrackDto("track-1", "Song", "uri", 200000, 1, 1,
            false, true, List.of(primary, featured), null, Map.of(), null);
        SpotifyPagingDto<SpotifyTrackDto> paging = new SpotifyPagingDto<>(
            List.of(track), 1, 50, 0, null, null);
        SpotifySearchResultDto result = new SpotifySearchResultDto(null, null, paging);

        when(objectMapper.readValue(anyString(), eq(SpotifySearchResultDto.class))).thenReturn(result);
        when(idResolutionService.resolveId(SpotifyEntityType.TRACK, "track-1")).thenReturn(60L);
        when(idResolutionService.resolveId(SpotifyEntityType.ARTIST, "a-1")).thenReturn(10L);
        when(idResolutionService.resolveId(SpotifyEntityType.ARTIST, "a-2")).thenReturn(11L);

        int count = processor.process(buildResponse(9L), buildIteration());

        // 1 track + 1 simplified artist + 1 track_artist relation = 3
        assertThat(count).isEqualTo(3);
        verify(stagingWriter).insertSimplifiedArtist(42L, 100L, featured, 11L);
        verify(stagingWriter).insertEntityRelation(42L, 100L,
            SpotifyEntityType.TRACK.getCode(), 60L,
            SpotifyEntityType.ARTIST.getCode(), 11L,
            SpotifyRelationType.TRACK_ARTIST.getCode());
    }

    @Test
    void getApiCallType_shouldReturnSearchTrack() {
        assertThat(processor.getApiCallType().name()).isEqualTo("SEARCH_TRACK");
    }
}
