package yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.utils;

import org.junit.jupiter.api.Test;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.utils.DataQualityUtil;

import static org.junit.jupiter.api.Assertions.*;

class DataQualityUtilTest {

    @Test
    void normalizeTrackUrl_shouldReplaceAlbumWithUnderscore() {
        // Given
        String originalUrl = "https://www.last.fm/music/Radiohead/OK+Computer/Paranoid+Android";

        // When
        String normalizedUrl = DataQualityUtil.normalizeTrackUrl(originalUrl);

        // Then
        assertEquals("https://www.last.fm/music/Radiohead/_/Paranoid+Android", normalizedUrl);
    }

    @Test
    void normalizeTrackUrl_shouldHandleAlreadyNormalizedUrl() {
        // Given
        String alreadyNormalizedUrl = "https://www.last.fm/music/Radiohead/_/Paranoid+Android";

        // When
        String normalizedUrl = DataQualityUtil.normalizeTrackUrl(alreadyNormalizedUrl);

        // Then
        assertEquals("https://www.last.fm/music/Radiohead/_/Paranoid+Android", normalizedUrl);
    }

    @Test
    void normalizeTrackUrl_shouldHandleUrlWithSpecialCharacters() {
        // Given
        String urlWithSpecialChars = "https://www.last.fm/music/Beyonc%C3%A9/Lemonade/Formation";

        // When
        String normalizedUrl = DataQualityUtil.normalizeTrackUrl(urlWithSpecialChars);

        // Then
        assertEquals("https://www.last.fm/music/Beyonc%C3%A9/_/Formation", normalizedUrl);
    }

    @Test
    void normalizeTrackUrl_shouldHandleUrlWithPlusEncoding() {
        // Given
        String urlWithPlus = "https://www.last.fm/music/The+Beatles/Abbey+Road/Come+Together";

        // When
        String normalizedUrl = DataQualityUtil.normalizeTrackUrl(urlWithPlus);

        // Then
        assertEquals("https://www.last.fm/music/The+Beatles/_/Come+Together", normalizedUrl);
    }

    @Test
    void normalizeTrackUrl_shouldHandleComplexAlbumNames() {
        // Given
        String complexAlbumUrl = "https://www.last.fm/music/Pink+Floyd/The+Dark+Side+of+the+Moon+(Remastered)/Money";

        // When
        String normalizedUrl = DataQualityUtil.normalizeTrackUrl(complexAlbumUrl);

        // Then
        assertEquals("https://www.last.fm/music/Pink+Floyd/_/Money", normalizedUrl);
    }

    @Test
    void normalizeTrackUrl_shouldHandleUrlWithMultipleSlashes() {
        // Given
        String urlWithMultipleSlashes = "https://www.last.fm/music/Artist/Album/Track/Extra/Parts";

        // When
        String normalizedUrl = DataQualityUtil.normalizeTrackUrl(urlWithMultipleSlashes);

        // Then
        assertEquals("https://www.last.fm/music/Artist/_/Track/Extra/Parts", normalizedUrl);
    }

    @Test
    void normalizeTrackUrl_shouldHandleNonLastfmUrl() {
        // Given
        String nonLastfmUrl = "https://example.com/some/path";

        // When
        String normalizedUrl = DataQualityUtil.normalizeTrackUrl(nonLastfmUrl);

        // Then
        assertEquals("https://example.com/some/path", normalizedUrl);
    }

    @Test
    void normalizeTrackUrl_shouldHandleUrlWithoutMusicPath() {
        // Given
        String urlWithoutMusic = "https://www.last.fm/user/someuser";

        // When
        String normalizedUrl = DataQualityUtil.normalizeTrackUrl(urlWithoutMusic);

        // Then
        assertEquals("https://www.last.fm/user/someuser", normalizedUrl);
    }

    @Test
    void normalizeTrackUrl_shouldHandleNullUrl() {
        // Given
        String nullUrl = null;

        // When
        String normalizedUrl = DataQualityUtil.normalizeTrackUrl(nullUrl);

        // Then
        assertNull(normalizedUrl);
    }

    @Test
    void normalizeTrackUrl_shouldHandleEmptyUrl() {
        // Given
        String emptyUrl = "";

        // When
        String normalizedUrl = DataQualityUtil.normalizeTrackUrl(emptyUrl);

        // Then
        assertEquals("", normalizedUrl);
    }

    @Test
    void normalizeTrackUrl_shouldHandleShortUrl() {
        // Given - URL with less than 6 parts
        String shortUrl = "https://www.last.fm/music/Artist";

        // When
        String normalizedUrl = DataQualityUtil.normalizeTrackUrl(shortUrl);

        // Then
        assertEquals("https://www.last.fm/music/Artist", normalizedUrl);
    }

    @Test
    void normalizeTrackUrl_shouldHandleUrlWithExactly6Parts() {
        // Given - URL with exactly 6 parts (no track name)
        String urlWith6Parts = "https://www.last.fm/music/Artist/Album";

        // When
        String normalizedUrl = DataQualityUtil.normalizeTrackUrl(urlWith6Parts);

        // Then
        assertEquals("https://www.last.fm/music/Artist/Album", normalizedUrl);
    }

    @Test
    void normalizeTrackUrl_shouldHandleRealWorldExamples() {
        // Test with real LastFM URLs from the problematic JSON

        // Example 1: Better in Time by Leona Lewis
        String leonaLewisUrl = "https://www.last.fm/music/Leona+Lewis/_/Better+in+Time";
        assertEquals("https://www.last.fm/music/Leona+Lewis/_/Better+in+Time", 
            DataQualityUtil.normalizeTrackUrl(leonaLewisUrl));

        // Example 2: Track with album information
        String trackWithAlbum = "https://www.last.fm/music/Leona+Lewis/Spirit/Better+in+Time";
        assertEquals("https://www.last.fm/music/Leona+Lewis/_/Better+in+Time", 
            DataQualityUtil.normalizeTrackUrl(trackWithAlbum));

        // Example 3: Complex artist and track names
        String complexUrl = "https://www.last.fm/music/Bob+Marley+%26+The+Wailers/Legend/Three+Little+Birds";
        assertEquals("https://www.last.fm/music/Bob+Marley+%26+The+Wailers/_/Three+Little+Birds", 
            DataQualityUtil.normalizeTrackUrl(complexUrl));
    }

    @Test
    void normalizeTrackUrl_shouldBeIdempotent() {
        // Given
        String originalUrl = "https://www.last.fm/music/Artist/Album/Track";
        
        // When - normalize twice
        String firstNormalization = DataQualityUtil.normalizeTrackUrl(originalUrl);
        String secondNormalization = DataQualityUtil.normalizeTrackUrl(firstNormalization);

        // Then - should be the same
        assertEquals(firstNormalization, secondNormalization);
        assertEquals("https://www.last.fm/music/Artist/_/Track", secondNormalization);
    }
}
