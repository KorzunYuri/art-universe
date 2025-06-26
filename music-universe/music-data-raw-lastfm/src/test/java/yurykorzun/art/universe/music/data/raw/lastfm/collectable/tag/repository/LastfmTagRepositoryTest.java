package yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.repository;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaOnlyTest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
class LastfmTagRepositoryTest extends JpaOnlyTest {

    @Autowired
    private LastfmTagRepository repository;

    @Autowired
    private DbConsistencyHelper consistencyHelper;

    private LastfmTag rock;
    private LastfmTag pop;
    private LastfmTag jazz;
    private LastfmTag electronic;

    @BeforeEach
    void setUp() {
        LastfmApiCall apiCall = consistencyHelper.createAndSaveApiCall();
        
        // Create test tags with different values for sorting tests
        rock = LastfmTag.builder()
            .name("Rock")
            .url("https://example.com/rock")
            .usageCount(5000)
            .usageUsersCount(2500)
            .approvalStatus(ApprovalStatus.APPROVED)
            .apiCall(apiCall)
            .build();
            
        pop = LastfmTag.builder()
            .name("Pop")
            .url("https://example.com/pop")
            .usageCount(8000)
            .usageUsersCount(4000)
            .approvalStatus(ApprovalStatus.APPROVED)
            .apiCall(apiCall)
            .build();
            
        jazz = LastfmTag.builder()
            .name("Jazz")
            .url("https://example.com/jazz")
            .usageCount(null)
            .usageUsersCount(1500)
            .approvalStatus(ApprovalStatus.PENDING)
            .apiCall(apiCall)
            .build();
            
        electronic = LastfmTag.builder()
            .name("Electronic")
            .url("https://example.com/electronic")
            .usageCount(3000)
            .usageUsersCount(null)
            .approvalStatus(ApprovalStatus.APPROVED)
            .apiCall(apiCall)
            .build();
            
        repository.saveAll(List.of(rock, pop, jazz, electronic));
    }
    
    @AfterEach
    void tearDown() {
        consistencyHelper.cleanup();
    }

    private LastfmTag createTag(String name, LastfmApiCall apiCall) {
        return LastfmTag.builder()
                .name(name)
                .usageCount(42)
                .usageUsersCount(10)
                .apiCall(apiCall)
            .build();
    }

    @Test
    void testSaveTag() {
        final String name = "Tag";
        final int usageCount = 42;
        final int usageUsersCount = 10;
        final LastfmApiCall sourceApiCall = consistencyHelper.createAndSaveApiCall(LastfmApiCallType.TAG_TOP_TAGS);

        LastfmTag tag = LastfmTag.builder()
                .name(name)
                .usageCount(usageCount)
                .usageUsersCount(usageUsersCount)
                .apiCall(sourceApiCall)
            .build();

        LastfmTag saved = repository.save(tag);
        assertNotNull(saved);
        assertTrue(saved.getId() > 0);
        assertEquals(name, saved.getName());
        assertEquals(usageCount, saved.getUsageCount());
        assertEquals(usageUsersCount, saved.getUsageUsersCount());
        assertEquals(sourceApiCall, saved.getApiCall());
    }

    @Test
    void testFindAllByNameIn() {
        // given
        LastfmApiCall apiCall = consistencyHelper.createAndSaveApiCall();
        LastfmTag tag1 = createTag("rock", apiCall);
        LastfmTag tag2 = createTag("pop", apiCall);
        repository.saveAllAndFlush(Arrays.asList(tag1, tag2));

        // when
        List<LastfmTag> foundTags = repository.findAllByNameIn(Collections.singletonList("rock"));

        // then
        assertEquals(1, foundTags.size(), "Only one tag should be found by name");
        assertEquals("rock", foundTags.get(0).getName(), "Tag name persisted incorrectly");
    }

    @Test
    void testSaveAllDoesNotDuplicate() {
        // given
        LastfmApiCall apiCall = consistencyHelper.createAndSaveApiCall();
        LastfmTag tag1 = createTag("jazz", apiCall);
        LastfmTag tag2 = createTag("latina", apiCall);
        repository.saveAllAndFlush(Arrays.asList(tag1, tag2));
        
        // Count tags before second save (including setUp tags)
        int tagsBeforeSecondSave = repository.findAll().size();

        // when
        LastfmTag tag3 = createTag("metal", apiCall);
        repository.saveAllAndFlush(Arrays.asList(tag1, tag3)); // one new, one existing

        // then
        List<LastfmTag> allTags = repository.findAll();
        assertEquals(tagsBeforeSecondSave + 1, allTags.size(), "Second save of the same tag should not produce duplicates");
    }
    
    @Test
    void findTags_withSearchParam_shouldReturnMatchingTags() {
        // Given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name"));
        
        // When
        Page<LastfmTag> result = repository.findTags("o", null, pageable);
        
        // Then
        assertEquals(3, result.getTotalElements()); // Rock, Pop, Electronic
        assertTrue(result.getContent().stream().anyMatch(t -> t.getName().equals("Rock")));
        assertTrue(result.getContent().stream().anyMatch(t -> t.getName().equals("Pop")));
        assertTrue(result.getContent().stream().anyMatch(t -> t.getName().equals("Electronic")));
        assertFalse(result.getContent().stream().anyMatch(t -> t.getName().equals("Jazz")));
    }
    
    @Test
    void findTags_withApprovalStatus_shouldReturnMatchingTags() {
        // Given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name"));
        List<ApprovalStatus> approvalStatuses = List.of(ApprovalStatus.APPROVED);
        
        // When
        Page<LastfmTag> result = repository.findTags(null, approvalStatuses, pageable);
        
        // Then
        assertEquals(3, result.getTotalElements()); // Rock, Pop, Electronic
        assertTrue(result.getContent().stream().anyMatch(t -> t.getName().equals("Rock")));
        assertTrue(result.getContent().stream().anyMatch(t -> t.getName().equals("Pop")));
        assertTrue(result.getContent().stream().anyMatch(t -> t.getName().equals("Electronic")));
        assertFalse(result.getContent().stream().anyMatch(t -> t.getName().equals("Jazz")));
    }
    
    @Test
    void findTags_withMultipleParams_shouldReturnMatchingTags() {
        // Given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name"));
        List<ApprovalStatus> approvalStatuses = List.of(ApprovalStatus.APPROVED);
        
        // When
        Page<LastfmTag> result = repository.findTags("o", approvalStatuses, pageable);
        
        // Then
        assertEquals(3, result.getTotalElements()); // Rock, Pop, Electronic (all approved and contain 'o')
        assertTrue(result.getContent().stream().anyMatch(t -> t.getName().equals("Rock")));
        assertTrue(result.getContent().stream().anyMatch(t -> t.getName().equals("Pop")));
        assertTrue(result.getContent().stream().anyMatch(t -> t.getName().equals("Electronic")));
        assertFalse(result.getContent().stream().anyMatch(t -> t.getName().equals("Jazz")));
    }
    
    @Test
    void findTags_withNoParams_shouldReturnAllTags() {
        // Given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name"));
        
        // When
        Page<LastfmTag> result = repository.findTags(null, null, pageable);
        
        // Then
        assertEquals(4, result.getTotalElements());
        assertTrue(result.getContent().stream().anyMatch(t -> t.getName().equals("Rock")));
        assertTrue(result.getContent().stream().anyMatch(t -> t.getName().equals("Pop")));
        assertTrue(result.getContent().stream().anyMatch(t -> t.getName().equals("Jazz")));
        assertTrue(result.getContent().stream().anyMatch(t -> t.getName().equals("Electronic")));
    }
    
    @Test
    void findTags_sortByName_shouldSortAlphabetically() {
        // Given
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"));
        
        // When
        Page<LastfmTag> result = repository.findTags(null, null, pageable);
        
        // Then
        List<LastfmTag> sortedTags = result.getContent();
        assertEquals(4, sortedTags.size());
        
        // Check alphabetical order: Electronic, Jazz, Pop, Rock
        assertEquals("Electronic", sortedTags.get(0).getName());
        assertEquals("Jazz", sortedTags.get(1).getName());
        assertEquals("Pop", sortedTags.get(2).getName());
        assertEquals("Rock", sortedTags.get(3).getName());
    }
    
    @Test
    void findTags_sortByNameDescending_shouldSortReverseAlphabetically() {
        // Given
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "name"));
        
        // When
        Page<LastfmTag> result = repository.findTags(null, null, pageable);
        
        // Then
        List<LastfmTag> sortedTags = result.getContent();
        assertEquals(4, sortedTags.size());
        
        // Check reverse alphabetical order: Rock, Pop, Jazz, Electronic
        assertEquals("Rock", sortedTags.get(0).getName());
        assertEquals("Pop", sortedTags.get(1).getName());
        assertEquals("Jazz", sortedTags.get(2).getName());
        assertEquals("Electronic", sortedTags.get(3).getName());
    }
    
    @Test
    void findTags_sortByUsageCount_shouldPlaceNullsLast() {
        // Given
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "usageCount"));
        
        // When
        Page<LastfmTag> result = repository.findTags(null, null, pageable);
        
        // Then
        List<LastfmTag> sortedTags = result.getContent();
        assertEquals(4, sortedTags.size());
        
        // Pop (8000), Rock (5000), Electronic (3000), Jazz (null)
        assertEquals("Pop", sortedTags.get(0).getName());
        assertEquals("Rock", sortedTags.get(1).getName());
        assertEquals("Electronic", sortedTags.get(2).getName());
        assertEquals("Jazz", sortedTags.get(3).getName());
        assertNull(sortedTags.get(3).getUsageCount());
    }
    
    @Test
    void findTags_sortByUsageUsersCount_shouldPlaceNullsLast() {
        // Given
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "usageUsersCount"));
        
        // When
        Page<LastfmTag> result = repository.findTags(null, null, pageable);
        
        // Then
        List<LastfmTag> sortedTags = result.getContent();
        assertEquals(4, sortedTags.size());
        
        // Pop (4000), Rock (2500), Jazz (1500), Electronic (null)
        assertEquals("Pop", sortedTags.get(0).getName());
        assertEquals("Rock", sortedTags.get(1).getName());
        assertEquals("Jazz", sortedTags.get(2).getName());
        assertEquals("Electronic", sortedTags.get(3).getName());
        assertNull(sortedTags.get(3).getUsageUsersCount());
    }
    
    @Test
    void findTags_withCaseInsensitiveSearch_shouldReturnMatchingTags() {
        // Given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name"));
        
        // When - search with lowercase
        Page<LastfmTag> result1 = repository.findTags("rock", null, pageable);
        
        // Then
        assertEquals(1, result1.getTotalElements());
        assertEquals("Rock", result1.getContent().get(0).getName());
        
        // When - search with uppercase
        Page<LastfmTag> result2 = repository.findTags("ROCK", null, pageable);
        
        // Then
        assertEquals(1, result2.getTotalElements());
        assertEquals("Rock", result2.getContent().get(0).getName());
        
        // When - search with mixed case
        Page<LastfmTag> result3 = repository.findTags("RoCk", null, pageable);
        
        // Then
        assertEquals(1, result3.getTotalElements());
        assertEquals("Rock", result3.getContent().get(0).getName());
    }
    
    @Test
    void findTags_withMultipleApprovalStatuses_shouldReturnMatchingTags() {
        // Given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name"));
        List<ApprovalStatus> approvalStatuses = List.of(ApprovalStatus.APPROVED, ApprovalStatus.PENDING);
        
        // When
        Page<LastfmTag> result = repository.findTags(null, approvalStatuses, pageable);
        
        // Then
        assertEquals(4, result.getTotalElements());
        assertTrue(result.getContent().stream().anyMatch(t -> t.getName().equals("Rock")));
        assertTrue(result.getContent().stream().anyMatch(t -> t.getName().equals("Pop")));
        assertTrue(result.getContent().stream().anyMatch(t -> t.getName().equals("Jazz")));
        assertTrue(result.getContent().stream().anyMatch(t -> t.getName().equals("Electronic")));
    }
}
