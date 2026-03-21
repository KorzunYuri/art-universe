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
import yurykorzun.art.universe.music.data.raw.spotify.integration.dto.SpotifyAlbumDto;
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
class SpotifyTrackGetResponseProcessorTest {

    @Mock private ObjectMapper objectMapper;
    @Mock private StagingWriter stagingWriter;
    @Mock private SyntheticIdResolutionService idResolutionService;

    @InjectMocks
    private SpotifyTrackGetResponseProcessor processor;

    private SpotifyApiResponse buildResponse() {
        SpotifyApiResponse response = mock(SpotifyApiResponse.class);
        when(response.getId()).thenReturn(100L);
        when(response.getResponseBody()).thenReturn("{}");
        return response;
    }

    private StagingIteration buildIteration() {
        StagingIteration iteration = mock(StagingIteration.class);
        lenient().when(iteration.getId()).thenReturn(42L);
        return iteration;
    }

    @Test
    void process_shouldStageTrackOnly_whenNoAlbumNoArtists() throws IOException {
        SpotifyTrackDto dto = new SpotifyTrackDto("track-1", "Song", "uri", 200000, 1, 1,
            false, true, null, null, Map.of(), null);

        when(objectMapper.readValue(anyString(), eq(SpotifyTrackDto.class))).thenReturn(dto);
        when(idResolutionService.resolveId(SpotifyEntityType.TRACK, "track-1")).thenReturn(60L);

        int count = processor.process(buildResponse(), buildIteration());

        assertThat(count).isEqualTo(1);
        verify(stagingWriter).insertTrack(42L, 100L, dto, 60L, null, null, null, null);
        verifyNoMoreInteractions(stagingWriter);
    }

    @Test
    void process_shouldStageTrackAndEmbeddedAlbum_whenAlbumPresent() throws IOException {
        SpotifyAlbumDto album = new SpotifyAlbumDto("alb-1", "Album", "uri", "album", 10,
            "2020", "year", null, Map.of(), null);
        SpotifyTrackDto dto = new SpotifyTrackDto("track-1", "Song", "uri", 200000, 1, 1,
            false, true, null, album, Map.of(), null);

        when(objectMapper.readValue(anyString(), eq(SpotifyTrackDto.class))).thenReturn(dto);
        when(idResolutionService.resolveId(SpotifyEntityType.TRACK, "track-1")).thenReturn(60L);
        when(idResolutionService.resolveId(SpotifyEntityType.ALBUM, "alb-1")).thenReturn(50L);

        int count = processor.process(buildResponse(), buildIteration());

        // 1 track + 1 album + 1 album_track relation = 3
        assertThat(count).isEqualTo(3);
        verify(stagingWriter).insertTrack(42L, 100L, dto, 60L, null, null, 50L, "alb-1");
        verify(stagingWriter).insertAlbum(42L, 100L, album, 50L, null, null);
        verify(stagingWriter).insertEntityRelation(42L, 100L,
            SpotifyEntityType.ALBUM.getCode(), 50L,
            SpotifyEntityType.TRACK.getCode(), 60L,
            SpotifyRelationType.ALBUM_TRACK.getCode());
    }

    @Test
    void process_shouldStageFeaturedArtistRelations() throws IOException {
        SpotifySimplifiedArtistDto primary = new SpotifySimplifiedArtistDto("a-1", "Drake", "uri", Map.of());
        SpotifySimplifiedArtistDto featured = new SpotifySimplifiedArtistDto("a-2", "Future", "uri", Map.of());
        SpotifyTrackDto dto = new SpotifyTrackDto("track-1", "Song", "uri", 200000, 1, 1,
            false, true, List.of(primary, featured), null, Map.of(), null);

        when(objectMapper.readValue(anyString(), eq(SpotifyTrackDto.class))).thenReturn(dto);
        when(idResolutionService.resolveId(SpotifyEntityType.TRACK, "track-1")).thenReturn(60L);
        when(idResolutionService.resolveId(SpotifyEntityType.ARTIST, "a-1")).thenReturn(10L);
        when(idResolutionService.resolveId(SpotifyEntityType.ARTIST, "a-2")).thenReturn(11L);

        int count = processor.process(buildResponse(), buildIteration());

        // 1 track + 1 track_artist relation = 2
        assertThat(count).isEqualTo(2);
        verify(stagingWriter).insertTrack(42L, 100L, dto, 60L, 10L, "a-1", null, null);
        verify(stagingWriter).insertEntityRelation(42L, 100L,
            SpotifyEntityType.TRACK.getCode(), 60L,
            SpotifyEntityType.ARTIST.getCode(), 11L,
            SpotifyRelationType.TRACK_ARTIST.getCode());
    }

    @Test
    void getApiCallType_shouldReturnTrackGet() {
        assertThat(processor.getApiCallType().name()).isEqualTo("TRACK_GET");
    }
}
