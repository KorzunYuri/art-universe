package yurykorzun.art.universe.music.data.raw.lastfm.etl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.repository.BlacklistedEntityUrlRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.LastfmEntityType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlacklistedEntityUrlServiceTest {

    @Mock
    private BlacklistedEntityUrlRepository blacklistRepository;

    @InjectMocks
    private BlacklistedEntityUrlService service;

    @Test
    void isBlacklisted_shouldReturnTrue_whenUrlIsBlacklisted() {
        // Given
        String url = "https://www.last.fm/music/TestArtist";
        when(blacklistRepository.existsByEntityTypeAndUrl(LastfmEntityType.ARTIST, url))
            .thenReturn(true);

        // When
        boolean result = service.isBlacklisted(LastfmEntityType.ARTIST, url);

        // Then
        assertThat(result).isTrue();
        verify(blacklistRepository).existsByEntityTypeAndUrl(LastfmEntityType.ARTIST, url);
    }

    @Test
    void isBlacklisted_shouldReturnFalse_whenUrlIsNotBlacklisted() {
        // Given
        String url = "https://www.last.fm/music/TestArtist";
        when(blacklistRepository.existsByEntityTypeAndUrl(LastfmEntityType.ARTIST, url))
            .thenReturn(false);

        // When
        boolean result = service.isBlacklisted(LastfmEntityType.ARTIST, url);

        // Then
        assertThat(result).isFalse();
        verify(blacklistRepository).existsByEntityTypeAndUrl(LastfmEntityType.ARTIST, url);
    }

    @Test
    void isBlacklisted_shouldReturnFalse_whenUrlIsNull() {
        // When
        boolean result = service.isBlacklisted(LastfmEntityType.ARTIST, null);

        // Then
        assertThat(result).isFalse();
        verifyNoInteractions(blacklistRepository);
    }

    @Test
    void isBlacklisted_shouldReturnFalse_whenUrlIsEmpty() {
        // When
        boolean result = service.isBlacklisted(LastfmEntityType.ARTIST, "");

        // Then
        assertThat(result).isFalse();
        verifyNoInteractions(blacklistRepository);
    }

    @Test
    void isBlacklisted_shouldReturnFalse_whenUrlIsBlank() {
        // When
        boolean result = service.isBlacklisted(LastfmEntityType.ARTIST, "   ");

        // Then
        assertThat(result).isFalse();
        verifyNoInteractions(blacklistRepository);
    }

    @Test
    void getBlacklisted_shouldReturnBlacklistedUrls() {
        // Given
        List<String> urls = List.of(
            "https://www.last.fm/music/Artist1",
            "https://www.last.fm/music/Artist2",
            "https://www.last.fm/music/Artist3"
        );
        List<String> blacklistedUrls = List.of(
            "https://www.last.fm/music/Artist1",
            "https://www.last.fm/music/Artist3"
        );
        
        when(blacklistRepository.findBlacklistedUrls(LastfmEntityType.ARTIST, urls))
            .thenReturn(blacklistedUrls);

        // When
        List<String> result = service.getBlacklisted(LastfmEntityType.ARTIST, urls);

        // Then
        assertThat(result).containsExactlyInAnyOrderElementsOf(blacklistedUrls);
        verify(blacklistRepository).findBlacklistedUrls(LastfmEntityType.ARTIST, urls);
    }

    @Test
    void getBlacklisted_shouldReturnEmpty_whenUrlListIsNull() {
        // When
        List<String> result = service.getBlacklisted(LastfmEntityType.ARTIST, null);

        // Then
        assertThat(result).isEmpty();
        verifyNoInteractions(blacklistRepository);
    }

    @Test
    void getBlacklisted_shouldReturnEmpty_whenUrlListIsEmpty() {
        // When
        List<String> result = service.getBlacklisted(LastfmEntityType.ARTIST, List.of());

        // Then
        assertThat(result).isEmpty();
        verifyNoInteractions(blacklistRepository);
    }

    @Test
    void getBlacklisted_shouldFilterNullAndEmptyUrls() {
        // Given
        List<String> urls = List.of(
            "https://www.last.fm/music/Artist1",
            "",
            "   ",
            "https://www.last.fm/music/Artist2"
        );
        List<String> expectedValidUrls = List.of(
            "https://www.last.fm/music/Artist1",
            "https://www.last.fm/music/Artist2"
        );
        List<String> blacklistedUrls = List.of("https://www.last.fm/music/Artist1");
        
        when(blacklistRepository.findBlacklistedUrls(LastfmEntityType.ARTIST, expectedValidUrls))
            .thenReturn(blacklistedUrls);

        // When
        List<String> result = service.getBlacklisted(LastfmEntityType.ARTIST, urls);

        // Then
        assertThat(result).containsExactlyInAnyOrderElementsOf(blacklistedUrls);
        verify(blacklistRepository).findBlacklistedUrls(LastfmEntityType.ARTIST, expectedValidUrls);
    }

    @Test
    void getBlacklisted_shouldHandleDuplicateUrls() {
        // Given
        List<String> urls = List.of(
            "https://www.last.fm/music/Artist1",
            "https://www.last.fm/music/Artist2",
            "https://www.last.fm/music/Artist1" // duplicate
        );
        List<String> expectedUniqueUrls = List.of(
            "https://www.last.fm/music/Artist1",
            "https://www.last.fm/music/Artist2"
        );
        List<String> blacklistedUrls = List.of("https://www.last.fm/music/Artist1");
        
        when(blacklistRepository.findBlacklistedUrls(LastfmEntityType.ARTIST, expectedUniqueUrls))
            .thenReturn(blacklistedUrls);

        // When
        List<String> result = service.getBlacklisted(LastfmEntityType.ARTIST, urls);

        // Then
        assertThat(result).containsExactlyInAnyOrderElementsOf(blacklistedUrls);
        verify(blacklistRepository).findBlacklistedUrls(LastfmEntityType.ARTIST, expectedUniqueUrls);
    }

    @Test
    void addToBlacklist_singleUrl_shouldCallRepository() {
        // Given
        String url = "https://www.last.fm/music/TestArtist";
        when(blacklistRepository.insertIgnoreDuplicate(LastfmEntityType.ARTIST, url))
            .thenReturn(1);

        // When
        service.addToBlacklist(LastfmEntityType.ARTIST, url);

        // Then
        verify(blacklistRepository).insertIgnoreDuplicate(LastfmEntityType.ARTIST, url);
    }

    @Test
    void addToBlacklist_singleUrl_shouldNotCallRepository_whenUrlIsNull() {
        // When
        service.addToBlacklist(LastfmEntityType.ARTIST, (String) null);

        // Then
        verifyNoInteractions(blacklistRepository);
    }

    @Test
    void addToBlacklist_singleUrl_shouldNotCallRepository_whenUrlIsEmpty() {
        // When
        service.addToBlacklist(LastfmEntityType.ARTIST, "");

        // Then
        verifyNoInteractions(blacklistRepository);
    }

    @Test
    void addToBlacklist_singleUrl_shouldNotCallRepository_whenUrlIsBlank() {
        // When
        service.addToBlacklist(LastfmEntityType.ARTIST, "   ");

        // Then
        verifyNoInteractions(blacklistRepository);
    }

    @Test
    void addToBlacklist_multipleUrls_shouldCallRepository() {
        // Given
        List<String> urls = List.of(
            "https://www.last.fm/music/Artist1",
            "https://www.last.fm/music/Artist2"
        );
        when(blacklistRepository.insertIgnoreDuplicates(LastfmEntityType.ARTIST, urls))
            .thenReturn(2);

        // When
        service.addToBlacklist(LastfmEntityType.ARTIST, urls);

        // Then
        verify(blacklistRepository).insertIgnoreDuplicates(LastfmEntityType.ARTIST, urls);
    }

    @Test
    void addToBlacklist_multipleUrls_shouldNotCallRepository_whenUrlListIsNull() {
        // When
        service.addToBlacklist(LastfmEntityType.ARTIST, (List<String>) null);

        // Then
        verifyNoInteractions(blacklistRepository);
    }

    @Test
    void addToBlacklist_multipleUrls_shouldNotCallRepository_whenUrlListIsEmpty() {
        // When
        service.addToBlacklist(LastfmEntityType.ARTIST, List.of());

        // Then
        verifyNoInteractions(blacklistRepository);
    }

    @Test
    void addToBlacklist_multipleUrls_shouldFilterNullAndEmptyUrls() {
        // Given
        List<String> urls = List.of(
            "https://www.last.fm/music/Artist1",
            "",
            "   ",
            "https://www.last.fm/music/Artist2"
        );
        List<String> expectedValidUrls = List.of(
            "https://www.last.fm/music/Artist1",
            "https://www.last.fm/music/Artist2"
        );
        when(blacklistRepository.insertIgnoreDuplicates(LastfmEntityType.ARTIST, expectedValidUrls))
            .thenReturn(2);

        // When
        service.addToBlacklist(LastfmEntityType.ARTIST, urls);

        // Then
        verify(blacklistRepository).insertIgnoreDuplicates(LastfmEntityType.ARTIST, expectedValidUrls);
    }

    @Test
    void addToBlacklist_multipleUrls_shouldHandleDuplicateUrls() {
        // Given
        List<String> urls = List.of(
            "https://www.last.fm/music/Artist1",
            "https://www.last.fm/music/Artist2",
            "https://www.last.fm/music/Artist1" // duplicate
        );
        List<String> expectedUniqueUrls = List.of(
            "https://www.last.fm/music/Artist1",
            "https://www.last.fm/music/Artist2"
        );
        when(blacklistRepository.insertIgnoreDuplicates(LastfmEntityType.ARTIST, expectedUniqueUrls))
            .thenReturn(2);

        // When
        service.addToBlacklist(LastfmEntityType.ARTIST, urls);

        // Then
        verify(blacklistRepository).insertIgnoreDuplicates(LastfmEntityType.ARTIST, expectedUniqueUrls);
    }

    @Test
    void addToBlacklist_multipleUrls_shouldNotCallRepository_whenAllUrlsAreInvalid() {
        // Given
        List<String> urls = List.of("", "   ");

        // When
        service.addToBlacklist(LastfmEntityType.ARTIST, urls);

        // Then
        verifyNoInteractions(blacklistRepository);
    }
}
