package yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate.generator.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import yurykorzun.art.universe.data.raw.common.domain.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.LastfmDataSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate.LastfmApiCallEntityService;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class LastfmArtistApiCallGeneratorTest {

    @Mock
    private LastfmApiCallService apiCallService;

    @Mock
    private LastfmDataSnapshotService snapshotService;

    @Mock
    private LastfmApiCallEntityService entityService;

    private TestableLastfmArtistApiCallGenerator generator;

    @Test
    void deduplicateEntitiesForApiCalls_shouldDeduplicateByMbid() {
        // Given: Artists with duplicate MBIDs
        List<LastfmArtist> artists = List.of(
            createArtist(1L, "Artist1", "duplicate-mbid", ApprovalStatus.PENDING, 1000),
            createArtist(2L, "Artist2", "duplicate-mbid", ApprovalStatus.APPROVED, 2000),
            createArtist(3L, "Artist3", "unique-mbid", ApprovalStatus.APPROVED, 1500)
        );
        
        generator = new TestableLastfmArtistApiCallGenerator(
            apiCallService, snapshotService, entityService);

        // When
        List<LastfmArtist> result = generator.deduplicateEntitiesForApiCalls(artists);

        // Then
        assertEquals(2, result.size(), "Should deduplicate artists with same MBID");
        
        // Should keep the approved artist with higher listeners count (artist2)
        assertEquals(1, result.stream()
            .mapToLong(a -> "duplicate-mbid".equals(a.getMbid()) ? 1 : 0)
            .sum(), "Should have only one artist with duplicate-mbid");
        
        LastfmArtist selectedDuplicate = result.stream()
            .filter(a -> "duplicate-mbid".equals(a.getMbid()))
            .findFirst()
            .orElseThrow();
        
        assertEquals(2L, selectedDuplicate.getId(), "Should select approved artist with higher listeners count");
        assertEquals(ApprovalStatus.APPROVED, selectedDuplicate.getApprovalStatus());
        assertEquals(2000, selectedDuplicate.getListenersCount());
    }

    @Test
    void deduplicateEntitiesForApiCalls_shouldDeduplicateByName() {
        // Given: Artists with same name but no MBID
        List<LastfmArtist> artists = List.of(
            createArtist(1L, "Same Artist", null, ApprovalStatus.PENDING, 1000),
            createArtist(2L, "Same Artist", null, ApprovalStatus.APPROVED, 2000),
            createArtist(3L, "Different Artist", null, ApprovalStatus.PENDING, 1500)
        );
        
        generator = new TestableLastfmArtistApiCallGenerator(
            apiCallService, snapshotService, entityService);

        // When
        List<LastfmArtist> result = generator.deduplicateEntitiesForApiCalls(artists);

        // Then
        assertEquals(2, result.size(), "Should deduplicate artists with same name");
        
        // Should keep the approved artist with higher listeners count (artist2)
        LastfmArtist selectedDuplicate = result.stream()
            .filter(a -> "Same Artist".equals(a.getName()))
            .findFirst()
            .orElseThrow();
        
        assertEquals(2L, selectedDuplicate.getId(), "Should select approved artist with higher listeners count");
    }

    @Test
    void hasHigherPriority_shouldPrioritizeByApprovalStatus() {
        // Given
        generator = new TestableLastfmArtistApiCallGenerator(
            apiCallService, snapshotService, entityService);
            
        LastfmArtist approvedArtist = createArtist(1L, "Artist1", "mbid1", ApprovalStatus.APPROVED, 1000);
        LastfmArtist pendingArtist = createArtist(2L, "Artist2", "mbid2", ApprovalStatus.PENDING, 2000);
        
        // When & Then
        assertTrue(generator.hasHigherPriority(approvedArtist, pendingArtist), 
            "Approved artist should have higher priority than pending artist");
        assertFalse(generator.hasHigherPriority(pendingArtist, approvedArtist), 
            "Pending artist should not have higher priority than approved artist");
    }

    @Test
    void hasHigherPriority_shouldPrioritizeByListenersCount_whenSameApprovalStatus() {
        // Given
        generator = new TestableLastfmArtistApiCallGenerator(
            apiCallService, snapshotService, entityService);
            
        LastfmArtist highListenersArtist = createArtist(1L, "Artist1", "mbid1", ApprovalStatus.APPROVED, 2000);
        LastfmArtist lowListenersArtist = createArtist(2L, "Artist2", "mbid2", ApprovalStatus.APPROVED, 1000);
        
        // When & Then
        assertTrue(generator.hasHigherPriority(highListenersArtist, lowListenersArtist), 
            "Artist with higher listeners count should have higher priority");
        assertFalse(generator.hasHigherPriority(lowListenersArtist, highListenersArtist), 
            "Artist with lower listeners count should not have higher priority");
    }

    @Test
    void hasHigherPriority_shouldPrioritizeByIdWhenEverythingElseIsEqual() {
        // Given
        generator = new TestableLastfmArtistApiCallGenerator(
            apiCallService, snapshotService, entityService);
            
        LastfmArtist olderArtist = createArtist(1L, "Artist1", "mbid1", ApprovalStatus.APPROVED, 1000);
        LastfmArtist newerArtist = createArtist(2L, "Artist2", "mbid2", ApprovalStatus.APPROVED, 1000);
        
        // When & Then
        assertTrue(generator.hasHigherPriority(olderArtist, newerArtist), 
            "Artist with lower ID (older) should have higher priority");
        assertFalse(generator.hasHigherPriority(newerArtist, olderArtist), 
            "Artist with higher ID (newer) should not have higher priority");
    }

    @Test
    void isValidForApiCall_shouldReturnTrue_whenArtistHasMbid() {
        // Given
        generator = new TestableLastfmArtistApiCallGenerator(
            apiCallService, snapshotService, entityService);
            
        LastfmArtist artist = createArtist(1L, "Artist1", "mbid1", ApprovalStatus.APPROVED, 1000);
        
        // When & Then
        assertTrue(generator.isValidForApiCall(artist), 
            "Artist with MBID should be valid for API call");
    }

    @Test
    void isValidForApiCall_shouldReturnTrue_whenArtistHasName() {
        // Given
        generator = new TestableLastfmArtistApiCallGenerator(
            apiCallService, snapshotService, entityService);
            
        LastfmArtist artist = createArtist(1L, "Artist1", null, ApprovalStatus.APPROVED, 1000);
        
        // When & Then
        assertTrue(generator.isValidForApiCall(artist), 
            "Artist with name should be valid for API call");
    }

    @Test
    void isValidForApiCall_shouldReturnFalse_whenArtistHasNoMbidAndNoName() {
        // Given
        generator = new TestableLastfmArtistApiCallGenerator(
            apiCallService, snapshotService, entityService);
            
        LastfmArtist artist = createArtist(1L, null, null, ApprovalStatus.APPROVED, 1000);
        
        // When & Then
        assertFalse(generator.isValidForApiCall(artist), 
            "Artist without MBID and name should not be valid for API call");
    }

    @Test
    void getApiCallUniqueKey_shouldReturnMbidKey_whenArtistHasMbid() {
        // Given
        generator = new TestableLastfmArtistApiCallGenerator(
            apiCallService, snapshotService, entityService);
            
        LastfmArtist artist = createArtist(1L, "Artist1", "mbid1", ApprovalStatus.APPROVED, 1000);
        
        // When
        String key = generator.getApiCallUniqueKey(artist);
        
        // Then
        assertEquals("mbid-mbid1", key, "Key should be based on MBID");
    }

    @Test
    void getApiCallUniqueKey_shouldReturnNameKey_whenArtistHasNoMbid() {
        // Given
        generator = new TestableLastfmArtistApiCallGenerator(
            apiCallService, snapshotService, entityService);
            
        LastfmArtist artist = createArtist(1L, "Artist1", null, ApprovalStatus.APPROVED, 1000);
        
        // When
        String key = generator.getApiCallUniqueKey(artist);
        
        // Then
        assertEquals("names-Artist1", key, "Key should be based on artist name");
    }

    @Test
    void getApiCallUniqueKey_shouldReturnNull_whenArtistIsInvalid() {
        // Given
        generator = new TestableLastfmArtistApiCallGenerator(
            apiCallService, snapshotService, entityService);
            
        LastfmArtist artist = createArtist(1L, null, null, ApprovalStatus.APPROVED, 1000);
        
        // When
        String key = generator.getApiCallUniqueKey(artist);
        
        // Then
        assertNull(key, "Key should be null for invalid artist");
    }

    // Тестируемый класс, который открывает protected методы
    private static class TestableLastfmArtistApiCallGenerator extends LastfmArtistApiCallGenerator {
        public TestableLastfmArtistApiCallGenerator(
            LastfmApiCallService apiCallService,
            LastfmDataSnapshotService snapshotService,
            LastfmApiCallEntityService entityService
        ) {
            super(apiCallService, snapshotService, entityService);
        }

        @Override
        public List<LastfmArtist> deduplicateEntitiesForApiCalls(List<LastfmArtist> entities) {
            return super.deduplicateEntitiesForApiCalls(entities);
        }

        @Override
        public LastfmApiCallType getApiCallType() {
            return LastfmApiCallType.ARTIST_GET_INFO;
        }

        @Override
        protected int getDueDurationDays() {
            return 7;
        }
        
        @Override
        public boolean isValidForApiCall(LastfmArtist artist) {
            return super.isValidForApiCall(artist);
        }
        
        @Override
        public String getApiCallUniqueKey(LastfmArtist artist) {
            return super.getApiCallUniqueKey(artist);
        }
        
        @Override
        public boolean hasHigherPriority(LastfmArtist candidate, LastfmArtist existing) {
            return super.hasHigherPriority(candidate, existing);
        }
    }

    private LastfmArtist createArtist(Long id, String name, String mbid, ApprovalStatus status, Integer listenersCount) {
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
            .mbid(mbid)
            .approvalStatus(status)
            .listenersCount(listenersCount)
            .apiCall(mockApiCall)
            .build();
        
        ReflectionTestUtils.setField(artist, "id", id);
        return artist;
    }
}
