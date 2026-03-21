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
class SpotifyAlbumGetResponseProcessorTest {

    @Mock private ObjectMapper objectMapper;
    @Mock private StagingWriter stagingWriter;
    @Mock private SyntheticIdResolutionService idResolutionService;

    @InjectMocks
    private SpotifyAlbumGetResponseProcessor processor;

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

    private SpotifyAlbumDto albumWithNoArtistsNoTracks(String id) {
        return new SpotifyAlbumDto(id, "Test Album", "uri:album", "album", 10,
            "2020-01-01", "day", null, Map.of("spotify", "https://open.spotify.com/album/" + id), null);
    }

    private SpotifySimplifiedArtistDto simplifiedArtist(String id, String name) {
        return new SpotifySimplifiedArtistDto(id, name, "uri:" + id, Map.of());
    }

    @Test
    void process_shouldStageAlbumOnly_whenNoArtistsNoTracks() throws IOException {
        SpotifyAlbumDto dto = albumWithNoArtistsNoTracks("album-1");
        when(objectMapper.readValue(anyString(), eq(SpotifyAlbumDto.class))).thenReturn(dto);
        when(idResolutionService.resolveId(SpotifyEntityType.ALBUM, "album-1")).thenReturn(50L);

        int count = processor.process(buildResponse(), buildIteration());

        assertThat(count).isEqualTo(1);
        verify(stagingWriter).insertAlbum(42L, 100L, dto, 50L, null, null);
        verifyNoMoreInteractions(stagingWriter);
    }

    @Test
    void process_shouldStageAlbumAndArtists_whenArtistsPresent() throws IOException {
        SpotifySimplifiedArtistDto artist = simplifiedArtist("artist-1", "Drake");
        SpotifyAlbumDto dto = new SpotifyAlbumDto("album-1", "Album", "uri", "album", 10,
            "2020", "year", List.of(artist), Map.of(), null);

        when(objectMapper.readValue(anyString(), eq(SpotifyAlbumDto.class))).thenReturn(dto);
        when(idResolutionService.resolveId(SpotifyEntityType.ALBUM, "album-1")).thenReturn(50L);
        when(idResolutionService.resolveId(SpotifyEntityType.ARTIST, "artist-1")).thenReturn(10L);

        int count = processor.process(buildResponse(), buildIteration());

        // 1 album + (1 artist + 1 relation) = 3
        assertThat(count).isEqualTo(3);
        verify(stagingWriter).insertAlbum(eq(42L), eq(100L), eq(dto), eq(50L), eq(10L), eq("artist-1"));
        verify(stagingWriter).insertSimplifiedArtist(42L, 100L, artist, 10L);
        verify(stagingWriter).insertEntityRelation(42L, 100L,
            SpotifyEntityType.ARTIST.getCode(), 10L,
            SpotifyEntityType.ALBUM.getCode(), 50L,
            SpotifyRelationType.ARTIST_ALBUM.getCode());
    }

    @Test
    void process_shouldStageEmbeddedTracks_whenTracksPresent() throws IOException {
        SpotifyTrackDto track = new SpotifyTrackDto("track-1", "Song", "uri", 200000, 1, 1,
            false, true, null, null, Map.of(), null);
        SpotifyPagingDto<SpotifyTrackDto> trackPaging = new SpotifyPagingDto<>(
            List.of(track), 1, 50, 0, null, null);
        SpotifyAlbumDto dto = new SpotifyAlbumDto("album-1", "Album", "uri", "album", 1,
            "2020", "year", null, Map.of(), trackPaging);

        when(objectMapper.readValue(anyString(), eq(SpotifyAlbumDto.class))).thenReturn(dto);
        when(idResolutionService.resolveId(SpotifyEntityType.ALBUM, "album-1")).thenReturn(50L);
        when(idResolutionService.resolveId(SpotifyEntityType.TRACK, "track-1")).thenReturn(60L);

        int count = processor.process(buildResponse(), buildIteration());

        // 1 album + 1 track + 1 relation = 3
        assertThat(count).isEqualTo(3);
        verify(stagingWriter).insertTrack(eq(42L), eq(100L), eq(track),
            eq(60L), isNull(), isNull(), eq(50L), eq("album-1"));
        verify(stagingWriter).insertEntityRelation(42L, 100L,
            SpotifyEntityType.ALBUM.getCode(), 50L,
            SpotifyEntityType.TRACK.getCode(), 60L,
            SpotifyRelationType.ALBUM_TRACK.getCode());
    }

    @Test
    void process_shouldStageFeaturedTrackArtistRelations() throws IOException {
        SpotifySimplifiedArtistDto primaryArtist = simplifiedArtist("primary-1", "Drake");
        SpotifySimplifiedArtistDto featuredArtist = simplifiedArtist("featured-1", "Future");
        SpotifyTrackDto track = new SpotifyTrackDto("track-1", "Song", "uri", 200000, 1, 1,
            false, true, List.of(primaryArtist, featuredArtist), null, Map.of(), null);
        SpotifyPagingDto<SpotifyTrackDto> trackPaging = new SpotifyPagingDto<>(
            List.of(track), 1, 50, 0, null, null);
        SpotifyAlbumDto dto = new SpotifyAlbumDto("album-1", "Album", "uri", "album", 1,
            "2020", "year", null, Map.of(), trackPaging);

        when(objectMapper.readValue(anyString(), eq(SpotifyAlbumDto.class))).thenReturn(dto);
        when(idResolutionService.resolveId(SpotifyEntityType.ALBUM, "album-1")).thenReturn(50L);
        when(idResolutionService.resolveId(SpotifyEntityType.TRACK, "track-1")).thenReturn(60L);
        when(idResolutionService.resolveId(SpotifyEntityType.ARTIST, "primary-1")).thenReturn(10L);
        when(idResolutionService.resolveId(SpotifyEntityType.ARTIST, "featured-1")).thenReturn(11L);

        int count = processor.process(buildResponse(), buildIteration());

        // 1 album + 1 track + 1 album_track relation + 1 track_artist relation = 4
        assertThat(count).isEqualTo(4);
        verify(stagingWriter).insertEntityRelation(42L, 100L,
            SpotifyEntityType.TRACK.getCode(), 60L,
            SpotifyEntityType.ARTIST.getCode(), 11L,
            SpotifyRelationType.TRACK_ARTIST.getCode());
    }

    @Test
    void getApiCallType_shouldReturnAlbumGet() {
        assertThat(processor.getApiCallType().name()).isEqualTo("ALBUM_GET");
    }
}
