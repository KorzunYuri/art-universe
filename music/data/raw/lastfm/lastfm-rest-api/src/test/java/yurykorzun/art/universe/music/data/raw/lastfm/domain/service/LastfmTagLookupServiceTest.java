package yurykorzun.art.universe.music.data.raw.lastfm.domain.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import yurykorzun.art.universe.data.raw.common.domain.entity.ApprovalStatus;
import yurykorzun.art.universe.common.domain.dto.lookup.LookupRequestDTO;
import yurykorzun.art.universe.common.domain.dto.lookup.LookupResultDTO;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.lookup.LastfmTagLookupService;
import yurykorzun.art.universe.music.data.raw.lastfm.test.archetypes.LastfmJpaTestHelper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Import({
    LastfmTagLookupService.class,
})
class LastfmTagLookupServiceTest extends LastfmJpaTestHelper {

    @Autowired
    private LastfmTagLookupService lookupService;

    @Test
    void lookup_shouldPrioritizeByApprovalStatus_whenMultipleTagsMatch() {
        
        // Create tags with different approval statuses but similar names
        LastfmTag pendingTag = consistencyHelper.createAndSaveTag(builder -> builder
            .name("rock-n-roll")
            .approvalStatus(ApprovalStatus.PENDING));
        
        LastfmTag approvedTag = consistencyHelper.createAndSaveTag(builder -> builder
            .name("rock music")
            .approvalStatus(ApprovalStatus.APPROVED));
        
        LastfmTag preApprovedTag = consistencyHelper.createAndSaveTag(builder -> builder
            .name("rock and roll")
            .approvalStatus(ApprovalStatus.PRE_APPROVED));
        
        LastfmTag declinedTag = consistencyHelper.createAndSaveTag(builder -> builder
            .name("rock alternative")
            .approvalStatus(ApprovalStatus.DECLINED));

        consistencyHelper.flush();

        // When
        LookupRequestDTO request = LookupRequestDTO.builder()
            .search("rock")
            .limit(10)
            .build();
        
        List<LookupResultDTO> results = lookupService.lookup(request);

        // Then
        assertNotNull(results);
        assertEquals(4, results.size());
        
        // Check that approved tag comes first, then pre-approved, then pending, then declined
        assertEquals(approvedTag.getId(), results.get(0).getId());
        assertEquals(preApprovedTag.getId(), results.get(1).getId());
        assertEquals(pendingTag.getId(), results.get(2).getId());
        assertEquals(declinedTag.getId(), results.get(3).getId());
    }

    @Test
    void lookup_shouldPrioritizeExactMatch_whenSearchTermMatchesExactly() {
        // Given
        consistencyHelper.cleanup();
        
        // Create tags where one matches exactly
        LastfmTag exactMatch = consistencyHelper.createAndSaveTag(builder -> builder
            .name("jazz")
            .approvalStatus(ApprovalStatus.PENDING));
        
        LastfmTag partialMatch = consistencyHelper.createAndSaveTag(builder -> builder
            .name("jazz fusion")
            .approvalStatus(ApprovalStatus.APPROVED)); // Higher approval status but not exact match

        consistencyHelper.flush();

        // When
        LookupRequestDTO request = LookupRequestDTO.builder()
            .search("jazz")
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
