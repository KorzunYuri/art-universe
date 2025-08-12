package yurykorzun.art.universe.music.data.raw.lastfm.collectable.blacklist.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.blacklist.repository.BlacklistedEntityUrlRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaOnlyTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
@Import(BlacklistedEntityUrlService.class)
class BlacklistedEntityUrlServiceIntegrationTest extends JpaOnlyTest {

    @Autowired
    private BlacklistedEntityUrlRepository blacklistRepository;

    @Autowired
    private DbConsistencyHelper dbHelper;

    @Autowired
    private BlacklistedEntityUrlService blacklistService;

    @BeforeEach
    void setUp() {
        dbHelper.cleanup();
    }

    @Test
    void shouldAddSingleUrlToBlacklist() {
        // Given
        String url = "https://www.last.fm/music/Test+Artist";

        // When
        blacklistService.addToBlacklist(LastfmEntityType.ARTIST, url);

        // Then
        assertThat(blacklistService.isBlacklisted(LastfmEntityType.ARTIST, url)).isTrue();
        assertThat(blacklistRepository.existsByEntityTypeAndUrl(LastfmEntityType.ARTIST, url)).isTrue();
    }

    @Test
    void shouldAddMultipleUrlsToBlacklist() {
        // Given
        List<String> urls = List.of(
            "https://www.last.fm/music/Artist+1",
            "https://www.last.fm/music/Artist+2",
            "https://www.last.fm/music/Artist+3"
        );

        // When
        blacklistService.addToBlacklist(LastfmEntityType.ARTIST, urls);

        // Then
        for (String url : urls) {
            assertThat(blacklistService.isBlacklisted(LastfmEntityType.ARTIST, url)).isTrue();
        }
        
        List<String> blacklisted = blacklistService.getBlacklisted(LastfmEntityType.ARTIST, urls);
        assertThat(blacklisted).containsExactlyInAnyOrderElementsOf(urls);
    }

    @Test
    void shouldHandleDuplicateUrls() {
        // Given
        String url = "https://www.last.fm/music/Duplicate+Artist";
        
        // When - add same URL twice
        blacklistService.addToBlacklist(LastfmEntityType.ARTIST, url);
        blacklistService.addToBlacklist(LastfmEntityType.ARTIST, url);

        // Then - should not cause errors and URL should still be blacklisted
        assertThat(blacklistService.isBlacklisted(LastfmEntityType.ARTIST, url)).isTrue();
        assertThat(blacklistRepository.count()).isEqualTo(1); // Only one record in DB
    }

    @Test
    void shouldHandleDuplicatesInBatchOperation() {
        // Given
        List<String> urlsWithDuplicates = List.of(
            "https://www.last.fm/music/Artist+1",
            "https://www.last.fm/music/Artist+2",
            "https://www.last.fm/music/Artist+1", // duplicate
            "https://www.last.fm/music/Artist+3"
        );

        // When
        blacklistService.addToBlacklist(LastfmEntityType.ARTIST, urlsWithDuplicates);

        // Then
        List<String> uniqueUrls = List.of(
            "https://www.last.fm/music/Artist+1",
            "https://www.last.fm/music/Artist+2",
            "https://www.last.fm/music/Artist+3"
        );
        
        List<String> blacklisted = blacklistService.getBlacklisted(LastfmEntityType.ARTIST, uniqueUrls);
        assertThat(blacklisted).containsExactlyInAnyOrderElementsOf(uniqueUrls);
        assertThat(blacklistRepository.count()).isEqualTo(3); // Only unique records in DB
    }

    @Test
    void shouldFilterBlacklistedUrls() {
        // Given
        List<String> allUrls = List.of(
            "https://www.last.fm/music/Artist+1",
            "https://www.last.fm/music/Artist+2",
            "https://www.last.fm/music/Artist+3",
            "https://www.last.fm/music/Artist+4"
        );
        
        List<String> urlsToBlacklist = List.of(
            "https://www.last.fm/music/Artist+2",
            "https://www.last.fm/music/Artist+4"
        );

        // When
        blacklistService.addToBlacklist(LastfmEntityType.ARTIST, urlsToBlacklist);
        List<String> blacklisted = blacklistService.getBlacklisted(LastfmEntityType.ARTIST, allUrls);

        // Then
        assertThat(blacklisted).containsExactlyInAnyOrderElementsOf(urlsToBlacklist);
    }

    @Test
    void shouldHandleDifferentEntityTypes() {
        // Given
        String artistUrl = "https://www.last.fm/music/Test+Artist";
        String trackUrl = "https://www.last.fm/music/Test+Artist/_/Test+Track";

        // When
        blacklistService.addToBlacklist(LastfmEntityType.ARTIST, artistUrl);
        blacklistService.addToBlacklist(LastfmEntityType.TRACK, trackUrl);

        // Then
        assertThat(blacklistService.isBlacklisted(LastfmEntityType.ARTIST, artistUrl)).isTrue();
        assertThat(blacklistService.isBlacklisted(LastfmEntityType.TRACK, trackUrl)).isTrue();
        
        // Same URL but different entity type should not be blacklisted
        assertThat(blacklistService.isBlacklisted(LastfmEntityType.TRACK, artistUrl)).isFalse();
        assertThat(blacklistService.isBlacklisted(LastfmEntityType.ARTIST, trackUrl)).isFalse();
    }

    @Test
    void shouldHandleEmptyUrls() {
        // Given
        List<String> urlsWithEmpty = List.of(
            "https://www.last.fm/music/Valid+Artist",
            "",
            "   ",
            "https://www.last.fm/music/Another+Valid+Artist"
        );

        // When
        blacklistService.addToBlacklist(LastfmEntityType.ARTIST, urlsWithEmpty);

        // Then - only valid URLs should be added
        assertThat(blacklistService.isBlacklisted(LastfmEntityType.ARTIST, "https://www.last.fm/music/Valid+Artist")).isTrue();
        assertThat(blacklistService.isBlacklisted(LastfmEntityType.ARTIST, "https://www.last.fm/music/Another+Valid+Artist")).isTrue();
        assertThat(blacklistRepository.count()).isEqualTo(2);
    }

    @Test
    void shouldReturnFalseForNullAndEmptyUrlsInCheck() {
        // When & Then
        assertThat(blacklistService.isBlacklisted(LastfmEntityType.ARTIST, null)).isFalse();
        assertThat(blacklistService.isBlacklisted(LastfmEntityType.ARTIST, "")).isFalse();
        assertThat(blacklistService.isBlacklisted(LastfmEntityType.ARTIST, "   ")).isFalse();
    }

    @Test
    void shouldReturnEmptyListForNullAndEmptyUrlLists() {
        // When & Then
        assertThat(blacklistService.getBlacklisted(LastfmEntityType.ARTIST, null)).isEmpty();
        assertThat(blacklistService.getBlacklisted(LastfmEntityType.ARTIST, List.of())).isEmpty();
    }

    @Test
    void shouldHandleMixedValidAndInvalidUrlsInGetBlacklisted() {
        // Given
        blacklistService.addToBlacklist(LastfmEntityType.ARTIST, "https://www.last.fm/music/Valid+Artist");
        
        List<String> mixedUrls = List.of(
            "https://www.last.fm/music/Valid+Artist",
            "",
            "https://www.last.fm/music/Not+Blacklisted"
        );

        // When
        List<String> blacklisted = blacklistService.getBlacklisted(LastfmEntityType.ARTIST, mixedUrls);

        // Then
        assertThat(blacklisted).containsExactly("https://www.last.fm/music/Valid+Artist");
    }

    @Test
    void shouldWorkWithLargeNumberOfUrls() {
        // Given
        List<String> manyUrls = List.of(
            "https://www.last.fm/music/Artist+1",
            "https://www.last.fm/music/Artist+2",
            "https://www.last.fm/music/Artist+3",
            "https://www.last.fm/music/Artist+4",
            "https://www.last.fm/music/Artist+5",
            "https://www.last.fm/music/Artist+6",
            "https://www.last.fm/music/Artist+7",
            "https://www.last.fm/music/Artist+8",
            "https://www.last.fm/music/Artist+9",
            "https://www.last.fm/music/Artist+10"
        );

        // When
        blacklistService.addToBlacklist(LastfmEntityType.ARTIST, manyUrls);

        // Then
        List<String> blacklisted = blacklistService.getBlacklisted(LastfmEntityType.ARTIST, manyUrls);
        assertThat(blacklisted).containsExactlyInAnyOrderElementsOf(manyUrls);
        assertThat(blacklistRepository.count()).isEqualTo(manyUrls.size());
    }

    @Test
    void shouldHandlePartialBlacklistInLargeList() {
        // Given
        List<String> allUrls = List.of(
            "https://www.last.fm/music/Artist+1",
            "https://www.last.fm/music/Artist+2",
            "https://www.last.fm/music/Artist+3",
            "https://www.last.fm/music/Artist+4",
            "https://www.last.fm/music/Artist+5"
        );
        
        List<String> urlsToBlacklist = List.of(
            "https://www.last.fm/music/Artist+2",
            "https://www.last.fm/music/Artist+4"
        );

        // When
        blacklistService.addToBlacklist(LastfmEntityType.ARTIST, urlsToBlacklist);
        List<String> blacklisted = blacklistService.getBlacklisted(LastfmEntityType.ARTIST, allUrls);

        // Then
        assertThat(blacklisted).containsExactlyInAnyOrderElementsOf(urlsToBlacklist);
        assertThat(blacklisted).hasSize(2);
    }
}
