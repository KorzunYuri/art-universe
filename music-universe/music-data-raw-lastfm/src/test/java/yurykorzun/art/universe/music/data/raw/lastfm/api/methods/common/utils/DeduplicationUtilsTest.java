package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.utils;

import lombok.Getter;
import org.junit.jupiter.api.Test;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.common.dto.ArtistDto;

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
}
