package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.getsimilar.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.BaseLastfmEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmDataSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmEntityQueryConfig;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmEntityService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LastfmArtistGetSimilarApiCallGeneratorTest {

    @Mock
    private LastfmApiCallService apiCallService;

    @Mock
    private LastfmDataSnapshotService snapshotService;

    @Mock
    private LastfmEntityService entityService;

    private TestableLastfmArtistGetSimilarApiCallGenerator generator;

    @Test
    void selectEntitiesForApiCalls_shouldDeduplicateByMbid() {
        // Given: Mock entity service returns artists with duplicate MBIDs
        List<BaseLastfmEntity> artistsFromService = List.of(
            createArtist(1L, "Artist1", "duplicate-mbid", ApprovalStatus.PENDING, 1000),
            createArtist(2L, "Artist2", "duplicate-mbid", ApprovalStatus.APPROVED, 2000),
            createArtist(3L, "Artist3", "unique-mbid", ApprovalStatus.APPROVED, 1500)
        );
        
        when(entityService.findAllUnprocessed(
            eq(LastfmEntityType.ARTIST), 
            eq(LastfmApiCallType.ARTIST_GET_SIMILAR), 
            any(LastfmEntityQueryConfig.class)
        )).thenReturn(artistsFromService);

        generator = new TestableLastfmArtistGetSimilarApiCallGenerator(
            apiCallService, snapshotService, entityService);

        // When
        List<LastfmArtist> result = generator.selectEntitiesForApiCalls();

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
        
        // Should keep the unique-mbid artist
        assertEquals(1, result.stream()
            .mapToLong(a -> "unique-mbid".equals(a.getMbid()) ? 1 : 0)
            .sum(), "Should keep artist with unique MBID");
    }

    @Test
    void selectEntitiesForApiCalls_shouldHandleAllSameMbid() {
        // Given: All artists have the same MBID
        List<BaseLastfmEntity> artistsFromService = List.of(
            createArtist(1L, "Artist1", "same-mbid", ApprovalStatus.PENDING, 1000),
            createArtist(2L, "Artist2", "same-mbid", ApprovalStatus.PENDING, 2000),
            createArtist(3L, "Artist3", "same-mbid", ApprovalStatus.APPROVED, 500)
        );
        
        when(entityService.findAllUnprocessed(
            eq(LastfmEntityType.ARTIST), 
            eq(LastfmApiCallType.ARTIST_GET_SIMILAR), 
            any(LastfmEntityQueryConfig.class)
        )).thenReturn(artistsFromService);

        generator = new TestableLastfmArtistGetSimilarApiCallGenerator(
            apiCallService, snapshotService, entityService);

        // When
        List<LastfmArtist> result = generator.selectEntitiesForApiCalls();

        // Then
        assertEquals(1, result.size(), "Should return only one artist when all have same MBID");
        assertEquals(3L, result.get(0).getId(), "Should select approved artist despite lower listeners count");
        assertEquals(ApprovalStatus.APPROVED, result.get(0).getApprovalStatus());
    }

    @Test
    void selectEntitiesForApiCalls_shouldHandleNullMbids() {
        // Given: Artists with null MBIDs (should not be deduplicated)
        List<BaseLastfmEntity> artistsFromService = List.of(
            createArtist(1L, "Artist1", null, ApprovalStatus.APPROVED, 1000),
            createArtist(2L, "Artist2", null, ApprovalStatus.APPROVED, 2000),
            createArtist(3L, "Artist3", "real-mbid", ApprovalStatus.APPROVED, 1500)
        );
        
        when(entityService.findAllUnprocessed(
            eq(LastfmEntityType.ARTIST), 
            eq(LastfmApiCallType.ARTIST_GET_SIMILAR), 
            any(LastfmEntityQueryConfig.class)
        )).thenReturn(artistsFromService);

        generator = new TestableLastfmArtistGetSimilarApiCallGenerator(
            apiCallService, snapshotService, entityService);

        // When
        List<LastfmArtist> result = generator.selectEntitiesForApiCalls();

        // Then
        assertEquals(3, result.size(), "Should keep all artists when MBIDs are null (different keys)");
    }

    // Helper method to access protected method
    private List<LastfmArtist> callSelectEntitiesForApiCalls() {
        // Create a testable version that exposes the protected method
        return new TestableLastfmArtistGetSimilarApiCallGenerator(
            apiCallService, snapshotService, entityService
        ).selectEntitiesForApiCalls();
    }

    private LastfmArtist createArtist(Long id, String name, String mbid, ApprovalStatus status, Integer listenersCount) {
        // Create a mock API call for the required field
        LastfmApiCall mockApiCall = LastfmApiCall.builder()
            .id(1L)
            .type(LastfmApiCallType.ARTIST_GET_SIMILAR)
            .dataSnapshotId(1L)
            .dueDttm(java.time.Instant.now())
            .params(java.util.Map.of())
            .build();
            
        return LastfmArtist.builder()
            .id(id)
            .name(name)
            .mbid(mbid)
            .approvalStatus(status)
            .listenersCount(listenersCount)
            .apiCall(mockApiCall)
            .build();
    }

    // Testable implementation that exposes protected methods
    private static class TestableLastfmArtistGetSimilarApiCallGenerator extends LastfmArtistGetSimilarApiCallGenerator {

        public TestableLastfmArtistGetSimilarApiCallGenerator(
            LastfmApiCallService apiCallService,
            LastfmDataSnapshotService snapshotService,
            LastfmEntityService entityService
        ) {
            super(apiCallService, snapshotService, entityService);
        }

        @Override
        public List<LastfmArtist> selectEntitiesForApiCalls() {
            return super.selectEntitiesForApiCalls();
        }
    }
}
