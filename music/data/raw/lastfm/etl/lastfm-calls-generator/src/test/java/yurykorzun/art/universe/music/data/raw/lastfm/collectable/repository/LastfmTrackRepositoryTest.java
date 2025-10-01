package yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaTestWithHelper;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
class LastfmTrackRepositoryTest extends JpaTestWithHelper {

    @Autowired
    private LastfmTrackRepository trackRepository;

    @Autowired
    private DbConsistencyHelper consistencyHelper;

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

    @Test
    void findTracksForGetInfo_shouldReturnTracks() {
        // given
        consistencyHelper.createAndSaveTrack();
        consistencyHelper.createAndSaveTrack();

        // when
        List<LastfmTrack> result = trackRepository.findTracksForGetInfo();

        // then
        assertNotNull(result);
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

}
