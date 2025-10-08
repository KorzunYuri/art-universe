package yurykorzun.art.universe.music.data.raw.lastfm.collectable.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.common.dto.lookup.LookupRequestDTO;
import yurykorzun.art.universe.common.dto.lookup.LookupResultDTO;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.lookup.LastfmArtistLookupService;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaTestWithHelper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Import({
    LastfmArtistLookupService.class,
})
class LastfmArtistLookupServiceTest extends JpaTestWithHelper {

    @Autowired
    private LastfmArtistLookupService lookupService;

    @Autowired
    private DbConsistencyHelper dbHelper;

    @Test
    void lookup_shouldPrioritizeByApprovalStatus_whenMultipleArtistsMatch() {
        // Given
        dbHelper.cleanup();
        
        // Create artists with different approval statuses but similar names
        LastfmArtist pendingArtist = dbHelper.createAndSaveArtist(builder -> builder
            .name("Test Artist 1")
            .approvalStatus(ApprovalStatus.PENDING));
        
        LastfmArtist approvedArtist = dbHelper.createAndSaveArtist(builder -> builder
            .name("Test Artist 2")
            .approvalStatus(ApprovalStatus.APPROVED));
        
        LastfmArtist preApprovedArtist = dbHelper.createAndSaveArtist(builder -> builder
            .name("Test Artist 3")
            .approvalStatus(ApprovalStatus.PRE_APPROVED));
        
        LastfmArtist declinedArtist = dbHelper.createAndSaveArtist(builder -> builder
            .name("Test Artist 4")
            .approvalStatus(ApprovalStatus.DECLINED));

        dbHelper.flush();

        // When
        LookupRequestDTO request = LookupRequestDTO.builder()
            .search("Test Artist")
            .limit(10)
            .build();
        
        List<LookupResultDTO> results = lookupService.lookup(request);

        // Then
        assertNotNull(results);
        assertEquals(4, results.size());
        
        // Check that approved artist comes first, then pre-approved, then pending, then declined
        assertEquals(approvedArtist.getId(), results.get(0).getId());
        assertEquals(preApprovedArtist.getId(), results.get(1).getId());
        assertEquals(pendingArtist.getId(), results.get(2).getId());
        assertEquals(declinedArtist.getId(), results.get(3).getId());
    }

    @Test
    void lookup_shouldPrioritizeExactMatch_whenSearchTermMatchesExactly() {
        // Given
        dbHelper.cleanup();
        
        // Create artists where one matches exactly
        LastfmArtist exactMatch = dbHelper.createAndSaveArtist(builder -> builder
            .name("Beatles")
            .approvalStatus(ApprovalStatus.PENDING));
        
        LastfmArtist partialMatch = dbHelper.createAndSaveArtist(builder -> builder
            .name("Beatles Revival")
            .approvalStatus(ApprovalStatus.APPROVED)); // Higher approval status but not exact match

        dbHelper.flush();

        // When
        LookupRequestDTO request = LookupRequestDTO.builder()
            .search("Beatles")
            .limit(10)
            .build();
        
        List<LookupResultDTO> results = lookupService.lookup(request);

        // Then
        assertNotNull(results);
        assertEquals(2, results.size());
        
        // Exact match should come first despite lower approval status
        assertEquals(exactMatch.getId(), results.get(0).getId());
        assertEquals(partialMatch.getId(), results.get(1).getId());
    }
}
