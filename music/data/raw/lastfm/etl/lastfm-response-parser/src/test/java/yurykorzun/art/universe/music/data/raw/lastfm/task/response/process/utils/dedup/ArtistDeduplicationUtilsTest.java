package yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.utils.dedup;

import lombok.Getter;
import org.junit.jupiter.api.Test;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.artist.ArtistDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ArtistDeduplicationUtilsTest {

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
        List<TestArtistDto> result = ArtistDeduplicationUtils.deduplicateArtistDtos(artists);

        // Then
        assertEquals(2, result.size());

        TestArtistDto paramore = result.stream()
            .filter(artist -> "Paramore".equals(artist.getName()))
            .findFirst()
            .orElseThrow();

        assertEquals("44cf61b8-5197-448a-b82b-cef6ee89fac5", paramore.getMbid());

        TestArtistDto snowPatrol = result.stream()
            .filter(artist -> "Snow Patrol".equals(artist.getName()))
            .findFirst()
            .orElseThrow();

        assertEquals("a66999a7-ae5c-460e-ba94-1a01143ae847", snowPatrol.getMbid());
    }

    @Test
    void deduplicateArtistDtos_shouldSkipNullNames() {
        // Given
        List<TestArtistDto> artists = List.of(
            new TestArtistDto(null, "mbid1", "url1"),
            new TestArtistDto("", "mbid2", "url2"),
            new TestArtistDto("ValidArtist", "mbid4", "url4")
        );

        // When
        List<TestArtistDto> result = ArtistDeduplicationUtils.deduplicateArtistDtos(artists);

        // Then
        assertEquals(1, result.size());
        assertEquals("ValidArtist", result.get(0).getName());
    }

    @Test
    void deduplicateArtistDtos_shouldHandleEmptyCollection() {
        // When
        List<TestArtistDto> result = ArtistDeduplicationUtils.deduplicateArtistDtos(List.of());

        // Then
        assertTrue(result.isEmpty());
    }
}
