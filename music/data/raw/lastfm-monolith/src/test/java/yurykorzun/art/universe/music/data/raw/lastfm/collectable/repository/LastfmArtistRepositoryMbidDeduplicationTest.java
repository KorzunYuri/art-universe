package yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaTestWithHelper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LastfmArtistRepositoryMbidDeduplicationTest extends JpaTestWithHelper {

    @Autowired
    private LastfmArtistRepository artistRepository;

    @Autowired
    private DbConsistencyHelper dbHelper;

    @BeforeEach
    void setUp() {
        dbHelper.cleanup();
    }

    @Test
    void findAllToGetInfoFor_shouldDeduplicateByMbid_whenDuplicatesExist() {
        // Given: Create artists with same MBID but different metrics
        LastfmArtist artist1 = dbHelper.createAndSaveArtist(builder -> builder
            .name("Artist Name 1")
            .mbid("same-mbid-123")
            .approvalStatus(ApprovalStatus.PENDING)
            .listenersCount(null)  // Missing stats - needs getInfo
            .playCount(null)
        );

        LastfmArtist artist2 = dbHelper.createAndSaveArtist(builder -> builder
            .name("Artist Name 2")
            .mbid("same-mbid-123")
            .approvalStatus(ApprovalStatus.APPROVED)
            .listenersCount(500)   // Has some stats but missing playCount
            .playCount(null)       // Missing - needs getInfo
        );

        LastfmArtist artist3 = dbHelper.createAndSaveArtist(builder -> builder
            .name("Artist Name 3")
            .mbid("same-mbid-123")
            .approvalStatus(ApprovalStatus.APPROVED)
            .listenersCount(2000)  // Has some stats but missing playCount
            .playCount(null)       // Missing - needs getInfo
        );

        // Different MBID artist for control
        LastfmArtist artist4 = dbHelper.createAndSaveArtist(builder -> builder
            .name("Different Artist")
            .mbid("different-mbid-456")
            .approvalStatus(ApprovalStatus.APPROVED)
            .listenersCount(null)  // Missing stats - needs getInfo
            .playCount(null)
        );

        // When
        List<LastfmArtist> result = artistRepository.findAllToGetInfoFor();

        // Then
        assertEquals(2, result.size(), "Should return only 2 unique artists (by MBID)");
        
        // Find the artist with same-mbid-123
        LastfmArtist selectedArtist = result.stream()
            .filter(a -> "same-mbid-123".equals(a.getMbid()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Should contain artist with same-mbid-123"));
        
        // Should select artist3 (APPROVED status + highest listeners count)
        assertEquals(artist3.getId(), selectedArtist.getId());
        assertEquals("Artist Name 3", selectedArtist.getName());
        assertEquals(ApprovalStatus.APPROVED, selectedArtist.getApprovalStatus());
        assertEquals(2000, selectedArtist.getListenersCount());
        
        // Should also contain the different MBID artist
        assertTrue(result.stream().anyMatch(a -> artist4.getId() == a.getId()));
    }

    @Test
    void findAllToGetInfoFor_shouldHandleNullMbid_withoutDuplication() {
        // Given: Create artists with null MBID
        LastfmArtist artist1 = dbHelper.createAndSaveArtist(builder -> builder
            .name("Artist 1")
            .mbid(null)
            .approvalStatus(ApprovalStatus.APPROVED)
            .listenersCount(null)  // Missing stats - needs getInfo
            .playCount(null)
        );

        LastfmArtist artist2 = dbHelper.createAndSaveArtist(builder -> builder
            .name("Artist 2")
            .mbid(null)
            .approvalStatus(ApprovalStatus.APPROVED)
            .listenersCount(2000)  // Has some stats but missing playCount
            .playCount(null)       // Missing - needs getInfo
        );

        // When
        List<LastfmArtist> result = artistRepository.findAllToGetInfoFor();

        // Then
        assertEquals(2, result.size(), "Should return both artists as they have different IDs");
        assertTrue(result.stream().anyMatch(a -> artist1.getId() == a.getId()));
        assertTrue(result.stream().anyMatch(a -> artist2.getId() == a.getId()));
    }

    @Test
    void findAllToGetInfoFor_shouldPreferApprovedStatus_overHigherListenersCount() {
        // Given
        LastfmArtist pendingWithHighListeners = dbHelper.createAndSaveArtist(builder -> builder
            .name("Pending Artist")
            .mbid("test-mbid")
            .approvalStatus(ApprovalStatus.PENDING)
            .listenersCount(null)  // Missing stats - needs getInfo
            .playCount(null)
        );

        LastfmArtist approvedWithLowListeners = dbHelper.createAndSaveArtist(builder -> builder
            .name("Approved Artist")
            .mbid("test-mbid")
            .approvalStatus(ApprovalStatus.APPROVED)
            .listenersCount(null)  // Missing stats - needs getInfo
            .playCount(null)
        );

        // When
        List<LastfmArtist> result = artistRepository.findAllToGetInfoFor();

        // Then
        assertEquals(1, result.size());
        assertEquals(approvedWithLowListeners.getId(), result.get(0).getId());
        assertEquals(ApprovalStatus.APPROVED, result.get(0).getApprovalStatus());
    }

    @Test
    void findAllToGetInfoFor_shouldPreferHigherListenersCount_whenSameApprovalStatus() {
        // Given
        LastfmArtist artistLowListeners = dbHelper.createAndSaveArtist(builder -> builder
            .name("Low Listeners")
            .mbid("test-mbid")
            .approvalStatus(ApprovalStatus.APPROVED)
            .listenersCount(null)  // Missing stats - needs getInfo
            .playCount(null)
        );

        LastfmArtist artistHighListeners = dbHelper.createAndSaveArtist(builder -> builder
            .name("High Listeners")
            .mbid("test-mbid")
            .approvalStatus(ApprovalStatus.APPROVED)
            .listenersCount(5000)  // Has some stats but missing playCount
            .playCount(null)       // Missing - needs getInfo
        );

        // When
        List<LastfmArtist> result = artistRepository.findAllToGetInfoFor();

        // Then
        assertEquals(1, result.size());
        assertEquals(artistHighListeners.getId(), result.get(0).getId());
        assertEquals(5000, result.get(0).getListenersCount());
    }

    @Test
    void findAllToGetInfoFor_shouldPreferLowerId_whenAllMetricsEqual() {
        // Given: Create artists with same MBID and same metrics, but different IDs
        // We need to ensure the first created artist has a lower ID
        LastfmArtist firstArtist = dbHelper.createAndSaveArtist(builder -> builder
            .name("First Artist")
            .mbid("test-mbid")
            .approvalStatus(ApprovalStatus.APPROVED)
            .listenersCount(null)  // Missing stats - needs getInfo
            .playCount(null)
        );

        LastfmArtist secondArtist = dbHelper.createAndSaveArtist(builder -> builder
            .name("Second Artist")
            .mbid("test-mbid")
            .approvalStatus(ApprovalStatus.APPROVED)
            .listenersCount(null)  // Missing stats - needs getInfo
            .playCount(null)
        );

        // When
        List<LastfmArtist> result = artistRepository.findAllToGetInfoFor();

        // Then
        assertEquals(1, result.size());
        // Should prefer the artist with lower ID (first created)
        assertTrue(result.get(0).getId() <= Math.max(firstArtist.getId(), secondArtist.getId()));
    }

    @Test
    void findAllToGetInfoFor_shouldHandleNullListenersCount() {
        // Given
        LastfmArtist artistWithNullListeners = dbHelper.createAndSaveArtist(builder -> builder
            .name("Null Listeners")
            .mbid("test-mbid")
            .approvalStatus(ApprovalStatus.APPROVED)
            .listenersCount(null)
        );

        LastfmArtist artistWithListeners = dbHelper.createAndSaveArtist(builder -> builder
            .name("With Listeners")
            .mbid("test-mbid")
            .approvalStatus(ApprovalStatus.APPROVED)
            .listenersCount(1000)
        );

        // When
        List<LastfmArtist> result = artistRepository.findAllToGetInfoFor();

        // Then
        assertEquals(1, result.size());
        assertEquals(artistWithListeners.getId(), result.get(0).getId());
        assertEquals(1000, result.get(0).getListenersCount());
    }
}
