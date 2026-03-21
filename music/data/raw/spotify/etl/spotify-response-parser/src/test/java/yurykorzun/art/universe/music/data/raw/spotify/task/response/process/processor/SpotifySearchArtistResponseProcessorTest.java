package yurykorzun.art.universe.music.data.raw.spotify.task.response.process.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.data.raw.spotify.enums.SpotifyEntityType;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCall;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiResponse;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.StagingIteration;
import yurykorzun.art.universe.music.data.raw.spotify.integration.dto.SpotifyArtistDto;
import yurykorzun.art.universe.music.data.raw.spotify.integration.dto.SpotifyPagingDto;
import yurykorzun.art.universe.music.data.raw.spotify.integration.dto.SpotifySearchResultDto;
import yurykorzun.art.universe.music.data.raw.spotify.staging.SearchMatchScoringService;
import yurykorzun.art.universe.music.data.raw.spotify.staging.StagingWriter;
import yurykorzun.art.universe.music.data.raw.spotify.staging.SyntheticIdResolutionService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class SpotifySearchArtistResponseProcessorTest {

    @Mock private ObjectMapper objectMapper;
    @Mock private StagingWriter stagingWriter;
    @Mock private SyntheticIdResolutionService idResolutionService;
    @Mock private SearchMatchScoringService searchMatchScoringService;

    @InjectMocks
    private SpotifySearchArtistResponseProcessor processor;

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
    void process_shouldReturnZeroAndCallScoring_whenArtistsPagingIsNull() throws IOException {
        SpotifySearchResultDto searchResult = new SpotifySearchResultDto(null, null, null);
        when(objectMapper.readValue(anyString(), eq(SpotifySearchResultDto.class))).thenReturn(searchResult);

        int count = processor.process(buildResponse(1L), buildIteration());

        assertThat(count).isZero();
        verify(searchMatchScoringService).scoreAndUpdate(1L, List.of(), List.of());
        verifyNoInteractions(stagingWriter, idResolutionService);
    }

    @Test
    void process_shouldReturnZeroAndCallScoring_whenArtistItemsIsNull() throws IOException {
        SpotifyPagingDto<SpotifyArtistDto> paging = new SpotifyPagingDto<>(null, 0, 0, 0, null, null);
        SpotifySearchResultDto searchResult = new SpotifySearchResultDto(paging, null, null);
        when(objectMapper.readValue(anyString(), eq(SpotifySearchResultDto.class))).thenReturn(searchResult);

        int count = processor.process(buildResponse(1L), buildIteration());

        assertThat(count).isZero();
        verify(searchMatchScoringService).scoreAndUpdate(1L, List.of(), List.of());
    }

    @Test
    void process_shouldStageArtistsAndCallScoring() throws IOException {
        SpotifyArtistDto artist1 = new SpotifyArtistDto("id-1", "Drake", "uri:1", "artist", null,
            Map.of("spotify", "https://open.spotify.com/artist/id-1"));
        SpotifyArtistDto artist2 = new SpotifyArtistDto("id-2", "Kendrick", "uri:2", "artist", null,
            Map.of());

        SpotifyPagingDto<SpotifyArtistDto> paging = new SpotifyPagingDto<>(
            List.of(artist1, artist2), 2, 50, 0, null, null);
        SpotifySearchResultDto searchResult = new SpotifySearchResultDto(paging, null, null);

        when(objectMapper.readValue(anyString(), eq(SpotifySearchResultDto.class))).thenReturn(searchResult);
        when(idResolutionService.resolveId(SpotifyEntityType.ARTIST, "id-1")).thenReturn(10L);
        when(idResolutionService.resolveId(SpotifyEntityType.ARTIST, "id-2")).thenReturn(20L);

        int count = processor.process(buildResponse(5L), buildIteration());

        assertThat(count).isEqualTo(2);
        verify(stagingWriter).insertArtist(42L, 100L, artist1, 10L);
        verify(stagingWriter).insertArtist(42L, 100L, artist2, 20L);
        verify(searchMatchScoringService).scoreAndUpdate(5L,
            List.of("Drake", "Kendrick"), List.of("id-1", "id-2"));
    }

    @Test
    void getApiCallType_shouldReturnSearchArtist() {
        assertThat(processor.getApiCallType().name()).isEqualTo("SEARCH_ARTIST");
    }
}
