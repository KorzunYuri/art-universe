package yurykorzun.art.universe.music.data.raw.spotify.task.response.process.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.data.raw.spotify.enums.SpotifyEntityType;
import yurykorzun.art.universe.music.data.raw.spotify.enums.SpotifyRelationType;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiResponse;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.StagingIteration;
import yurykorzun.art.universe.music.data.raw.spotify.integration.dto.SpotifyArtistDto;
import yurykorzun.art.universe.music.data.raw.spotify.staging.StagingWriter;
import yurykorzun.art.universe.music.data.raw.spotify.staging.SyntheticIdResolutionService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpotifyArtistGetResponseProcessorTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private StagingWriter stagingWriter;

    @Mock
    private SyntheticIdResolutionService idResolutionService;

    @InjectMocks
    private SpotifyArtistGetResponseProcessor processor;

    private SpotifyApiResponse buildResponse(String body) {
        SpotifyApiResponse response = mock(SpotifyApiResponse.class);
        when(response.getId()).thenReturn(100L);
        when(response.getResponseBody()).thenReturn(body);
        return response;
    }

    private StagingIteration buildIteration() {
        StagingIteration iteration = mock(StagingIteration.class);
        lenient().when(iteration.getId()).thenReturn(42L);
        return iteration;
    }

    @Test
    void process_shouldStageArtistAndReturnCount1_whenNoGenres() throws IOException {
        SpotifyArtistDto dto = new SpotifyArtistDto("artist-id", "Drake", "spotify:artist:abc",
            "artist", null, Map.of("spotify", "https://open.spotify.com/artist/abc"));
        SpotifyApiResponse response = buildResponse("{}");
        StagingIteration iteration = buildIteration();

        when(objectMapper.readValue(anyString(), eq(SpotifyArtistDto.class))).thenReturn(dto);
        when(idResolutionService.resolveId(SpotifyEntityType.ARTIST, "artist-id")).thenReturn(55L);

        int count = processor.process(response, iteration);

        assertThat(count).isEqualTo(1);
        verify(stagingWriter).insertArtist(42L, 100L, dto, 55L);
        verifyNoMoreInteractions(stagingWriter);
    }

    @Test
    void process_shouldStageArtistGenresAndRelations_whenGenresPresent() throws IOException {
        SpotifyArtistDto dto = new SpotifyArtistDto("artist-id", "Drake", "spotify:artist:abc",
            "artist", List.of("hip-hop", "rap"), Map.of());
        SpotifyApiResponse response = buildResponse("{}");
        StagingIteration iteration = buildIteration();

        when(objectMapper.readValue(anyString(), eq(SpotifyArtistDto.class))).thenReturn(dto);
        when(idResolutionService.resolveId(SpotifyEntityType.ARTIST, "artist-id")).thenReturn(55L);
        when(idResolutionService.resolveId(SpotifyEntityType.GENRE, "hip-hop")).thenReturn(11L);
        when(idResolutionService.resolveId(SpotifyEntityType.GENRE, "rap")).thenReturn(12L);

        int count = processor.process(response, iteration);

        // 1 artist + 2 genres * 2 (genre + relation) = 5
        assertThat(count).isEqualTo(5);
        verify(stagingWriter).insertArtist(42L, 100L, dto, 55L);
        verify(stagingWriter).insertGenre(42L, 100L, "hip-hop", 11L);
        verify(stagingWriter).insertGenre(42L, 100L, "rap", 12L);
        verify(stagingWriter).insertEntityRelation(42L, 100L,
            SpotifyEntityType.ARTIST.getCode(), 55L,
            SpotifyEntityType.GENRE.getCode(), 11L,
            SpotifyRelationType.ARTIST_GENRE.getCode());
        verify(stagingWriter).insertEntityRelation(42L, 100L,
            SpotifyEntityType.ARTIST.getCode(), 55L,
            SpotifyEntityType.GENRE.getCode(), 12L,
            SpotifyRelationType.ARTIST_GENRE.getCode());
    }

    @Test
    void getApiCallType_shouldReturnArtistGet() {
        assertThat(processor.getApiCallType().name()).isEqualTo("ARTIST_GET");
    }
}
