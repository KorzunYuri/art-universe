package yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.LastfmJpaTestHelper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LastfmArtistRepositoryTest extends LastfmJpaTestHelper {

    @Autowired
    private LastfmArtistRepository artistRepository;

    private LastfmArtist radiohead;
    private LastfmArtist radioMoscow;
    private LastfmArtist metallica;

    @BeforeEach
    void setup() {
        // Create test data
        radiohead = consistencyHelper.createAndSaveArtist(builder -> builder
            .name("Radiohead")
            .playCount(10000L)
            .listenersCount(5000)
            .approvalStatus(ApprovalStatus.APPROVED));
            
        radioMoscow = consistencyHelper.createAndSaveArtist(builder -> builder
            .name("Radio Moscow")
            .playCount(5000L)
            .listenersCount(2000)
            .approvalStatus(ApprovalStatus.PENDING));
            
        metallica = consistencyHelper.createAndSaveArtist(builder -> builder
            .name("Metallica")
            .playCount(20000L)
            .listenersCount(10000)
            .approvalStatus(ApprovalStatus.APPROVED));
    }

    @Test
    void findAllToGetInfoFor_shouldReturnArtists() {

        // when
        List<LastfmArtist> result = artistRepository.findAllToGetInfoFor();

        // then
        assertNotNull(result);
    }

    @Test
    void findAllToGetInfoFor_shouldExcludeBlacklistedArtists() {
        // isolate the test
        artistRepository.deleteAll();

        // Create additional artists
        LastfmArtist artist1 = consistencyHelper.createAndSaveArtist(builder ->
            builder.name("Artist 1")
                .url("https://www.last.fm/music/Artist+1")
                .listenersCount(null)  // Missing stats - needs getInfo
                .playCount(null)
                .approvalStatus(ApprovalStatus.APPROVED));

        LastfmArtist artist2 = consistencyHelper.createAndSaveArtist(builder ->
            builder.name("Artist 2")
                .url("https://www.last.fm/music/Artist+2")
                .listenersCount(null)  // Missing stats - needs getInfo
                .playCount(null)
                .approvalStatus(ApprovalStatus.APPROVED));

        LastfmArtist artist3 = consistencyHelper.createAndSaveArtist(builder ->
            builder.name("Artist 3")
                .url("https://www.last.fm/music/Artist+3")
                .listenersCount(null)  // Missing stats - needs getInfo
                .playCount(null)
                .approvalStatus(ApprovalStatus.APPROVED));

        // Add artist2 to blacklist
        consistencyHelper.addToBlacklist(LastfmEntityType.ARTIST, artist2.getUrl());

        // make sure changes have been applied
        consistencyHelper.flush();

        // Execute query
        List<LastfmArtist> result = artistRepository.findAllToGetInfoFor(10);

        // Verify results - should exclude blacklisted artist2
        assertEquals(2, result.size(), "Should return all artists excluding blacklisted");
        assertTrue(result.stream().anyMatch(a -> "Artist 1".equals(a.getName())),
            "Should include Artist 1");
        assertTrue(result.stream().anyMatch(a -> "Artist 3".equals(a.getName())),
            "Should include Artist 3");
        assertFalse(result.stream().anyMatch(a -> "Artist 2".equals(a.getName())),
            "Should exclude blacklisted Artist 2");
    }

    @Test
    void findAllToGetInfoFor_shouldNotExcludeArtistsWithNullUrls() {

        // Create artist with null URL
        LastfmArtist artistWithNullUrl = consistencyHelper.createAndSaveArtist(builder ->
            builder.name("Artist With Null URL")
                .url(null)
                .listenersCount(null)  // Missing stats - needs getInfo
                .playCount(null)
                .approvalStatus(ApprovalStatus.APPROVED));

        // Create artist with empty URL
        LastfmArtist artistWithEmptyUrl = consistencyHelper.createAndSaveArtist(builder ->
            builder.name("Artist With Empty URL")
                .url("")
                .listenersCount(null)  // Missing stats - needs getInfo
                .playCount(null)
                .approvalStatus(ApprovalStatus.APPROVED));

        // Create artist with valid URL and blacklist it
        LastfmArtist artistWithUrl = consistencyHelper.createAndSaveArtist(builder ->
            builder.name("Artist With URL but blacklisted")
                .url("https://www.last.fm/music/Artist+With+URL")
                .listenersCount(null)  // Missing stats - needs getInfo
                .playCount(null)
                .approvalStatus(ApprovalStatus.APPROVED));

        // Add artist with URL to blacklist
        consistencyHelper.addToBlacklist(LastfmEntityType.ARTIST, artistWithUrl.getUrl());

        // make sure changes have been applied
        consistencyHelper.flush();

        // Execute query
        List<LastfmArtist> result = artistRepository.findAllToGetInfoFor(10);

        // Verify results - should include artist with null URL, exclude blacklisted artist
        assertTrue(result.stream().anyMatch(a -> "Artist With Null URL".equals(a.getName())),
            "Should include artist with null URL");
        assertTrue(result.stream().anyMatch(a -> "Artist With Empty URL".equals(a.getName())),
            "Should include artist with empty URL");
        assertFalse(result.stream().anyMatch(a -> "Artist With URL but blacklisted".equals(a.getName())),
            "Should exclude blacklisted artist");
    }
}

