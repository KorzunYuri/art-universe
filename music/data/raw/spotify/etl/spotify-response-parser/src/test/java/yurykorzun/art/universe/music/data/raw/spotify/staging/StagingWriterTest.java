package yurykorzun.art.universe.music.data.raw.spotify.staging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import yurykorzun.art.universe.music.data.raw.spotify.enums.SpotifyEntityType;
import yurykorzun.art.universe.music.data.raw.spotify.enums.SpotifyRelationType;
import yurykorzun.art.universe.music.data.raw.spotify.integration.dto.SpotifyAlbumDto;
import yurykorzun.art.universe.music.data.raw.spotify.integration.dto.SpotifyArtistDto;
import yurykorzun.art.universe.music.data.raw.spotify.integration.dto.SpotifySimplifiedArtistDto;
import yurykorzun.art.universe.music.data.raw.spotify.integration.dto.SpotifyTrackDto;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StagingWriterTest {

    @Mock
    private JdbcTemplate jdbc;

    @InjectMocks
    private StagingWriter writer;

    @Test
    void createTablesForIteration_shouldExecuteFiveDdlStatements() {
        writer.createTablesForIteration(10L);

        verify(jdbc, times(5)).execute(anyString());
    }

    @Test
    void createTablesForIteration_shouldUseCorrectIterationIdInTableNames() {
        writer.createTablesForIteration(42L);

        verify(jdbc).execute(contains("stg_artist_42"));
        verify(jdbc).execute(contains("stg_genre_42"));
        verify(jdbc).execute(contains("stg_album_42"));
        verify(jdbc).execute(contains("stg_track_42"));
        verify(jdbc).execute(contains("stg_entity_relation_42"));
    }

    @Test
    void insertGenre_shouldCallUpdateWithCorrectTableAndParams() {
        writer.insertGenre(1L, 100L, "hip-hop", 999L);

        verify(jdbc).update(
            contains("stg_genre_1"),
            eq(100L), eq(999L), eq("hip-hop"), eq("hip-hop")
        );
    }

    @Test
    void insertArtist_shouldCallUpdateWithCorrectTableAndParams() {
        SpotifyArtistDto dto = new SpotifyArtistDto(
            "artist-id", "Drake", "spotify:artist:abc", "artist", null,
            Map.of("spotify", "https://open.spotify.com/artist/abc")
        );

        writer.insertArtist(2L, 200L, dto, 55L);

        verify(jdbc).update(
            contains("stg_artist_2"),
            eq(200L), eq(55L), eq("artist-id"), eq("Drake"),
            eq("https://open.spotify.com/artist/abc"), eq("spotify:artist:abc")
        );
    }

    @Test
    void insertAlbum_shouldCallUpdateWithCorrectTable() {
        SpotifyAlbumDto dto = buildAlbumDto("album", "day");

        writer.insertAlbum(3L, 300L, dto, 77L, 1L, "artist-spotify-id");

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbc).update(sqlCaptor.capture(), any(Object[].class));
        assertThat(sqlCaptor.getValue()).contains("stg_album_3");
    }

    @Test
    void insertAlbum_shouldNotThrow_forAllAlbumTypesAndDatePrecisions() {
        // Covers resolveAlbumTypeCode: album=1, single=2, compilation=3, ep=4, null=null, unknown=null
        // Covers resolveDatePrecisionCode: year=1, month=2, day=3, null=null, unknown=null
        for (String albumType : new String[]{"album", "single", "compilation", "ep", null, "unknown"}) {
            writer.insertAlbum(1L, 1L, buildAlbumDto(albumType, null), 1L, null, null);
        }
        for (String datePrecision : new String[]{"year", "month", "day", null, "unknown"}) {
            writer.insertAlbum(1L, 1L, buildAlbumDto(null, datePrecision), 1L, null, null);
        }

        verify(jdbc, times(11)).update(anyString(), any(Object[].class));
    }

    @Test
    void insertEntityRelation_shouldCallUpdateWithCorrectTableAndParams() {
        writer.insertEntityRelation(5L, 500L,
            SpotifyEntityType.ARTIST.getCode(), 10L,
            SpotifyEntityType.GENRE.getCode(), 20L,
            SpotifyRelationType.ARTIST_GENRE.getCode()
        );

        verify(jdbc).update(
            contains("stg_entity_relation_5"),
            eq(500L),
            eq(SpotifyEntityType.ARTIST.getCode()), eq(10L),
            eq(SpotifyEntityType.GENRE.getCode()), eq(20L),
            eq(SpotifyRelationType.ARTIST_GENRE.getCode())
        );
    }

    @Test
    void insertSimplifiedArtist_shouldCallUpdateWithCorrectTableAndParams() {
        SpotifySimplifiedArtistDto dto = new SpotifySimplifiedArtistDto(
            "simplified-id", "The Weeknd", "spotify:artist:xyz",
            Map.of("spotify", "https://open.spotify.com/artist/xyz")
        );

        writer.insertSimplifiedArtist(4L, 400L, dto, 66L);

        verify(jdbc).update(
            contains("stg_artist_4"),
            eq(400L), eq(66L), eq("simplified-id"), eq("The Weeknd"),
            eq("https://open.spotify.com/artist/xyz"), eq("spotify:artist:xyz")
        );
    }

    @Test
    void insertTrack_shouldCallUpdateWithCorrectTableAndParams() {
        SpotifyTrackDto dto = new SpotifyTrackDto(
            "track-id", "Blinding Lights", "spotify:track:abc",
            210000, 3, 1, false, true,
            null, null,
            Map.of("spotify", "https://open.spotify.com/track/abc"),
            Map.of("isrc", "USRC12345678", "ean", "ean-value", "upc", "upc-value")
        );

        writer.insertTrack(6L, 600L, dto, 88L, 11L, "artist-spotify-id", 22L, "album-spotify-id");

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbc).update(sqlCaptor.capture(), any(Object[].class));
        assertThat(sqlCaptor.getValue()).contains("stg_track_6");
    }

    @Test
    void insertTrack_shouldPassAllFieldsCorrectly() {
        SpotifyTrackDto dto = new SpotifyTrackDto(
            "track-id", "Track Name", "spotify:track:t1",
            180000, 5, 2, true, false,
            null, null,
            Map.of("spotify", "https://open.spotify.com/track/t1"),
            Map.of("isrc", "US123")
        );

        writer.insertTrack(7L, 700L, dto, 99L, 33L, "artist-sid", 44L, "album-sid");

        verify(jdbc).update(
            anyString(),
            eq(700L), eq(99L), eq("track-id"), eq("Track Name"), eq(180000),
            eq(5), eq(2), eq(true), eq(false),
            eq("https://open.spotify.com/track/t1"), eq("spotify:track:t1"),
            eq("US123"), isNull(), isNull(),
            eq(33L), eq("artist-sid"), eq(44L), eq("album-sid")
        );
    }

    private SpotifyAlbumDto buildAlbumDto(String albumType, String datePrecision) {
        return new SpotifyAlbumDto(
            "album-spotify-id", "Test Album", "spotify:album:abc",
            albumType, 10, "2020-01-01", datePrecision,
            null,
            Map.of("spotify", "https://open.spotify.com/album/abc"),
            null
        );
    }
}
