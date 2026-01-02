package yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.LastfmJpaTestHelper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LastfmAlbumRepositoryTest extends LastfmJpaTestHelper {

    @Autowired
    private LastfmAlbumRepository albumRepository;

    @Autowired
    private DbConsistencyHelper consistencyHelper;

    @Autowired
    private EntityManager entityManager;
    
    @BeforeEach
    void setUp() {
        consistencyHelper.cleanup();
    }
    
    @AfterEach
    void tearDown() {
        consistencyHelper.cleanup();
    }

    @Test
    void findAlbumsForGetInfo_shouldReturnAlbums() {
        // given
        consistencyHelper.createAndSaveAlbum();
        consistencyHelper.createAndSaveAlbum();

        // when
        List<LastfmAlbum> result = albumRepository.findAlbumsForGetInfo();

        // then
        assertNotNull(result);
    }


    @Test
    void findAlbumsForGetInfo_shouldPrioritizeAlbumsWithMissingStats() {
        // Create artists with different listener counts
        LastfmArtist popularArtist = consistencyHelper.createAndSaveArtist(builder ->
            builder.name("Popular Artist").listenersCount(10000).approvalStatus(ApprovalStatus.APPROVED));

        LastfmArtist lessPopularArtist = consistencyHelper.createAndSaveArtist(builder ->
            builder.name("Less Popular Artist").listenersCount(5000).approvalStatus(ApprovalStatus.PENDING));

        LastfmArtist unpopularArtist = consistencyHelper.createAndSaveArtist(builder ->
            builder.name("Unpopular Artist").listenersCount(1000).approvalStatus(ApprovalStatus.PENDING));

        // Create albums with different stats
        // Album with missing stats from popular artist (highest priority)
        LastfmAlbum albumWithMissingStats = consistencyHelper.createAndSaveAlbum(builder ->
            builder.name("Album With Missing Stats")
                .url("https://example.com/album1")
                .playCount(null)
                .listenersCount(null)
                .artist(popularArtist));

        // Album with stats from popular artist (lower priority)
        LastfmAlbum albumWithStats = consistencyHelper.createAndSaveAlbum(builder ->
            builder.name("Album With Stats")
                .url("https://example.com/album2")
                .playCount(5000L)
                .listenersCount(1000)
                .artist(popularArtist));

        // Album with missing stats from less popular artist (medium priority)
        LastfmAlbum albumFromLessPopularArtist = consistencyHelper.createAndSaveAlbum(builder ->
            builder.name("Album From Less Popular Artist")
                .url("https://example.com/album3")
                .playCount(null)
                .listenersCount(null)
                .artist(lessPopularArtist));

        // Album with missing stats from unpopular artist (lowest priority)
        LastfmAlbum albumFromUnpopularArtist = consistencyHelper.createAndSaveAlbum(builder ->
            builder.name("Album From Unpopular Artist")
                .url("https://example.com/album4")
                .playCount(null)
                .listenersCount(null)
                .artist(unpopularArtist));

        // Create an API call for one album to test filtering
        LastfmApiCall apiCall = consistencyHelper.createAndSaveApiCall(LastfmApiCallType.ALBUM_GET_INFO, albumWithStats);

        // Execute query with limit 3
        List<LastfmAlbum> result = albumRepository.findAlbumsForGetInfo(3);

        // Verify results
        assertEquals(3, result.size(), "Should return 3 albums");

        // First should be the album with missing stats from popular artist
        assertEquals(albumWithMissingStats.getId(), result.get(0).getId(),
            "First album should be the one with missing stats from popular artist");

        // Second should be the album from less popular artist
        assertEquals(albumFromLessPopularArtist.getId(), result.get(1).getId(),
            "Second album should be the one from less popular artist");

        // Third should be the album from unpopular artist
        assertEquals(albumFromUnpopularArtist.getId(), result.get(2).getId(),
            "Third album should be the one from unpopular artist");

        // Album with stats and pending API call should not be included
        assertFalse(result.contains(albumWithStats),
            "Album with stats and pending API call should not be included");
    }

    @Test
    void findAlbumsForGetInfo_shouldLimitAlbumsPerArtist() {
        // Create an artist
        LastfmArtist artist = consistencyHelper.createAndSaveArtist(builder ->
            builder.name("Test Artist").listenersCount(5000));

        // Create 5 albums for the same artist
        for (int i = 1; i <= 5; i++) {
            int ind = i;
            consistencyHelper.createAndSaveAlbum(builder ->
                builder.name("Album " + ind)
                    .url("https://example.com/album" + ind)
                    .playCount(null)
                    .listenersCount(null)
                    .artist(artist));
        }

        // Execute query with albumsPerArtist = 2
        List<LastfmAlbum> result = albumRepository.findAlbumsForGetInfo(10, 2);

        // Verify results
        assertEquals(2, result.size(), "Should return only 2 albums per artist");
        assertEquals("Album 1", result.get(0).getName(), "Should return albums in order of creation");
        assertEquals("Album 2", result.get(1).getName(), "Should return albums in order of creation");
    }

    @Test
    void findAlbumsForGetInfo_shouldExcludeBlacklistedAlbums() {
        // Create an artist
        LastfmArtist artist = consistencyHelper.createAndSaveArtist(builder ->
            builder.name("Test Artist")
                .listenersCount(5000)
                .approvalStatus(ApprovalStatus.APPROVED));

        // Create albums
        LastfmAlbum album1 = consistencyHelper.createAndSaveAlbum(builder ->
            builder.name("Album 1")
                .url("https://www.last.fm/music/Test+Artist/Album+1")
                .artist(artist));

        LastfmAlbum album2 = consistencyHelper.createAndSaveAlbum(builder ->
            builder.name("Album 2")
                .url("https://www.last.fm/music/Test+Artist/Album+2")
                .artist(artist));

        LastfmAlbum album3 = consistencyHelper.createAndSaveAlbum(builder ->
            builder.name("Album 3")
                .url("https://www.last.fm/music/Test+Artist/Album+3")
                .artist(artist));

        // Add album2 to blacklist
        consistencyHelper.addToBlacklist(LastfmEntityType.ALBUM, album2.getUrl());

        // Execute query
        List<LastfmAlbum> result = albumRepository.findAlbumsForGetInfo(10);

        // Verify results - should exclude blacklisted album2
        assertEquals(2, result.size(), "Should return 2 albums (excluding blacklisted)");
        assertTrue(result.stream().anyMatch(a -> "Album 1".equals(a.getName())),
            "Should include Album 1");
        assertTrue(result.stream().anyMatch(a -> "Album 3".equals(a.getName())),
            "Should include Album 3");
        assertFalse(result.stream().anyMatch(a -> "Album 2".equals(a.getName())),
            "Should exclude blacklisted Album 2");
    }

    @Test
    void findAlbumsForGetInfo_shouldNotExcludeAlbumsWithNullUrls() {
        // Create an artist
        LastfmArtist artist = consistencyHelper.createAndSaveArtist(builder ->
            builder.name("Test Artist")
                .listenersCount(5000)
                .approvalStatus(ApprovalStatus.APPROVED));

        // Create album with null URL
        LastfmAlbum albumWithNullUrl = consistencyHelper.createAndSaveAlbum(builder ->
            builder.name("Album With Null URL")
                .url(null)
                .artist(artist));

        // Create album with valid URL and blacklist it
        LastfmAlbum albumWithUrl = consistencyHelper.createAndSaveAlbum(builder ->
            builder.name("Album With URL")
                .url("https://www.last.fm/music/Test+Artist/Album")
                .artist(artist));

        // Add album with URL to blacklist
        consistencyHelper.addToBlacklist(LastfmEntityType.ALBUM, albumWithUrl.getUrl());

        // Execute query
        List<LastfmAlbum> result = albumRepository.findAlbumsForGetInfo(10);

        // Verify results - should include album with null URL, exclude blacklisted album
        assertEquals(1, result.size(), "Should return 1 album");
        assertEquals("Album With Null URL", result.get(0).getName(),
            "Should include album with null URL");
    }
}
