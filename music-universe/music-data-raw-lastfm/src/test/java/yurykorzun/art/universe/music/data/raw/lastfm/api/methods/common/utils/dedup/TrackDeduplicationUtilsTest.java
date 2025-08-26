package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.utils.dedup;

import org.junit.jupiter.api.Test;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.album.getinfo.dto.AlbumGetInfoTrackArtistDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.album.getinfo.dto.AlbumGetInfoTrackDto;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TrackDeduplicationUtilsTest {

    @Test
    void deduplicateTrackDtos_shouldRemoveDuplicatesByArtistNameAndName() {
        // Given
        AlbumGetInfoTrackDto track1 = createTrackDto("Gigi D'Agostino", "Please Don't Cry", "url1", "mbid1");
        AlbumGetInfoTrackDto track2 = createTrackDto("Gigi D'Agostino", "Please Don't Cry", "url2", null); // duplicate with less data
        AlbumGetInfoTrackDto track3 = createTrackDto("Gigi D'Agostino", "Different Track", "url3", "mbid3");
        AlbumGetInfoTrackDto track4 = createTrackDto("Different Artist", "Please Don't Cry", "url4", "mbid4");

        List<AlbumGetInfoTrackDto> tracks = Arrays.asList(track1, track2, track3, track4);

        // When
        List<AlbumGetInfoTrackDto> result = TrackDeduplicationUtils.deduplicateTrackDtos(tracks);

        // Then
        assertEquals(3, result.size(), "Should have 3 unique tracks after deduplication");

        // Should keep track1 (more complete) instead of track2
        assertTrue(result.contains(track1), "Should keep track with MBID");
        assertFalse(result.contains(track2), "Should remove duplicate with less data");

        // Should keep other unique tracks
        assertTrue(result.contains(track3), "Should keep unique track from same artist");
        assertTrue(result.contains(track4), "Should keep track with same name but different artist");
    }

    @Test
    void deduplicateTrackDtos_shouldSelectTrackWithMoreCompleteData() {
        // Given
        AlbumGetInfoTrackDto trackWithoutMbid = createTrackDto("Artist", "Track", "url1", null);
        AlbumGetInfoTrackDto trackWithMbid = createTrackDto("Artist", "Track", "url2", "mbid123");

        List<AlbumGetInfoTrackDto> tracks = Arrays.asList(trackWithoutMbid, trackWithMbid);

        // When
        List<AlbumGetInfoTrackDto> result = TrackDeduplicationUtils.deduplicateTrackDtos(tracks);

        // Then
        assertEquals(1, result.size());
        assertEquals("mbid123", result.get(0).getMbid(), "Should keep track with MBID");
    }

    @Test
    void deduplicateTrackDtos_shouldSkipTracksWithoutNameOrArtist() {
        // Given
        AlbumGetInfoTrackDto validTrack = createTrackDto("Artist", "Track", "url1", "mbid1");
        AlbumGetInfoTrackDto trackWithoutName = createTrackDto("Artist", null, "url2", "mbid2");
        AlbumGetInfoTrackDto trackWithoutArtist = createTrackDto(null, "Track", "url3", "mbid3");

        List<AlbumGetInfoTrackDto> tracks = Arrays.asList(validTrack, trackWithoutName, trackWithoutArtist);

        // When
        List<AlbumGetInfoTrackDto> result = TrackDeduplicationUtils.deduplicateTrackDtos(tracks);

        // Then
        assertEquals(1, result.size());
        assertEquals(validTrack, result.get(0), "Should only keep valid track");
    }

    private AlbumGetInfoTrackDto createTrackDto(String artistName, String trackName, String url, String mbid) {
        AlbumGetInfoTrackDto track = new AlbumGetInfoTrackDto();
        track.setName(trackName);
        track.setUrl(url);
        track.setMbid(mbid);

        if (artistName != null) {
            AlbumGetInfoTrackArtistDto artist = new AlbumGetInfoTrackArtistDto();
            artist.setName(artistName);
            track.setArtist(artist);
        }

        return track;
    }
}
