package yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaOnlyTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LastfmTagRepositoryTest extends JpaOnlyTest {

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
        Page<LastfmTag> result = tagRepository.findTags("o", null, pageable);
        
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
        Page<LastfmTag> result = tagRepository.findTags(null, List.of(ApprovalStatus.APPROVED), pageable);
        
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
        Page<LastfmTag> resultAsc = tagRepository.findTags(null, null, pageableAsc);
        
        // When - sort by name descending
        Pageable pageableDesc = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "name"));
        Page<LastfmTag> resultDesc = tagRepository.findTags(null, null, pageableDesc);
        
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
}
