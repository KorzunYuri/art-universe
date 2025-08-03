package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.track.common.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmDataSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmEntityService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.entity.LastfmTrack;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class LastfmTrackApiCallGeneratorTest {

    @Mock
    private LastfmApiCallService apiCallService;

    @Mock
    private LastfmDataSnapshotService snapshotService;

    @Mock
    private LastfmEntityService entityService;

    private TestableLastfmTrackApiCallGenerator generator;

    @Test
    void deduplicateEntitiesForApiCalls_shouldDeduplicateByMbid() {
        // Given: Tracks with duplicate MBIDs
        List<LastfmTrack> tracks = List.of(
            createTrack(1L, "Track1", "duplicate-mbid", ApprovalStatus.PENDING, 1000),
            createTrack(2L, "Track2", "duplicate-mbid", ApprovalStatus.APPROVED, 2000),
            createTrack(3L, "Track3", "unique-mbid", ApprovalStatus.APPROVED, 1500)
        );
        
        generator = new TestableLastfmTrackApiCallGenerator(
            apiCallService, snapshotService, entityService);

        // When
        List<LastfmTrack> result = generator.deduplicateEntitiesForApiCalls(tracks);

        // Then
        assertEquals(2, result.size(), "Should deduplicate tracks with same MBID");
        
        // Should keep the approved track with higher listeners count (track2)
        assertEquals(1, result.stream()
            .mapToLong(t -> "duplicate-mbid".equals(t.getMbid()) ? 1 : 0)
            .sum(), "Should have only one track with duplicate-mbid");
        
        LastfmTrack selectedDuplicate = result.stream()
            .filter(t -> "duplicate-mbid".equals(t.getMbid()))
            .findFirst()
            .orElseThrow();
        
        assertEquals(2L, selectedDuplicate.getId(), "Should select approved track with higher listeners count");
        assertEquals(ApprovalStatus.APPROVED, selectedDuplicate.getApprovalStatus());
        assertEquals(2000, selectedDuplicate.getListenersCount());
    }

    @Test
    void deduplicateEntitiesForApiCalls_shouldDeduplicateByNameAndArtist() {
        // Given: Tracks with same name and artist but no MBID
        LastfmArtist artist = createArtist(10L, "Artist1");
        
        LastfmTrack track1 = createTrack(1L, "Same Track", null, ApprovalStatus.PENDING, 1000);
        track1.setArtist(artist);
        
        LastfmTrack track2 = createTrack(2L, "Same Track", null, ApprovalStatus.APPROVED, 2000);
        track2.setArtist(artist);
        
        LastfmTrack track3 = createTrack(3L, "Different Track", null, ApprovalStatus.PENDING, 1500);
        track3.setArtist(artist);
        
        List<LastfmTrack> tracks = List.of(track1, track2, track3);
        
        generator = new TestableLastfmTrackApiCallGenerator(
            apiCallService, snapshotService, entityService);

        // When
        List<LastfmTrack> result = generator.deduplicateEntitiesForApiCalls(tracks);

        // Then
        assertEquals(2, result.size(), "Should deduplicate tracks with same name and artist");
        
        // Should keep the approved track with higher listeners count (track2)
        LastfmTrack selectedDuplicate = result.stream()
            .filter(t -> "Same Track".equals(t.getName()))
            .findFirst()
            .orElseThrow();
        
        assertEquals(2L, selectedDuplicate.getId(), "Should select approved track with higher listeners count");
    }

    @Test
    void hasHigherPriority_shouldPrioritizeByApprovalStatus() {
        // Given
        generator = new TestableLastfmTrackApiCallGenerator(
            apiCallService, snapshotService, entityService);
            
        LastfmTrack approvedTrack = createTrack(1L, "Track1", "mbid1", ApprovalStatus.APPROVED, 1000);
        LastfmTrack pendingTrack = createTrack(2L, "Track2", "mbid2", ApprovalStatus.PENDING, 2000);
        
        // When & Then
        assertTrue(generator.hasHigherPriority(approvedTrack, pendingTrack), 
            "Approved track should have higher priority than pending track");
        assertFalse(generator.hasHigherPriority(pendingTrack, approvedTrack), 
            "Pending track should not have higher priority than approved track");
    }

    @Test
    void hasHigherPriority_shouldPrioritizeByListenersCount_whenSameApprovalStatus() {
        // Given
        generator = new TestableLastfmTrackApiCallGenerator(
            apiCallService, snapshotService, entityService);
            
        LastfmTrack highListenersTrack = createTrack(1L, "Track1", "mbid1", ApprovalStatus.APPROVED, 2000);
        LastfmTrack lowListenersTrack = createTrack(2L, "Track2", "mbid2", ApprovalStatus.APPROVED, 1000);
        
        // When & Then
        assertTrue(generator.hasHigherPriority(highListenersTrack, lowListenersTrack), 
            "Track with higher listeners count should have higher priority");
        assertFalse(generator.hasHigherPriority(lowListenersTrack, highListenersTrack), 
            "Track with lower listeners count should not have higher priority");
    }

    @Test
    void hasHigherPriority_shouldPrioritizeByArtistListenersCount_whenSameTrackListenersCount() {
        // Given
        generator = new TestableLastfmTrackApiCallGenerator(
            apiCallService, snapshotService, entityService);
            
        LastfmArtist popularArtist = createArtist(1L, "Popular Artist");
        popularArtist.setListenersCount(5000);
        
        LastfmArtist lessPopularArtist = createArtist(2L, "Less Popular Artist");
        lessPopularArtist.setListenersCount(1000);
        
        LastfmTrack trackWithPopularArtist = createTrack(1L, "Track1", "mbid1", ApprovalStatus.APPROVED, 1000);
        trackWithPopularArtist.setArtist(popularArtist);
        
        LastfmTrack trackWithLessPopularArtist = createTrack(2L, "Track2", "mbid2", ApprovalStatus.APPROVED, 1000);
        trackWithLessPopularArtist.setArtist(lessPopularArtist);
        
        // When & Then
        assertTrue(generator.hasHigherPriority(trackWithPopularArtist, trackWithLessPopularArtist), 
            "Track with more popular artist should have higher priority");
        assertFalse(generator.hasHigherPriority(trackWithLessPopularArtist, trackWithPopularArtist), 
            "Track with less popular artist should not have higher priority");
    }

    @Test
    void hasHigherPriority_shouldPrioritizeByIdWhenEverythingElseIsEqual() {
        // Given
        generator = new TestableLastfmTrackApiCallGenerator(
            apiCallService, snapshotService, entityService);
            
        LastfmTrack olderTrack = createTrack(1L, "Track1", "mbid1", ApprovalStatus.APPROVED, 1000);
        LastfmTrack newerTrack = createTrack(2L, "Track2", "mbid2", ApprovalStatus.APPROVED, 1000);
        
        // When & Then
        assertTrue(generator.hasHigherPriority(olderTrack, newerTrack), 
            "Track with lower ID (older) should have higher priority");
        assertFalse(generator.hasHigherPriority(newerTrack, olderTrack), 
            "Track with higher ID (newer) should not have higher priority");
    }

    @Test
    void isValidForApiCall_shouldReturnTrue_whenTrackHasMbid() {
        // Given
        generator = new TestableLastfmTrackApiCallGenerator(
            apiCallService, snapshotService, entityService);
            
        LastfmTrack track = createTrack(1L, "Track1", "mbid1", ApprovalStatus.APPROVED, 1000);
        
        // When & Then
        assertTrue(generator.isValidForApiCall(track), 
            "Track with MBID should be valid for API call");
    }

    @Test
    void isValidForApiCall_shouldReturnTrue_whenTrackHasNameAndArtist() {
        // Given
        generator = new TestableLastfmTrackApiCallGenerator(
            apiCallService, snapshotService, entityService);
            
        LastfmTrack track = createTrack(1L, "Track1", null, ApprovalStatus.APPROVED, 1000);
        LastfmArtist artist = createArtist(1L, "Artist1");
        track.setArtist(artist);
        
        // When & Then
        assertTrue(generator.isValidForApiCall(track), 
            "Track with name and artist should be valid for API call");
    }

    @Test
    void isValidForApiCall_shouldReturnFalse_whenTrackHasNoMbidAndNoArtist() {
        // Given
        generator = new TestableLastfmTrackApiCallGenerator(
            apiCallService, snapshotService, entityService);
            
        LastfmTrack track = createTrack(1L, "Track1", null, ApprovalStatus.APPROVED, 1000);
        
        // When & Then
        assertFalse(generator.isValidForApiCall(track), 
            "Track without MBID and artist should not be valid for API call");
    }

    @Test
    void getApiCallUniqueKey_shouldReturnMbidKey_whenTrackHasMbid() {
        // Given
        generator = new TestableLastfmTrackApiCallGenerator(
            apiCallService, snapshotService, entityService);
            
        LastfmTrack track = createTrack(1L, "Track1", "mbid1", ApprovalStatus.APPROVED, 1000);
        
        // When
        String key = generator.getApiCallUniqueKey(track);
        
        // Then
        assertEquals("mbid-mbid1", key, "Key should be based on MBID");
    }

    @Test
    void getApiCallUniqueKey_shouldReturnNameKey_whenTrackHasNoMbidButHasArtist() {
        // Given
        generator = new TestableLastfmTrackApiCallGenerator(
            apiCallService, snapshotService, entityService);
            
        LastfmTrack track = createTrack(1L, "Track1", null, ApprovalStatus.APPROVED, 1000);
        LastfmArtist artist = createArtist(1L, "Artist1");
        track.setArtist(artist);
        
        // When
        String key = generator.getApiCallUniqueKey(track);
        
        // Then
        assertEquals("names-Artist1-Track1", key, "Key should be based on artist name and track name");
    }

    @Test
    void getApiCallUniqueKey_shouldReturnNull_whenTrackIsInvalid() {
        // Given
        generator = new TestableLastfmTrackApiCallGenerator(
            apiCallService, snapshotService, entityService);
            
        LastfmTrack track = createTrack(1L, "Track1", null, ApprovalStatus.APPROVED, 1000);
        // No artist set, so track is invalid
        
        // When
        String key = generator.getApiCallUniqueKey(track);
        
        // Then
        assertNull(key, "Key should be null for invalid track");
    }

    // open protected methods for testing
    private static class TestableLastfmTrackApiCallGenerator extends LastfmTrackApiCallGenerator {
        public TestableLastfmTrackApiCallGenerator(
            LastfmApiCallService apiCallService,
            LastfmDataSnapshotService snapshotService,
            LastfmEntityService entityService
        ) {
            super(apiCallService, snapshotService, entityService);
        }

        @Override
        public List<LastfmTrack> deduplicateEntitiesForApiCalls(List<LastfmTrack> entities) {
            return super.deduplicateEntitiesForApiCalls(entities);
        }

        @Override
        public List<LastfmTrack> selectEntitiesForApiCalls() {
            return super.selectEntitiesForApiCalls();
        }

        @Override
        public LastfmApiCallType getApiCallType() {
            return LastfmApiCallType.TRACK_GET_INFO;
        }

        @Override
        protected int getDueDurationDays() {
            return 7;
        }
        
        @Override
        public boolean isValidForApiCall(LastfmTrack track) {
            return super.isValidForApiCall(track);
        }
        
        @Override
        public String getApiCallUniqueKey(LastfmTrack track) {
            return super.getApiCallUniqueKey(track);
        }
        
        @Override
        public boolean hasHigherPriority(LastfmTrack candidate, LastfmTrack existing) {
            return super.hasHigherPriority(candidate, existing);
        }
    }

    private LastfmTrack createTrack(Long id, String name, String mbid, ApprovalStatus status, Integer listenersCount) {
        // Create a mock API call for the required field
        LastfmApiCall mockApiCall = LastfmApiCall.builder()
            .id(1L)
            .type(LastfmApiCallType.TRACK_GET_INFO)
            .dataSnapshotId(1L)
            .dueDttm(java.time.Instant.now())
            .params(Map.of())
            .build();
            
        LastfmTrack track = LastfmTrack.builder()
            .name(name)
            .mbid(mbid)
            .url(UUID.randomUUID().toString())
            .approvalStatus(status)
            .listenersCount(listenersCount)
            .apiCall(mockApiCall)
            .build();
        
        ReflectionTestUtils.setField(track, "id", id);
        return track;
    }
    
    private LastfmArtist createArtist(Long id, String name) {
        // Create a mock API call for the required field
        LastfmApiCall mockApiCall = LastfmApiCall.builder()
            .id(1L)
            .type(LastfmApiCallType.ARTIST_GET_INFO)
            .dataSnapshotId(1L)
            .dueDttm(java.time.Instant.now())
            .params(Map.of())
            .build();
            
        LastfmArtist artist = LastfmArtist.builder()
            .name(name)
            .approvalStatus(ApprovalStatus.APPROVED)
            .apiCall(mockApiCall)
            .build();
        
        ReflectionTestUtils.setField(artist, "id", id);
        return artist;
    }
}
