package yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaTestWithHelper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LastfmTagRepositoryTest extends JpaTestWithHelper {

    @Autowired
    private LastfmTagRepository tagRepository;

    @Autowired
    private DbConsistencyHelper dbHelper;

    @BeforeEach
    void setUp() {
        dbHelper.cleanup();
    }

    @Test
    void findTags_shouldReturnTagsMatchingSearchCriteria() {
        // Given
        LastfmTag tagRock = dbHelper.createAndSaveTag(builder -> builder.name("rock"));
        LastfmTag tagPop = dbHelper.createAndSaveTag(builder -> builder.name("pop"));
        LastfmTag tagJazz = dbHelper.createAndSaveTag(builder -> builder.name("jazz"));
        
        // When - search for "o" should match "rock" and "pop"
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"));
        Page<LastfmTag> result = tagRepository.findTags("o", null, null, null, pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        List<LastfmTag> content = result.getContent();
        assertTrue(content.stream().anyMatch(tag -> tag.getId() == tagPop.getId()));
        assertTrue(content.stream().anyMatch(tag -> tag.getId() == tagRock.getId()));
        assertFalse(content.stream().anyMatch(tag -> tag.getId() == tagJazz.getId()));
    }
    
    @Test
    void findTags_shouldFilterByApprovalStatus() {
        // Given
        LastfmTag tagPending = dbHelper.createAndSaveTag(builder -> builder
            .name("pending")
            .approvalStatus(ApprovalStatus.PENDING));
        
        LastfmTag tagApproved = dbHelper.createAndSaveTag(builder -> builder
            .name("approved")
            .approvalStatus(ApprovalStatus.APPROVED));
        
        LastfmTag tagDeclined = dbHelper.createAndSaveTag(builder -> builder
            .name("declined")
            .approvalStatus(ApprovalStatus.DECLINED));
        
        // When - filter by APPROVED status
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"));
        Page<LastfmTag> result = tagRepository.findTags(null, List.of(ApprovalStatus.APPROVED), null, null, pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(tagApproved.getId(), result.getContent().get(0).getId());
    }
    
    @Test
    void findTags_shouldSortBySpecifiedField() {
        // Given
        LastfmTag tag1 = dbHelper.createAndSaveTag(builder -> builder.name("zebra"));
        LastfmTag tag2 = dbHelper.createAndSaveTag(builder -> builder.name("apple"));
        LastfmTag tag3 = dbHelper.createAndSaveTag(builder -> builder.name("banana"));
        
        // When - sort by name ascending
        Pageable pageableAsc = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"));
        Page<LastfmTag> resultAsc = tagRepository.findTags(null, null, null, null, pageableAsc);
        
        // When - sort by name descending
        Pageable pageableDesc = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "name"));
        Page<LastfmTag> resultDesc = tagRepository.findTags(null, null, null, null, pageableDesc);
        
        // Then - ascending order
        assertNotNull(resultAsc);
        assertEquals(3, resultAsc.getTotalElements());
        assertEquals("apple", resultAsc.getContent().get(0).getName());
        assertEquals("banana", resultAsc.getContent().get(1).getName());
        assertEquals("zebra", resultAsc.getContent().get(2).getName());
        
        // Then - descending order
        assertNotNull(resultDesc);
        assertEquals(3, resultDesc.getTotalElements());
        assertEquals("zebra", resultDesc.getContent().get(0).getName());
        assertEquals("banana", resultDesc.getContent().get(1).getName());
        assertEquals("apple", resultDesc.getContent().get(2).getName());
    }
    
    @Test
    void findAllByNameIn_shouldReturnMatchingTags() {
        // Given
        LastfmTag tag1 = dbHelper.createAndSaveTag(builder -> builder.name("rock"));
        LastfmTag tag2 = dbHelper.createAndSaveTag(builder -> builder.name("pop"));
        LastfmTag tag3 = dbHelper.createAndSaveTag(builder -> builder.name("jazz"));
        
        // When
        List<LastfmTag> result = tagRepository.findAllByNameIn(List.of("rock", "jazz"));
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(tag -> tag.getId() == tag1.getId()));
        assertTrue(result.stream().anyMatch(tag -> tag.getId() == tag3.getId()));
        assertFalse(result.stream().anyMatch(tag -> tag.getId() == tag2.getId()));
    }
    
    @Test
    void findTags_shouldFilterByMinUsageCount() {
        // Given
        LastfmTag tagLowUsage = dbHelper.createAndSaveTag(builder -> builder
            .name("low-usage")
            .usageCount(50));
        
        LastfmTag tagHighUsage = dbHelper.createAndSaveTag(builder -> builder
            .name("high-usage")
            .usageCount(150));
        
        // When - filter by minUsageCount = 100
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"));
        Page<LastfmTag> result = tagRepository.findTags(null, null, 100, null, pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(tagHighUsage.getId(), result.getContent().get(0).getId());
    }
    
    @Test
    void findTags_shouldFilterByMinUsageUsersCount() {
        // Given
        LastfmTag tagLowUsers = dbHelper.createAndSaveTag(builder -> builder
            .name("low-users")
            .usageUsersCount(25));
        
        LastfmTag tagHighUsers = dbHelper.createAndSaveTag(builder -> builder
            .name("high-users")
            .usageUsersCount(75));
        
        // When - filter by minUsageUsersCount = 50
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"));
        Page<LastfmTag> result = tagRepository.findTags(null, null, null, 50, pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(tagHighUsers.getId(), result.getContent().get(0).getId());
    }
    
    @Test
    void findTags_shouldFilterByAllParameters() {
        // Given
        LastfmTag tagMatching = dbHelper.createAndSaveTag(builder -> builder
            .name("rock-music")
            .approvalStatus(ApprovalStatus.APPROVED)
            .usageCount(200)
            .usageUsersCount(100));
        
        LastfmTag tagNotMatching1 = dbHelper.createAndSaveTag(builder -> builder
            .name("pop-music")  // doesn't match search
            .approvalStatus(ApprovalStatus.APPROVED)
            .usageCount(200)
            .usageUsersCount(100));
        
        LastfmTag tagNotMatching2 = dbHelper.createAndSaveTag(builder -> builder
            .name("rock-alternative")
            .approvalStatus(ApprovalStatus.PENDING)  // doesn't match approval status
            .usageCount(200)
            .usageUsersCount(100));
        
        LastfmTag tagNotMatching3 = dbHelper.createAndSaveTag(builder -> builder
            .name("rock-classic")
            .approvalStatus(ApprovalStatus.APPROVED)
            .usageCount(50)  // doesn't match minUsageCount
            .usageUsersCount(100));
        
        LastfmTag tagNotMatching4 = dbHelper.createAndSaveTag(builder -> builder
            .name("rock-indie")
            .approvalStatus(ApprovalStatus.APPROVED)
            .usageCount(200)
            .usageUsersCount(25));  // doesn't match minUsageUsersCount
        
        // When - filter by all parameters
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"));
        Page<LastfmTag> result = tagRepository.findTags(
            "rock", 
            List.of(ApprovalStatus.APPROVED), 
            100, 
            50, 
            pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(tagMatching.getId(), result.getContent().get(0).getId());
    }
    
    @Test
    void findTags_shouldHandleNullUsageValues() {
        // Given
        LastfmTag tagWithNulls = dbHelper.createAndSaveTag(builder -> builder
            .name("null-values")
            .usageCount(null)
            .usageUsersCount(null));
        
        LastfmTag tagWithValues = dbHelper.createAndSaveTag(builder -> builder
            .name("with-values")
            .usageCount(100)
            .usageUsersCount(50));
        
        // When - filter with minUsageCount and minUsageUsersCount
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"));
        Page<LastfmTag> result = tagRepository.findTags(null, null, 50, 25, pageable);
        
        // Then - only tag with values should be returned
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(tagWithValues.getId(), result.getContent().get(0).getId());
    }
}
