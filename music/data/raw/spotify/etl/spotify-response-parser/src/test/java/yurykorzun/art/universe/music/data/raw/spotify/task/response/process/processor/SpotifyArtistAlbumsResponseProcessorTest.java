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
import yurykorzun.art.universe.music.data.raw.spotify.integration.dto.SpotifyAlbumDto;
import yurykorzun.art.universe.music.data.raw.spotify.integration.dto.SpotifyPagingDto;
import yurykorzun.art.universe.music.data.raw.spotify.integration.dto.SpotifySimplifiedArtistDto;
import yurykorzun.art.universe.music.data.raw.spotify.staging.StagingWriter;
import yurykorzun.art.universe.music.data.raw.spotify.staging.SyntheticIdResolutionService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpotifyArtistAlbumsResponseProcessorTest {

    @Mock private ObjectMapper objectMapper;
    @Mock private StagingWriter stagingWriter;
    @Mock private SyntheticIdResolutionService idResolutionService;

    @InjectMocks
    private SpotifyArtistAlbumsResponseProcessor processor;

    private SpotifyApiResponse buildResponse(String artistSpotifyId) {
        SpotifyApiCall apiCall = mock(SpotifyApiCall.class);
        lenient().when(apiCall.getSpotifyId()).thenReturn(artistSpotifyId);
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
    void process_shouldReturnZero_whenAlbumsListIsEmpty() throws IOException {
        SpotifyPagingDto<SpotifyAlbumDto> paging = new SpotifyPagingDto<>(
            List.of(), 0, 50, 0, null, null);
        when(objectMapper.readValue(anyString(), any(TypeReference.class))).thenReturn(paging);

        int count = processor.process(buildResponse("artist-x"), buildIteration());

        assertThat(count).isZero();
        verifyNoInteractions(stagingWriter, idResolutionService);
    }

    @SuppressWarnings("unchecked")
    @Test
    void process_shouldStageAlbumAndArtistAlbumRelation_whenNoAlbumArtists() throws IOException {
        SpotifyAlbumDto album = new SpotifyAlbumDto("alb-1", "Album", "uri", "album", 10,
            "2020", "year", null, Map.of(), null);
        SpotifyPagingDto<SpotifyAlbumDto> paging = new SpotifyPagingDto<>(
            List.of(album), 1, 50, 0, null, null);

        when(objectMapper.readValue(anyString(), any(TypeReference.class))).thenReturn(paging);
        when(idResolutionService.resolveId(SpotifyEntityType.ARTIST, "artist-1")).thenReturn(10L);
        when(idResolutionService.resolveId(SpotifyEntityType.ALBUM, "alb-1")).thenReturn(50L);

        int count = processor.process(buildResponse("artist-1"), buildIteration());

        // 1 album + 1 ARTIST_ALBUM relation = 2
        assertThat(count).isEqualTo(2);
        verify(stagingWriter).insertAlbum(eq(42L), eq(100L), eq(album), eq(50L), eq(10L), eq("artist-1"));
        verify(stagingWriter).insertEntityRelation(42L, 100L,
            SpotifyEntityType.ARTIST.getCode(), 10L,
            SpotifyEntityType.ALBUM.getCode(), 50L,
            SpotifyRelationType.ARTIST_ALBUM.getCode());
    }

    @SuppressWarnings("unchecked")
    @Test
    void process_shouldStageAlbumArtistsAndTheirRelations() throws IOException {
        SpotifySimplifiedArtistDto albumArtist = new SpotifySimplifiedArtistDto("collab-1", "Collab", "uri", Map.of());
        SpotifyAlbumDto album = new SpotifyAlbumDto("alb-1", "Collab Album", "uri", "album", 5,
            "2021", "year", List.of(albumArtist), Map.of(), null);
        SpotifyPagingDto<SpotifyAlbumDto> paging = new SpotifyPagingDto<>(
            List.of(album), 1, 50, 0, null, null);

        when(objectMapper.readValue(anyString(), any(TypeReference.class))).thenReturn(paging);
        when(idResolutionService.resolveId(SpotifyEntityType.ARTIST, "artist-1")).thenReturn(10L);
        when(idResolutionService.resolveId(SpotifyEntityType.ALBUM, "alb-1")).thenReturn(50L);
        when(idResolutionService.resolveId(SpotifyEntityType.ARTIST, "collab-1")).thenReturn(20L);

        int count = processor.process(buildResponse("artist-1"), buildIteration());

        // 1 album + 1 ARTIST_ALBUM (queried artist) + 1 simplifiedArtist + 1 ARTIST_ALBUM (album artist) = 4
        assertThat(count).isEqualTo(4);
        verify(stagingWriter).insertSimplifiedArtist(42L, 100L, albumArtist, 20L);
        verify(stagingWriter, times(2)).insertEntityRelation(eq(42L), eq(100L),
            eq(SpotifyEntityType.ARTIST.getCode()), anyLong(),
            eq(SpotifyEntityType.ALBUM.getCode()), eq(50L),
            eq(SpotifyRelationType.ARTIST_ALBUM.getCode()));
    }

    @Test
    void getApiCallType_shouldReturnArtistAlbums() {
        assertThat(processor.getApiCallType().name()).isEqualTo("ARTIST_ALBUMS");
    }
}
