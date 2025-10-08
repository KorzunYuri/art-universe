package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.utils.dedup;

import org.junit.jupiter.api.Test;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.track.common.dto.TrackDto;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TrackDeduplicationUtilsTest {

    @Test
    void deduplicateTrackDtos_shouldReturnEmptyList_whenInputIsNull() {
        List<TestTrackDto> result = TrackDeduplicationUtils.deduplicateTrackDtos(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void deduplicateTrackDtos_shouldReturnEmptyList_whenInputIsEmpty() {
        List<TestTrackDto> result = TrackDeduplicationUtils.deduplicateTrackDtos(Collections.emptyList());
        assertTrue(result.isEmpty());
    }

    @Test
    void deduplicateTrackDtos_shouldReturnSameList_whenNoDuplicates() {
        List<TestTrackDto> tracks = Arrays.asList(
            new TestTrackDto("Artist1", "Track1", "url1", "mbid1"),
            new TestTrackDto("Artist2", "Track2", "url2", "mbid2")
        );

        List<TestTrackDto> result = TrackDeduplicationUtils.deduplicateTrackDtos(tracks);

        assertEquals(2, result.size());
        assertEquals("Track1", result.get(0).getName());
        assertEquals("Track2", result.get(1).getName());
    }

    @Test
    void deduplicateTrackDtos_shouldDeduplicateByArtistAndName() {
        List<TestTrackDto> tracks = Arrays.asList(
            new TestTrackDto("Artist1", "Track1", "url1", "mbid1"),
            new TestTrackDto("Artist1", "Track1", "url2", "mbid2") // Same artist+name, different URL
        );

        List<TestTrackDto> result = TrackDeduplicationUtils.deduplicateTrackDtos(tracks);

        assertEquals(1, result.size());
        assertEquals("Track1", result.get(0).getName());
        assertEquals("Artist1", result.get(0).getArtistName());
    }

    @Test
    void deduplicateTrackDtos_shouldDeduplicateByUrl() {
        List<TestTrackDto> tracks = Arrays.asList(
            new TestTrackDto("Artist1", "Track1", "url1", "mbid1"),
            new TestTrackDto("Artist2", "Track2", "url1", "mbid2") // Different artist+name, same URL
        );

        List<TestTrackDto> result = TrackDeduplicationUtils.deduplicateTrackDtos(tracks);

        assertEquals(1, result.size());
        assertEquals("url1", result.get(0).getUrl());
    }

    @Test
    void deduplicateTrackDtos_shouldSelectBetterTrack_whenDuplicatesFound() {
        List<TestTrackDto> tracks = Arrays.asList(
            new TestTrackDto("Artist1", "Track1", "url1", null), // No MBID
            new TestTrackDto("Artist1", "Track1", "url2", "mbid1") // Has MBID - better
        );

        List<TestTrackDto> result = TrackDeduplicationUtils.deduplicateTrackDtos(tracks);

        assertEquals(1, result.size());
        assertEquals("mbid1", result.get(0).getMbid());
    }

    @Test
    void deduplicateTrackDtos_shouldSkipTracksWithNullOrEmptyNames() {
        List<TestTrackDto> tracks = Arrays.asList(
            new TestTrackDto("Artist1", null, "url1", "mbid1"),
            new TestTrackDto("Artist2", "", "url2", "mbid2"),
            new TestTrackDto(null, "Track3", "url3", "mbid3"),
            new TestTrackDto("", "Track4", "url4", "mbid4"),
            new TestTrackDto("Artist5", "Track5", "url5", "mbid5") // Valid
        );

        List<TestTrackDto> result = TrackDeduplicationUtils.deduplicateTrackDtos(tracks);

        assertEquals(1, result.size());
        assertEquals("Track5", result.get(0).getName());
        assertEquals("Artist5", result.get(0).getArtistName());
    }

    @Test
    void deduplicateTrackDtos_shouldSkipTracksWithNullOrEmptyUrls() {
        List<TestTrackDto> tracks = Arrays.asList(
            new TestTrackDto("Artist1", "Track1", null, "mbid1"),
            new TestTrackDto("Artist2", "Track2", "", "mbid2"),
            new TestTrackDto("Artist3", "Track3", "url3", "mbid3") // Valid
        );

        List<TestTrackDto> result = TrackDeduplicationUtils.deduplicateTrackDtos(tracks);

        assertEquals(1, result.size());
        assertEquals("Track3", result.get(0).getName());
        assertEquals("url3", result.get(0).getUrl());
    }

    @Test
    void deduplicateTrackDtos_shouldHandleBothDeduplicationPasses() {
        List<TestTrackDto> tracks = Arrays.asList(
            new TestTrackDto("Artist1", "Track1", "url1", "mbid1"),
            new TestTrackDto("Artist1", "Track1", "url2", "mbid2"), // Duplicate by artist+name
            new TestTrackDto("Artist2", "Track2", "url1", "mbid3"), // Duplicate by URL with first track
            new TestTrackDto("Artist3", "Track3", "url3", "mbid4")  // Unique
        );

        List<TestTrackDto> result = TrackDeduplicationUtils.deduplicateTrackDtos(tracks);

        assertEquals(2, result.size());
        // Should have one track with url1 and one with url3
        assertTrue(result.stream().anyMatch(t -> "url1".equals(t.getUrl())));
        assertTrue(result.stream().anyMatch(t -> "url3".equals(t.getUrl())));
    }

    // Test DTO implementation
    private static class TestTrackDto extends TrackDto {
        private final String artistName;

        public TestTrackDto(String artistName, String name, String url, String mbid) {
            this.artistName = artistName;
            setName(name);
            setUrl(url);
            setMbid(mbid);
        }

        @Override
        public String getArtistName() {
            return artistName;
        }
    }
}
