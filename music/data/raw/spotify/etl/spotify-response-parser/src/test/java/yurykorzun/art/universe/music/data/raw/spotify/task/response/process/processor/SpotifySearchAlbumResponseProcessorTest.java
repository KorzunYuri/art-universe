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
class SpotifySearchAlbumResponseProcessorTest {

    @Mock private ObjectMapper objectMapper;
    @Mock private StagingWriter stagingWriter;
    @Mock private SyntheticIdResolutionService idResolutionService;
    @Mock private SearchMatchScoringService searchMatchScoringService;

    @InjectMocks
    private SpotifySearchAlbumResponseProcessor processor;

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
    void process_shouldReturnZeroAndCallScoring_whenAlbumsPagingIsNull() throws IOException {
        SpotifySearchResultDto result = new SpotifySearchResultDto(null, null, null);
        when(objectMapper.readValue(anyString(), eq(SpotifySearchResultDto.class))).thenReturn(result);

        int count = processor.process(buildResponse(7L), buildIteration());

        assertThat(count).isZero();
        verify(searchMatchScoringService).scoreAndUpdate(7L, List.of(), List.of());
        verifyNoInteractions(stagingWriter, idResolutionService);
    }

    @Test
    void process_shouldStageAlbumsAndArtistsAndCallScoring() throws IOException {
        SpotifySimplifiedArtistDto artist = new SpotifySimplifiedArtistDto("a-1", "Drake", "uri", Map.of());
        SpotifyAlbumDto album = new SpotifyAlbumDto("alb-1", "Take Care", "uri", "album", 20,
            "2011", "day", List.of(artist), Map.of(), null);

        SpotifyPagingDto<SpotifyAlbumDto> paging = new SpotifyPagingDto<>(
            List.of(album), 1, 50, 0, null, null);
        SpotifySearchResultDto result = new SpotifySearchResultDto(null, paging, null);

        when(objectMapper.readValue(anyString(), eq(SpotifySearchResultDto.class))).thenReturn(result);
        when(idResolutionService.resolveId(SpotifyEntityType.ALBUM, "alb-1")).thenReturn(50L);
        when(idResolutionService.resolveId(SpotifyEntityType.ARTIST, "a-1")).thenReturn(10L);

        int count = processor.process(buildResponse(7L), buildIteration());

        // 1 album + 1 simplified artist + 1 relation = 3
        assertThat(count).isEqualTo(3);
        verify(stagingWriter).insertAlbum(eq(42L), eq(100L), eq(album), eq(50L), eq(10L), eq("a-1"));
        verify(stagingWriter).insertSimplifiedArtist(42L, 100L, artist, 10L);
        verify(stagingWriter).insertEntityRelation(42L, 100L,
            SpotifyEntityType.ARTIST.getCode(), 10L,
            SpotifyEntityType.ALBUM.getCode(), 50L,
            SpotifyRelationType.ARTIST_ALBUM.getCode());
        verify(searchMatchScoringService).scoreAndUpdate(7L, List.of("Take Care"), List.of("alb-1"));
    }

    @Test
    void process_shouldStageAlbumWithoutArtists_whenArtistsListIsNull() throws IOException {
        SpotifyAlbumDto album = new SpotifyAlbumDto("alb-2", "Album", "uri", "single", 1,
            "2020", "year", null, Map.of(), null);
        SpotifyPagingDto<SpotifyAlbumDto> paging = new SpotifyPagingDto<>(
            List.of(album), 1, 50, 0, null, null);
        SpotifySearchResultDto result = new SpotifySearchResultDto(null, paging, null);

        when(objectMapper.readValue(anyString(), eq(SpotifySearchResultDto.class))).thenReturn(result);
        when(idResolutionService.resolveId(SpotifyEntityType.ALBUM, "alb-2")).thenReturn(55L);

        int count = processor.process(buildResponse(7L), buildIteration());

        assertThat(count).isEqualTo(1);
        verify(stagingWriter).insertAlbum(eq(42L), eq(100L), eq(album), eq(55L), isNull(), isNull());
        verify(searchMatchScoringService).scoreAndUpdate(7L, List.of("Album"), List.of("alb-2"));
    }

    @Test
    void getApiCallType_shouldReturnSearchAlbum() {
        assertThat(processor.getApiCallType().name()).isEqualTo("SEARCH_ALBUM");
    }
}
