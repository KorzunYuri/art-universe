package yurykorzun.art.universe.music.data.raw.lastfm.etl.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.BlacklistedEntityUrl;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.test.archetypes.LastfmJpaTestHelper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BlacklistedEntityUrlRepositoryTest extends LastfmJpaTestHelper {

    @Autowired
    private BlacklistedEntityUrlRepository repository;

    @Test
    void existsByEntityTypeAndUrl_shouldReturnTrue_whenUrlExists() {
        // Given
        BlacklistedEntityUrl blacklistEntry = BlacklistedEntityUrl.builder()
            .entityType(LastfmEntityType.ARTIST)
            .url("https://www.last.fm/music/TestArtist")
            .build();
        repository.save(blacklistEntry);

        // When
        boolean exists = repository.existsByEntityTypeAndUrl(LastfmEntityType.ARTIST, "https://www.last.fm/music/TestArtist");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByEntityTypeAndUrl_shouldReturnFalse_whenUrlDoesNotExist() {
        // When
        boolean exists = repository.existsByEntityTypeAndUrl(LastfmEntityType.ARTIST, "https://www.last.fm/music/NonExistent");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void existsByEntityTypeAndUrl_shouldReturnFalse_whenDifferentEntityType() {
        // Given
        BlacklistedEntityUrl blacklistEntry = BlacklistedEntityUrl.builder()
            .entityType(LastfmEntityType.ARTIST)
            .url("https://www.last.fm/music/TestArtist")
            .build();
        repository.save(blacklistEntry);

        // When
        boolean exists = repository.existsByEntityTypeAndUrl(LastfmEntityType.TRACK, "https://www.last.fm/music/TestArtist");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void findBlacklistedUrls_shouldReturnMatchingUrls() {
        // Given
        List<BlacklistedEntityUrl> blacklistEntries = List.of(
            BlacklistedEntityUrl.builder()
                .entityType(LastfmEntityType.ARTIST)
                .url("https://www.last.fm/music/Artist1")
                .build(),
            BlacklistedEntityUrl.builder()
                .entityType(LastfmEntityType.ARTIST)
                .url("https://www.last.fm/music/Artist2")
                .build(),
            BlacklistedEntityUrl.builder()
                .entityType(LastfmEntityType.TRACK)
                .url("https://www.last.fm/music/Artist1/_/Track1")
                .build()
        );
        repository.saveAll(blacklistEntries);

        List<String> urlsToCheck = List.of(
            "https://www.last.fm/music/Artist1",
            "https://www.last.fm/music/Artist3",
            "https://www.last.fm/music/Artist2"
        );

        // When
        List<String> blacklistedUrls = repository.findBlacklistedUrls(LastfmEntityType.ARTIST, urlsToCheck);

        // Then
        assertThat(blacklistedUrls)
            .hasSize(2)
            .containsExactlyInAnyOrder(
                "https://www.last.fm/music/Artist1",
                "https://www.last.fm/music/Artist2"
            );
    }

    @Test
    void findBlacklistedUrls_shouldReturnEmpty_whenNoMatches() {
        // Given
        List<String> urlsToCheck = List.of(
            "https://www.last.fm/music/NonExistent1",
            "https://www.last.fm/music/NonExistent2"
        );

        // When
        List<String> blacklistedUrls = repository.findBlacklistedUrls(LastfmEntityType.ARTIST, urlsToCheck);

        // Then
        assertThat(blacklistedUrls).isEmpty();
    }

    @Test
    void insertIgnoreDuplicate_shouldInsertNewUrl() {
        // When
        int inserted = repository.insertIgnoreDuplicate(LastfmEntityType.ARTIST, "https://www.last.fm/music/NewArtist");

        // Then
        assertThat(inserted).isEqualTo(1);
        assertThat(repository.existsByEntityTypeAndUrl(LastfmEntityType.ARTIST, "https://www.last.fm/music/NewArtist"))
            .isTrue();
    }

    @Test
    void insertIgnoreDuplicate_shouldIgnoreDuplicate() {
        // Given
        BlacklistedEntityUrl existing = BlacklistedEntityUrl.builder()
            .entityType(LastfmEntityType.ARTIST)
            .url("https://www.last.fm/music/ExistingArtist")
            .build();
        repository.save(existing);

        // When
        int inserted = repository.insertIgnoreDuplicate(LastfmEntityType.ARTIST, "https://www.last.fm/music/ExistingArtist");

        // Then
        assertThat(repository.count()).isEqualTo(1); // Still only one record
        assertThat(inserted).isEqualTo(0);
    }

    @Test
    void insertIgnoreDuplicates_shouldInsertNewUrls() {
        // Given
        List<String> urls = List.of(
            "https://www.last.fm/music/Artist1",
            "https://www.last.fm/music/Artist2",
            "https://www.last.fm/music/Artist3"
        );

        // When
        int inserted = repository.insertIgnoreDuplicates(LastfmEntityType.ARTIST, urls);

        // Then
        assertThat(inserted).isEqualTo(3);
        assertThat(repository.count()).isEqualTo(3);
        
        for (String url : urls) {
            assertThat(repository.existsByEntityTypeAndUrl(LastfmEntityType.ARTIST, url)).isTrue();
        }
    }

    @Test
    void insertIgnoreDuplicates_shouldIgnoreDuplicatesAndInsertNew() {
        // Given
        BlacklistedEntityUrl existing = BlacklistedEntityUrl.builder()
            .entityType(LastfmEntityType.ARTIST)
            .url("https://www.last.fm/music/ExistingArtist")
            .build();
        repository.save(existing);

        List<String> urls = List.of(
            "https://www.last.fm/music/ExistingArtist", // duplicate
            "https://www.last.fm/music/NewArtist1",     // new
            "https://www.last.fm/music/NewArtist2"      // new
        );

        // When
        int inserted = repository.insertIgnoreDuplicates(LastfmEntityType.ARTIST, urls);

        // Then
        assertThat(inserted).isEqualTo(2); // Only 2 new records inserted
        assertThat(repository.count()).isEqualTo(3); // 1 existing + 2 new
        
        for (String url : urls) {
            assertThat(repository.existsByEntityTypeAndUrl(LastfmEntityType.ARTIST, url)).isTrue();
        }
    }

    @Test
    void insertIgnoreDuplicates_shouldHandleEmptyList() {
        // When
        int inserted = repository.insertIgnoreDuplicates(LastfmEntityType.ARTIST, List.of());

        // Then
        assertThat(inserted).isEqualTo(0);
        assertThat(repository.count()).isEqualTo(0);
    }

    @Test
    void findByEntityTypeAndUrlIn_shouldReturnMatchingEntities() {
        // Given
        List<BlacklistedEntityUrl> blacklistEntries = List.of(
            BlacklistedEntityUrl.builder()
                .entityType(LastfmEntityType.ARTIST)
                .url("https://www.last.fm/music/Artist1")
                .build(),
            BlacklistedEntityUrl.builder()
                .entityType(LastfmEntityType.ARTIST)
                .url("https://www.last.fm/music/Artist2")
                .build(),
            BlacklistedEntityUrl.builder()
                .entityType(LastfmEntityType.TRACK)
                .url("https://www.last.fm/music/Artist1/_/Track1")
                .build()
        );
        repository.saveAll(blacklistEntries);

        List<String> urlsToFind = List.of(
            "https://www.last.fm/music/Artist1",
            "https://www.last.fm/music/NonExistent"
        );

        // When
        List<BlacklistedEntityUrl> found = repository.findByEntityTypeAndUrlIn(LastfmEntityType.ARTIST, urlsToFind);

        // Then
        assertThat(found)
            .hasSize(1)
            .extracting(BlacklistedEntityUrl::getUrl)
            .containsExactly("https://www.last.fm/music/Artist1");
    }
}
