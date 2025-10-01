package yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmTrack;
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
