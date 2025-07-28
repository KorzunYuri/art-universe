package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.common.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmDataSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmEntityService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class LastfmArtistApiCallsGeneratorTest {

    @Mock
    private LastfmApiCallService apiCallService;
    
    @Mock
    private LastfmDataSnapshotService snapshotService;
    
    @Mock
    private LastfmEntityService entityService;

    private TestableLastfmArtistApiCallsGenerator createGenerator() {
        return new TestableLastfmArtistApiCallsGenerator(apiCallService, snapshotService, entityService);
    }

    @Test
    void deduplicateByMbid_shouldKeepUniqueArtists_whenNoMbidDuplicates() {
        // Given
        TestableLastfmArtistApiCallsGenerator generator = createGenerator();
        List<LastfmArtist> artists = List.of(
            createArtist(1L, "Artist1", "mbid1", ApprovalStatus.APPROVED, 1000),
            createArtist(2L, "Artist2", "mbid2", ApprovalStatus.APPROVED, 2000),
            createArtist(3L, "Artist3", null, ApprovalStatus.PENDING, 500)
        );

        // When
        List<LastfmArtist> result = generator.deduplicateByMbid(artists);

        // Then
        assertEquals(3, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());
        assertEquals(3L, result.get(2).getId());
    }

    @Test
    void deduplicateByMbid_shouldKeepBestArtist_whenMbidDuplicates() {
        // Given
        TestableLastfmArtistApiCallsGenerator generator = createGenerator();
        List<LastfmArtist> artists = List.of(
            createArtist(1L, "Artist1", "same-mbid", ApprovalStatus.PENDING, 1000),
            createArtist(2L, "Artist2", "same-mbid", ApprovalStatus.APPROVED, 500),
            createArtist(3L, "Artist3", "same-mbid", ApprovalStatus.APPROVED, 2000)
        );

        // When
        List<LastfmArtist> result = generator.deduplicateByMbid(artists);

        // Then
        assertEquals(1, result.size());
        assertEquals(3L, result.get(0).getId()); // Approved with highest listeners count
    }

    @Test
    void deduplicateByMbid_shouldPreferApprovedStatus() {
        // Given
        TestableLastfmArtistApiCallsGenerator generator = createGenerator();
        List<LastfmArtist> artists = List.of(
            createArtist(1L, "Artist1", "same-mbid", ApprovalStatus.PENDING, 5000),
            createArtist(2L, "Artist2", "same-mbid", ApprovalStatus.APPROVED, 1000)
        );

        // When
        List<LastfmArtist> result = generator.deduplicateByMbid(artists);

        // Then
        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getId()); // Approved status wins over higher listeners count
    }

    @Test
    void deduplicateByMbid_shouldPreferHigherListenersCount_whenSameStatus() {
        // Given
        TestableLastfmArtistApiCallsGenerator generator = createGenerator();
        List<LastfmArtist> artists = List.of(
            createArtist(1L, "Artist1", "same-mbid", ApprovalStatus.APPROVED, 1000),
            createArtist(2L, "Artist2", "same-mbid", ApprovalStatus.APPROVED, 5000)
        );

        // When
        List<LastfmArtist> result = generator.deduplicateByMbid(artists);

        // Then
        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getId()); // Higher listeners count wins
    }

    @Test
    void deduplicateByMbid_shouldPreferLowerId_whenSameMetrics() {
        // Given
        TestableLastfmArtistApiCallsGenerator generator = createGenerator();
        List<LastfmArtist> artists = List.of(
            createArtist(5L, "Artist1", "same-mbid", ApprovalStatus.APPROVED, 1000),
            createArtist(2L, "Artist2", "same-mbid", ApprovalStatus.APPROVED, 1000)
        );

        // When
        List<LastfmArtist> result = generator.deduplicateByMbid(artists);

        // Then
        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getId()); // Lower ID wins (older artist)
    }

    @Test
    void deduplicateByMbid_shouldHandleNullMbid() {
        // Given
        TestableLastfmArtistApiCallsGenerator generator = createGenerator();
        List<LastfmArtist> artists = List.of(
            createArtist(1L, "Artist1", null, ApprovalStatus.APPROVED, 1000),
            createArtist(2L, "Artist2", null, ApprovalStatus.APPROVED, 2000),
            createArtist(3L, "Artist3", "mbid3", ApprovalStatus.APPROVED, 500)
        );

        // When
        List<LastfmArtist> result = generator.deduplicateByMbid(artists);

        // Then
        assertEquals(3, result.size()); // All different because null MBIDs create unique keys with ID
    }

    @Test
    void deduplicateByMbid_shouldHandleNullListenersCount() {
        // Given
        TestableLastfmArtistApiCallsGenerator generator = createGenerator();
        List<LastfmArtist> artists = List.of(
            createArtist(1L, "Artist1", "same-mbid", ApprovalStatus.APPROVED, null),
            createArtist(2L, "Artist2", "same-mbid", ApprovalStatus.APPROVED, 1000)
        );

        // When
        List<LastfmArtist> result = generator.deduplicateByMbid(artists);

        // Then
        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getId()); // Non-null listeners count wins
    }

    @Test
    void shouldReplaceArtist_shouldReturnTrue_whenCandidateHasBetterStatus() {
        // Given
        TestableLastfmArtistApiCallsGenerator generator = createGenerator();
        LastfmArtist existing = createArtist(1L, "Artist1", "mbid", ApprovalStatus.PENDING, 1000);
        LastfmArtist candidate = createArtist(2L, "Artist2", "mbid", ApprovalStatus.APPROVED, 500);

        // When
        boolean result = generator.shouldReplaceArtist(existing, candidate);

        // Then
        assertTrue(result);
    }

    @Test
    void shouldReplaceArtist_shouldReturnFalse_whenExistingHasBetterStatus() {
        // Given
        TestableLastfmArtistApiCallsGenerator generator = createGenerator();
        LastfmArtist existing = createArtist(1L, "Artist1", "mbid", ApprovalStatus.APPROVED, 500);
        LastfmArtist candidate = createArtist(2L, "Artist2", "mbid", ApprovalStatus.PENDING, 1000);

        // When
        boolean result = generator.shouldReplaceArtist(existing, candidate);

        // Then
        assertFalse(result);
    }

    private LastfmArtist createArtist(Long id, String name, String mbid, ApprovalStatus status, Integer listenersCount) {
        // Create a mock API call for the required field
        LastfmApiCall mockApiCall = LastfmApiCall.builder()
            .id(1L)
            .type(LastfmApiCallType.ARTIST_GET_INFO)
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

    // Testable implementation of abstract class
    private static class TestableLastfmArtistApiCallsGenerator extends LastfmArtistApiCallsGenerator {

        public TestableLastfmArtistApiCallsGenerator(
            LastfmApiCallService apiCallService,
            LastfmDataSnapshotService snapshotService,
            LastfmEntityService entityService
        ) {
            super(apiCallService, snapshotService, entityService);
        }

        @Override
        public LastfmApiCallType getApiCallType() {
            return LastfmApiCallType.ARTIST_GET_SIMILAR;
        }

        @Override
        protected int getDueDurationDays() {
            return 7;
        }

        // Expose protected methods for testing
        @Override
        public List<LastfmArtist> deduplicateByMbid(List<LastfmArtist> artists) {
            return super.deduplicateByMbid(artists);
        }

        @Override
        public boolean shouldReplaceArtist(LastfmArtist existing, LastfmArtist candidate) {
            return super.shouldReplaceArtist(existing, candidate);
        }
    }
}
