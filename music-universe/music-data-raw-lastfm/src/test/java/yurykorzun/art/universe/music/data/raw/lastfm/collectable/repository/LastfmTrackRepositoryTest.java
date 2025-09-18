package yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaOnlyTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
class LastfmTrackRepositoryTest extends JpaOnlyTest {

    @Autowired
    private LastfmTrackRepository trackRepository;

    @Autowired
    private DbConsistencyHelper consistencyHelper;

    @Autowired
    private EntityManager entityManager;

    private LastfmArtist artist;
    private List<LastfmTrack> testTracks;

    @BeforeEach
    void setUp() {
        artist = consistencyHelper.createAndSaveArtist(builder -> builder.approvalStatus(ApprovalStatus.APPROVED));
        
        // Create test tracks with different values for sorting tests
        LastfmApiCall apiCall = consistencyHelper.createAndSaveApiCall();
        
        testTracks = new ArrayList<>();
        
        // Track with all values set
        testTracks.add(LastfmTrack.builder()
            .name("Track A")
            .url("https://example.com/track-a")
            .mbid("mbid-a")
            .playCount(1000L)
            .listenersCount(500)
            .artist(artist)
            .approvalStatus(ApprovalStatus.APPROVED)
            .apiCall(apiCall)
            .build());
            
        // Track with null playCount
        testTracks.add(LastfmTrack.builder()
            .name("Track B")
            .url("https://example.com/track-b")
            .mbid("mbid-b")
            .playCount(null)
            .listenersCount(800)
            .artist(artist)
            .approvalStatus(ApprovalStatus.APPROVED)
            .apiCall(apiCall)
            .build());
            
        // Track with null listenersCount
        testTracks.add(LastfmTrack.builder()
            .name("Track C")
            .url("https://example.com/track-c")
            .mbid("mbid-c")
            .playCount(1500L)
            .listenersCount(null)
            .artist(artist)
            .approvalStatus(ApprovalStatus.APPROVED)
            .apiCall(apiCall)
            .build());
            
        // Track with both null
        testTracks.add(LastfmTrack.builder()
            .name("Track D")
            .url("https://example.com/track-d")
            .mbid("mbid-d")
            .playCount(null)
            .listenersCount(null)
            .artist(artist)
            .approvalStatus(ApprovalStatus.APPROVED)
            .apiCall(apiCall)
            .build());
            
        // Save all test tracks
        trackRepository.saveAll(testTracks);
    }
    
    @AfterEach
    void tearDown() {
        consistencyHelper.cleanup();
    }

    private LastfmTrack createTrack() {
        return consistencyHelper.createAndSaveTrack();
    }

    @Test
    void save_shouldSaveTrack_whenValidDataProvided() {
        final String name = "Smells Like Teen Spirit";
        final int duration = 301;
        final String url = "https://www.last.fm/music/Nirvana/_/Smells+Like+Teen+Spirit";
        final boolean streamable = true;
        final String mbid = "0ebe2d92-a11d-4b2b-9922-806383074ed7";

        LastfmApiCall apiCall = consistencyHelper.createAndSaveApiCall();
        LastfmTrack track = LastfmTrack.builder()
                .name(name)
                .url(url)
                .mbid(mbid)
                .duration(duration)
                .apiCall(apiCall)
            .build();
        LastfmTrack saved = trackRepository.save(track);

        assertEquals(name, saved.getName());
        assertEquals(url, saved.getUrl());
        assertEquals(mbid, saved.getMbid());
        assertEquals(duration, saved.getDuration());
    }

    @Test
    void saveAll_shouldSaveAllTracks_whenValidDataProvided() {
        LastfmTrack track1 = createTrack();
        LastfmTrack track2 = createTrack();

        List<LastfmTrack> firstSaveResult = trackRepository.saveAll(List.of(track1, track2));
        assertEquals(6, trackRepository.findAll().size()); // 4 from setup + 2 new ones
        LastfmTrack track1After1stSave = firstSaveResult.stream()
                .filter(a -> a.getName().equals(track1.getName()))
                .findFirst().get();

        LastfmTrack track3 = createTrack();

        List<LastfmTrack> secondSaveResult = trackRepository.saveAll(List.of(track1, track3));
        assertEquals(7, trackRepository.findAll().size()); // 6 previous + 1 new
        LastfmTrack track1After2ndSave = secondSaveResult.stream()
                .filter(a -> a.getName().equals(track1.getName()))
                .findFirst().get();
        assertEquals(track1After2ndSave.getId(), track1After1stSave.getId());
    }

    @Test
    void testSortByListenersCount_shouldPlaceNullsLast() {
        // Sort by listenersCount in descending order
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "listenersCount"));
        
        // Execute query
        Page<LastfmTrack> result = trackRepository.findTracks(
            null, null, null, null, Collections.emptyList(), null, pageable);
            
        // Verify results
        List<LastfmTrack> sortedTracks = result.getContent();
        assertEquals(4, sortedTracks.size());
        
        // Track B (800) should be first, then Track A (500), then Track C and D (null)
        assertEquals("Track B", sortedTracks.get(0).getName());
        assertEquals("Track A", sortedTracks.get(1).getName());
        
        // The last two should have null listenersCount
        assertNull(sortedTracks.get(2).getListenersCount());
        assertNull(sortedTracks.get(3).getListenersCount());
    }
    
    @Test
    void testSortByPlayCount_shouldPlaceNullsLast() {
        // Sort by playCount in descending order
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "playCount"));
        
        // Execute query
        Page<LastfmTrack> result = trackRepository.findTracks(
            null, null, null, null, Collections.emptyList(), null, pageable);
            
        // Verify results
        List<LastfmTrack> sortedTracks = result.getContent();
        assertEquals(4, sortedTracks.size());
        
        // Track C (1500) should be first, then Track A (1000), then Track B and D (null)
        assertEquals("Track C", sortedTracks.get(0).getName());
        assertEquals("Track A", sortedTracks.get(1).getName());
        
        // The last two should have null playCount
        assertNull(sortedTracks.get(2).getPlayCount());
        assertNull(sortedTracks.get(3).getPlayCount());
    }
    
    @Test
    void testSortByName_shouldSortAlphabetically() {
        // Sort by name in ascending order
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"));
        
        // Execute query
        Page<LastfmTrack> result = trackRepository.findTracks(
            null, null, null, null, Collections.emptyList(), null, pageable);
            
        // Verify results
        List<LastfmTrack> sortedTracks = result.getContent();
        assertEquals(4, sortedTracks.size());
        
        // Tracks should be in alphabetical order: A, B, C, D
        assertEquals("Track A", sortedTracks.get(0).getName());
        assertEquals("Track B", sortedTracks.get(1).getName());
        assertEquals("Track C", sortedTracks.get(2).getName());
        assertEquals("Track D", sortedTracks.get(3).getName());
    }
    
    @Test
    void testSortByNameDescending_shouldSortReverseAlphabetically() {
        // Sort by name in descending order
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "name"));
        
        // Execute query
        Page<LastfmTrack> result = trackRepository.findTracks(
            null, null, null, null, Collections.emptyList(), null, pageable);
            
        // Verify results
        List<LastfmTrack> sortedTracks = result.getContent();
        assertEquals(4, sortedTracks.size());
        
        // Tracks should be in reverse alphabetical order: D, C, B, A
        assertEquals("Track D", sortedTracks.get(0).getName());
        assertEquals("Track C", sortedTracks.get(1).getName());
        assertEquals("Track B", sortedTracks.get(2).getName());
        assertEquals("Track A", sortedTracks.get(3).getName());
    }

    @Test
    void findTracksForGetInfo_shouldExcludeBlacklistedTracks() {
        // Create an artist
        LastfmArtist artist = consistencyHelper.createAndSaveArtist(builder ->
            builder.name("Test Artist")
                   .listenersCount(5000)
                   .approvalStatus(ApprovalStatus.APPROVED));

        // Create tracks
        LastfmTrack track1 = consistencyHelper.createAndSaveTrack(builder -> 
            builder.name("Track 1")
                   .url("https://www.last.fm/music/Test+Artist/_/Track+1")
                   .artist(artist));
        
        LastfmTrack track2 = consistencyHelper.createAndSaveTrack(builder -> 
            builder.name("Track 2")
                   .url("https://www.last.fm/music/Test+Artist/_/Track+2")
                   .artist(artist));
        
        LastfmTrack track3 = consistencyHelper.createAndSaveTrack(builder -> 
            builder.name("Track 3")
                   .url("https://www.last.fm/music/Test+Artist/_/Track+3")
                   .artist(artist));

        // Add track2 to blacklist
        consistencyHelper.addToBlacklist(LastfmEntityType.TRACK, track2.getUrl());

        // make sure changes have been applied
        consistencyHelper.flush();

        // Execute query
        List<LastfmTrack> result = trackRepository.findTracksForGetInfo(10);

        // Verify results - should exclude blacklisted track2
        assertEquals(5, result.size(), "Should return 5 tracks total (3 from setup + 2 from test, excluding blacklisted)");
        assertTrue(result.stream().anyMatch(t -> "Track 1".equals(t.getName())), 
                  "Should include Track 1");
        assertTrue(result.stream().anyMatch(t -> "Track 3".equals(t.getName())), 
                  "Should include Track 3");
        assertFalse(result.stream().anyMatch(t -> "Track 2".equals(t.getName())), 
                   "Should exclude blacklisted Track 2");
    }

    @Test
    void findTracksForGetInfo_shouldNotExcludeTracksWithEmptyUrls() {
        // Create an artist
        LastfmArtist artist = consistencyHelper.createAndSaveArtist(builder -> 
            builder.name("Test Artist")
                   .listenersCount(5000)
                   .approvalStatus(ApprovalStatus.APPROVED));

        // Create track with null URL
        LastfmTrack trackWithNullUrl = consistencyHelper.createAndSaveTrack(builder -> 
            builder.name("Track With Empty URL")
                   .url("")
                   .artist(artist));
        
        // Create track with valid URL and blacklist it
        LastfmTrack trackWithUrl = consistencyHelper.createAndSaveTrack(builder -> 
            builder.name("Track With URL")
                   .url("https://www.last.fm/music/Test+Artist/_/Track")
                   .artist(artist));

        // Add track with URL to blacklist
        consistencyHelper.addToBlacklist(LastfmEntityType.TRACK, trackWithUrl.getUrl());

        // make sure changes have been applied
        consistencyHelper.flush();

        // Execute query
        List<LastfmTrack> result = trackRepository.findTracksForGetInfo(10);

        // Verify results - should include track with null URL, exclude blacklisted track
        assertEquals(4, result.size(), "Should return 4 tracks total (3 from setup + 1 from test, excluding blacklisted)");
        assertTrue(result.stream().anyMatch(t -> "Track With Empty URL".equals(t.getName())),
                    "Should include track with empty URL");
    }

    @Test
    void updateTrackStatusByArtistId_shouldUpdatePendingTracksOnly() {
        // Given
        LastfmArtist artist = consistencyHelper.createAndSaveArtist();
        
        LastfmTrack pendingTrack1 = consistencyHelper.createAndSaveTrack(builder -> 
            builder.name("Pending Track 1")
                   .url("http://test1.com")
                   .artist(artist)
                   .approvalStatus(ApprovalStatus.PENDING));
                   
        LastfmTrack pendingTrack2 = consistencyHelper.createAndSaveTrack(builder -> 
            builder.name("Pending Track 2")
                   .url("http://test2.com")
                   .artist(artist)
                   .approvalStatus(ApprovalStatus.PENDING));
                   
        LastfmTrack approvedTrack = consistencyHelper.createAndSaveTrack(builder -> 
            builder.name("Approved Track")
                   .url("http://test3.com")
                   .artist(artist)
                   .approvalStatus(ApprovalStatus.APPROVED));
                   
        // Different artist's track
        LastfmArtist otherArtist = consistencyHelper.createAndSaveArtist();
        LastfmTrack otherArtistTrack = consistencyHelper.createAndSaveTrack(builder -> 
            builder.name("Other Artist Track")
                   .url("http://test4.com")
                   .artist(otherArtist)
                   .approvalStatus(ApprovalStatus.PENDING));

        // make sure changes have been applied
        entityManager.flush();

        // When
        int updatedCount = trackRepository.updateTrackStatusByArtistId(artist.getId(), ApprovalStatus.DECLINED);

        // Clear entity manager cache to get fresh data from DB
        entityManager.flush();
        entityManager.clear();

        // Then
        assertEquals(2, updatedCount);
        
        // Verify status changes
        LastfmTrack updatedTrack1 = trackRepository.findById(pendingTrack1.getId()).orElseThrow();
        LastfmTrack updatedTrack2 = trackRepository.findById(pendingTrack2.getId()).orElseThrow();
        LastfmTrack unchangedApproved = trackRepository.findById(approvedTrack.getId()).orElseThrow();
        LastfmTrack unchangedOther = trackRepository.findById(otherArtistTrack.getId()).orElseThrow();
        
        assertEquals(ApprovalStatus.DECLINED, updatedTrack1.getApprovalStatus());
        assertEquals(ApprovalStatus.DECLINED, updatedTrack2.getApprovalStatus());
        assertEquals(ApprovalStatus.APPROVED, unchangedApproved.getApprovalStatus());
        assertEquals(ApprovalStatus.PENDING, unchangedOther.getApprovalStatus());
    }

    @Test
    void updateTrackStatusByAlbumId_shouldUpdatePendingTracksOnly() {
        // Given
        LastfmArtist artist = consistencyHelper.createAndSaveArtist();
        LastfmAlbum album = consistencyHelper.createAndSaveAlbum(builder -> 
            builder.name("Test Album").url("http://album.com").artist(artist));
        
        LastfmTrack pendingTrack1 = consistencyHelper.createAndSaveTrack(builder -> 
            builder.name("Pending Track 1")
                   .url("http://test1.com")
                   .artist(artist)
                   .approvalStatus(ApprovalStatus.PENDING));
                   
        LastfmTrack pendingTrack2 = consistencyHelper.createAndSaveTrack(builder -> 
            builder.name("Pending Track 2")
                   .url("http://test2.com")
                   .artist(artist)
                   .approvalStatus(ApprovalStatus.PENDING));
                   
        LastfmTrack approvedTrack = consistencyHelper.createAndSaveTrack(builder -> 
            builder.name("Approved Track")
                   .url("http://test3.com")
                   .artist(artist)
                   .approvalStatus(ApprovalStatus.APPROVED));
                   
        // Track not in album
        LastfmTrack trackNotInAlbum = consistencyHelper.createAndSaveTrack(builder -> 
            builder.name("Track Not In Album")
                   .url("http://test4.com")
                   .artist(artist)
                   .approvalStatus(ApprovalStatus.PENDING));

        // Create album-track relationships
        consistencyHelper.createAndSaveAlbumTrack(album, pendingTrack1);
        consistencyHelper.createAndSaveAlbumTrack(album, pendingTrack2);
        consistencyHelper.createAndSaveAlbumTrack(album, approvedTrack);

        // make sure changes have been applied
        entityManager.flush();

        // When
        int updatedCount = trackRepository.updateTrackStatusByAlbumId(album.getId(), ApprovalStatus.IGNORED);

        // Clear entity manager cache to get fresh data from DB
        entityManager.flush();
        entityManager.clear();

        // Then
        assertEquals(2, updatedCount);
        
        // Verify status changes
        LastfmTrack updatedTrack1 = trackRepository.findById(pendingTrack1.getId()).orElseThrow();
        LastfmTrack updatedTrack2 = trackRepository.findById(pendingTrack2.getId()).orElseThrow();
        LastfmTrack unchangedApproved = trackRepository.findById(approvedTrack.getId()).orElseThrow();
        LastfmTrack unchangedNotInAlbum = trackRepository.findById(trackNotInAlbum.getId()).orElseThrow();
        
        assertEquals(ApprovalStatus.IGNORED, updatedTrack1.getApprovalStatus());
        assertEquals(ApprovalStatus.IGNORED, updatedTrack2.getApprovalStatus());
        assertEquals(ApprovalStatus.APPROVED, unchangedApproved.getApprovalStatus());
        assertEquals(ApprovalStatus.PENDING, unchangedNotInAlbum.getApprovalStatus());
    }
}
