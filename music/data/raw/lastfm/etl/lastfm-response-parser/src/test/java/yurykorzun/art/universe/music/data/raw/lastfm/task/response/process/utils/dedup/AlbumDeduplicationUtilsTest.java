package yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.utils.dedup;

import lombok.Getter;
import org.junit.jupiter.api.Test;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.album.AlbumDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AlbumDeduplicationUtilsTest {

    @Getter
    static class TestAlbumDto extends AlbumDto {
        public TestAlbumDto(String artistName, String name, String url, String mbid) {
            super();
            this.setName(name);
            this.setUrl(url);
            this.setMbid(mbid);
        }

        @Override
        public String getArtistName() {
            return "TestArtist"; // Simplified for testing
        }
    }

    @Test
    void deduplicateAlbumDtos_shouldDeduplicateByArtistAndNameFirst() {
        // Given - same artist + name, different URLs
        TestAlbumDto album1 = new TestAlbumDto("Artist", "Album", "url1", "mbid1");
        TestAlbumDto album2 = new TestAlbumDto("Artist", "Album", "url2", null); // duplicate by name, less complete
        TestAlbumDto album3 = new TestAlbumDto("Artist", "Different Album", "url3", "mbid3");

        List<TestAlbumDto> albums = List.of(album1, album2, album3);

        // When
        List<TestAlbumDto> result = AlbumDeduplicationUtils.deduplicateAlbumDtos(albums);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.contains(album1)); // Should keep more complete version
        assertFalse(result.contains(album2)); // Should remove duplicate
        assertTrue(result.contains(album3)); // Should keep unique album
    }

    @Test
    void deduplicateAlbumDtos_shouldDeduplicateByUrlSecond() {
        // Given - different names but same URL (second pass deduplication)
        TestAlbumDto album1 = new TestAlbumDto("Artist", "Album1", "same-url", "mbid1");
        TestAlbumDto album2 = new TestAlbumDto("Artist", "Album2", "same-url", null); // same URL, less complete
        TestAlbumDto album3 = new TestAlbumDto("Artist", "Album3", "different-url", "mbid3");

        List<TestAlbumDto> albums = List.of(album1, album2, album3);

        // When
        List<TestAlbumDto> result = AlbumDeduplicationUtils.deduplicateAlbumDtos(albums);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.contains(album1)); // Should keep more complete version
        assertFalse(result.contains(album2)); // Should remove duplicate by URL
        assertTrue(result.contains(album3)); // Should keep unique album
    }

    @Test
    void deduplicateAlbumDtos_shouldSelectAlbumWithMoreCompleteData() {
        // Given
        TestAlbumDto albumWithoutMbid = new TestAlbumDto("Artist", "Album", "url1", null);
        TestAlbumDto albumWithMbid = new TestAlbumDto("Artist", "Album", "url2", "mbid123");

        List<TestAlbumDto> albums = List.of(albumWithoutMbid, albumWithMbid);

        // When
        List<TestAlbumDto> result = AlbumDeduplicationUtils.deduplicateAlbumDtos(albums);

        // Then
        assertEquals(1, result.size());
        assertEquals("mbid123", result.get(0).getMbid());
    }

    @Test
    void deduplicateAlbumDtos_shouldSkipAlbumsWithoutNameOrArtist() {
        // Given
        TestAlbumDto validAlbum = new TestAlbumDto("Artist", "Album", "url1", "mbid1");
        TestAlbumDto albumWithoutName = new TestAlbumDto("Artist", null, "url2", "mbid2");
        TestAlbumDto albumWithoutUrl = new TestAlbumDto("Artist", "Album2", null, "mbid3");

        List<TestAlbumDto> albums = List.of(validAlbum, albumWithoutName, albumWithoutUrl);

        // When
        List<TestAlbumDto> result = AlbumDeduplicationUtils.deduplicateAlbumDtos(albums);

        // Then
        assertEquals(1, result.size());
        assertEquals(validAlbum, result.get(0));
    }

    @Test
    void deduplicateAlbumDtos_shouldHandleEmptyCollection() {
        // When
        List<TestAlbumDto> result = AlbumDeduplicationUtils.deduplicateAlbumDtos(List.of());

        // Then
        assertTrue(result.isEmpty());
    }
}
