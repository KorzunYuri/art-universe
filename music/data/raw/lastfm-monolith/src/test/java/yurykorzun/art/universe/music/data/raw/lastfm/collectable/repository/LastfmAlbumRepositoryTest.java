package yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaTestWithHelper;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
class LastfmAlbumRepositoryTest extends JpaTestWithHelper {

    @Autowired
    private LastfmAlbumRepository albumRepository;

    @Autowired
    private DbConsistencyHelper consistencyHelper;

    @Autowired
    private EntityManager entityManager;
    
    @BeforeEach
    void setUp() {
        // Clean up before each test to ensure consistent state
        consistencyHelper.cleanup();
    }
    
    @AfterEach
    void tearDown() {
        consistencyHelper.cleanup();
    }

    @Test
    void save_shouldSaveAlbum_whenValidDataProvided() {
        final String name = "album";
        final String description = "description";
        final String url = "url";
        final String mbid = "mbid";
        final long playCount = 1;
        final int listenersCount = 1;
        final LocalDateTime publishTs = LocalDateTime.now();

        LastfmApiCall apiCall = consistencyHelper.createAndSaveApiCall();
        LastfmAlbum album = LastfmAlbum.builder()
                .apiCall(apiCall)
                .name(name)
                .description(description)
                .url(url)
                .mbid(mbid)
                .playCount(playCount)
                .listenersCount(listenersCount)
                .publishTs(publishTs)
            .build();

        LastfmAlbum saved = albumRepository.save(album);

        assertEquals(name, saved.getName());
        assertEquals(description, saved.getDescription());
        assertEquals(url, saved.getUrl());
        assertEquals(mbid, saved.getMbid());
        assertEquals(playCount, saved.getPlayCount());
        assertEquals(listenersCount, saved.getListenersCount());
        assertEquals(publishTs, saved.getPublishTs());
    }

    @Test
    void save_shouldSaveAlbums_whenValidDataProvided() {
        LastfmAlbum album1 = consistencyHelper.createAlbumForPersistence();
        LastfmAlbum album2 = consistencyHelper.createAlbumForPersistence();

        List<LastfmAlbum> firstSaveResult = albumRepository.saveAll(List.of(album1, album2));
        assertEquals(2, firstSaveResult.size());
        assertEquals(2, albumRepository.findAll().size());
        LastfmAlbum album1after1stSave = firstSaveResult.stream()
            .filter(a -> album1.getName().equals(a.getName()))
            .findFirst().get();

        LastfmAlbum album3 = consistencyHelper.createAlbumForPersistence();
        List<LastfmAlbum> secondSaveResult = albumRepository.saveAll(List.of(album1, album3));
        assertEquals(2, secondSaveResult.size());
        assertEquals(3, albumRepository.findAll().size());
        LastfmAlbum album1after2ndSave = firstSaveResult.stream()
            .filter(a -> album1.getName().equals(a.getName()))
            .findFirst().get();
        assertEquals(album1after1stSave.getId(), album1after2ndSave.getId());
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

    @Test
    void updateAlbumStatusByArtistId_shouldUpdatePendingAlbumsOnly() {
        // Given
        LastfmArtist artist = consistencyHelper.createAndSaveArtist();
        
        LastfmAlbum pendingAlbum1 = consistencyHelper.createAndSaveAlbum(builder -> 
            builder.name("Pending Album 1")
                   .url("http://test1.com")
                   .artist(artist)
                   .approvalStatus(ApprovalStatus.PENDING));
                   
        LastfmAlbum pendingAlbum2 = consistencyHelper.createAndSaveAlbum(builder -> 
            builder.name("Pending Album 2")
                   .url("http://test2.com")
                   .artist(artist)
                   .approvalStatus(ApprovalStatus.PENDING));
                   
        LastfmAlbum approvedAlbum = consistencyHelper.createAndSaveAlbum(builder -> 
            builder.name("Approved Album")
                   .url("http://test3.com")
                   .artist(artist)
                   .approvalStatus(ApprovalStatus.APPROVED));
                   
        // Different artist's album
        LastfmArtist otherArtist = consistencyHelper.createAndSaveArtist();
        LastfmAlbum otherArtistAlbum = consistencyHelper.createAndSaveAlbum(builder -> 
            builder.name("Other Artist Album")
                   .url("http://test4.com")
                   .artist(otherArtist)
                   .approvalStatus(ApprovalStatus.PENDING));

        // make sure changes have been applied
        entityManager.flush();

        // When
        int updatedCount = albumRepository.updateAlbumStatusByArtistId(artist.getId(), ApprovalStatus.DECLINED);

        // Clear entity manager cache to get fresh data from DB
        entityManager.flush();
        entityManager.clear();

        // Then
        assertEquals(2, updatedCount);
        
        // Verify status changes
        LastfmAlbum updatedAlbum1 = albumRepository.findById(pendingAlbum1.getId()).orElseThrow();
        LastfmAlbum updatedAlbum2 = albumRepository.findById(pendingAlbum2.getId()).orElseThrow();
        LastfmAlbum unchangedApproved = albumRepository.findById(approvedAlbum.getId()).orElseThrow();
        LastfmAlbum unchangedOther = albumRepository.findById(otherArtistAlbum.getId()).orElseThrow();
        
        assertEquals(ApprovalStatus.DECLINED, updatedAlbum1.getApprovalStatus());
        assertEquals(ApprovalStatus.DECLINED, updatedAlbum2.getApprovalStatus());
        assertEquals(ApprovalStatus.APPROVED, unchangedApproved.getApprovalStatus());
        assertEquals(ApprovalStatus.PENDING, unchangedOther.getApprovalStatus());
    }
}
