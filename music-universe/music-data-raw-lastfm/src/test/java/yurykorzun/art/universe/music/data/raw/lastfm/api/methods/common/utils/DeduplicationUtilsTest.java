package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.utils;

import lombok.Getter;
import org.junit.jupiter.api.Test;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.album.getinfo.dto.AlbumGetInfoTrackArtistDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.album.getinfo.dto.AlbumGetInfoTrackDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.common.dto.ArtistDto;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DeduplicationUtilsTest {

    // Test DTO class
    @Getter
    static class TestArtistDto extends ArtistDto {

        public TestArtistDto(String name, String mbid, String url) {
            super();
            this.setName(name);
            this.setMbid(mbid);
            this.setUrl(url);
        }
    }

    @Test
    void deduplicateArtistDtos_shouldKeepMostCompleteArtist_whenDuplicateNames() {
        // Given
        List<TestArtistDto> artists = List.of(
            new TestArtistDto("Paramore", null, "https://www.last.fm/music/Paramore"),
            new TestArtistDto("Paramore", "44cf61b8-5197-448a-b82b-cef6ee89fac5", "https://www.last.fm/music/Paramore"),
            new TestArtistDto("Snow Patrol", "a66999a7-ae5c-460e-ba94-1a01143ae847", null),
            new TestArtistDto("Snow Patrol", null, "https://www.last.fm/music/Snow+Patrol")
        );

        // When
        List<TestArtistDto> result = DeduplicationUtils.deduplicateArtistDtos(artists);

        // Then
        assertEquals(2, result.size(), "Should have 2 unique artists");

        // Find Paramore in result
        TestArtistDto paramore = result.stream()
            .filter(artist -> "Paramore".equals(artist.getName()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Paramore not found in result"));

        // Should keep the version with MBID (more complete)
        assertEquals("44cf61b8-5197-448a-b82b-cef6ee89fac5", paramore.getMbid(),
            "Should keep Paramore version with MBID");
        assertEquals("https://www.last.fm/music/Paramore", paramore.getUrl(),
            "Should keep URL from the more complete version");

        // Find Snow Patrol in result
        TestArtistDto snowPatrol = result.stream()
            .filter(artist -> "Snow Patrol".equals(artist.getName()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Snow Patrol not found in result"));

        // Should keep the version with MBID (higher priority than URL)
        assertEquals("a66999a7-ae5c-460e-ba94-1a01143ae847", snowPatrol.getMbid(),
            "Should keep Snow Patrol version with MBID");
    }

    @Test
    void deduplicateArtistDtos_shouldPreserveOrder_whenNoDeduplicationNeeded() {
        // Given
        List<TestArtistDto> artists = List.of(
            new TestArtistDto("Artist1", "mbid1", "url1"),
            new TestArtistDto("Artist2", "mbid2", "url2"),
            new TestArtistDto("Artist3", "mbid3", "url3")
        );

        // When
        List<TestArtistDto> result = DeduplicationUtils.deduplicateArtistDtos(artists);

        // Then
        assertEquals(3, result.size(), "Should preserve all unique artists");
        assertEquals("Artist1", result.get(0).getName(), "Should preserve order");
        assertEquals("Artist2", result.get(1).getName(), "Should preserve order");
        assertEquals("Artist3", result.get(2).getName(), "Should preserve order");
    }

    @Test
    void deduplicateArtistDtos_shouldSkipNullNames() {
        // Given
        List<TestArtistDto> artists = List.of(
            new TestArtistDto(null, "mbid1", "url1"),
            new TestArtistDto("", "mbid2", "url2"),
            new TestArtistDto("  ", "mbid3", "url3"),
            new TestArtistDto("ValidArtist", "mbid4", "url4")
        );

        // When
        List<TestArtistDto> result = DeduplicationUtils.deduplicateArtistDtos(artists);

        // Then
        assertEquals(1, result.size(), "Should only keep artist with valid name");
        assertEquals("ValidArtist", result.get(0).getName(), "Should keep valid artist");
    }

    @Test
    void deduplicateArtistDtos_shouldHandleEmptyCollection() {
        // Given
        List<TestArtistDto> artists = List.of();

        // When
        List<TestArtistDto> result = DeduplicationUtils.deduplicateArtistDtos(artists);

        // Then
        assertTrue(result.isEmpty(), "Should return empty list for empty input");
    }

    @Test
    void deduplicateArtistDtos_shouldHandleNullCollection() {
        // When
        List<TestArtistDto> result = DeduplicationUtils.deduplicateArtistDtos(null);

        // Then
        assertTrue(result.isEmpty(), "Should return empty list for null input");
    }

    @Test
    void deduplicateArtistDtos_shouldPrioritizeDataCompleteness() {
        // Given - test different completeness scenarios
        List<TestArtistDto> artists = List.of(
            // Only name (score: 100)
            new TestArtistDto("TestArtist", null, null),
            // Name + URL (score: 101)
            new TestArtistDto("TestArtist", null, "http://example.com"),
            // Name + MBID (score: 110) - should win
            new TestArtistDto("TestArtist", "test-mbid", null),
            // Name + MBID + URL (score: 111) - should win over previous
            new TestArtistDto("TestArtist", "test-mbid-2", "http://example2.com")
        );

        // When
        List<TestArtistDto> result = DeduplicationUtils.deduplicateArtistDtos(artists);

        // Then
        assertEquals(1, result.size(), "Should have 1 deduplicated artist");
        TestArtistDto winner = result.get(0);
        assertEquals("TestArtist", winner.getName());
        assertEquals("test-mbid-2", winner.getMbid(), "Should keep the most complete version");
        assertEquals("http://example2.com", winner.getUrl(), "Should keep the most complete version");
    }

    @Test
    void deduplicateTrackDtos_shouldRemoveDuplicatesByArtistNameAndName() {
        // Given
        AlbumGetInfoTrackDto track1 = createTrackDto("Gigi D'Agostino", "Please Don't Cry", "url1", "mbid1");
        AlbumGetInfoTrackDto track2 = createTrackDto("Gigi D'Agostino", "Please Don't Cry", "url2", null); // duplicate with less data
        AlbumGetInfoTrackDto track3 = createTrackDto("Gigi D'Agostino", "Different Track", "url3", "mbid3");
        AlbumGetInfoTrackDto track4 = createTrackDto("Different Artist", "Please Don't Cry", "url4", "mbid4");

        List<AlbumGetInfoTrackDto> tracks = Arrays.asList(track1, track2, track3, track4);

        // When
        List<AlbumGetInfoTrackDto> result = DeduplicationUtils.deduplicateTrackDtos(tracks);

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
        List<AlbumGetInfoTrackDto> result = DeduplicationUtils.deduplicateTrackDtos(tracks);

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
        List<AlbumGetInfoTrackDto> result = DeduplicationUtils.deduplicateTrackDtos(tracks);

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
