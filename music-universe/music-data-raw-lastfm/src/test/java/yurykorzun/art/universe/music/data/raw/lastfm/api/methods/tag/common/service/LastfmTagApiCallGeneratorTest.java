package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.common.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.LastfmDataSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallEntityService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmTag;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class LastfmTagApiCallGeneratorTest {

    @Mock
    private LastfmApiCallService apiCallService;

    @Mock
    private LastfmDataSnapshotService snapshotService;

    @Mock
    private LastfmApiCallEntityService entityService;

    private TestableLastfmTagApiCallGenerator generator;

    @Test
    void deduplicateEntitiesForApiCalls_shouldDeduplicateByName() {
        // Given: Tags with duplicate names
        List<LastfmTag> tags = List.of(
            createTag(1L, "duplicate-tag", ApprovalStatus.PENDING, 1000),
            createTag(2L, "duplicate-tag", ApprovalStatus.APPROVED, 2000),
            createTag(3L, "unique-tag", ApprovalStatus.APPROVED, 1500)
        );
        
        generator = new TestableLastfmTagApiCallGenerator(
            apiCallService, snapshotService, entityService);

        // When
        List<LastfmTag> result = generator.deduplicateEntitiesForApiCalls(tags);

        // Then
        assertEquals(2, result.size(), "Should deduplicate tags with same name");
        
        // Should keep the approved tag with higher usage count (tag2)
        assertEquals(1, result.stream()
            .mapToLong(t -> "duplicate-tag".equals(t.getName()) ? 1 : 0)
            .sum(), "Should have only one tag with duplicate name");
        
        LastfmTag selectedDuplicate = result.stream()
            .filter(t -> "duplicate-tag".equals(t.getName()))
            .findFirst()
            .orElseThrow();
        
        assertEquals(2L, selectedDuplicate.getId(), "Should select approved tag with higher usage count");
        assertEquals(ApprovalStatus.APPROVED, selectedDuplicate.getApprovalStatus());
        assertEquals(2000, selectedDuplicate.getUsageCount());
    }

    @Test
    void hasHigherPriority_shouldPrioritizeByApprovalStatus() {
        // Given
        generator = new TestableLastfmTagApiCallGenerator(
            apiCallService, snapshotService, entityService);
            
        LastfmTag approvedTag = createTag(1L, "Tag1", ApprovalStatus.APPROVED, 1000);
        LastfmTag pendingTag = createTag(2L, "Tag2", ApprovalStatus.PENDING, 2000);
        
        // When & Then
        assertTrue(generator.hasHigherPriority(approvedTag, pendingTag), 
            "Approved tag should have higher priority than pending tag");
        assertFalse(generator.hasHigherPriority(pendingTag, approvedTag), 
            "Pending tag should not have higher priority than approved tag");
    }

    @Test
    void hasHigherPriority_shouldPrioritizeByUsageCount_whenSameApprovalStatus() {
        // Given
        generator = new TestableLastfmTagApiCallGenerator(
            apiCallService, snapshotService, entityService);
            
        LastfmTag highUsageTag = createTag(1L, "Tag1", ApprovalStatus.APPROVED, 2000);
        LastfmTag lowUsageTag = createTag(2L, "Tag2", ApprovalStatus.APPROVED, 1000);
        
        // When & Then
        assertTrue(generator.hasHigherPriority(highUsageTag, lowUsageTag), 
            "Tag with higher usage count should have higher priority");
        assertFalse(generator.hasHigherPriority(lowUsageTag, highUsageTag), 
            "Tag with lower usage count should not have higher priority");
    }

    @Test
    void hasHigherPriority_shouldPrioritizeByIdWhenEverythingElseIsEqual() {
        // Given
        generator = new TestableLastfmTagApiCallGenerator(
            apiCallService, snapshotService, entityService);
            
        LastfmTag olderTag = createTag(1L, "Tag1", ApprovalStatus.APPROVED, 1000);
        LastfmTag newerTag = createTag(2L, "Tag2", ApprovalStatus.APPROVED, 1000);
        
        // When & Then
        assertTrue(generator.hasHigherPriority(olderTag, newerTag), 
            "Tag with lower ID (older) should have higher priority");
        assertFalse(generator.hasHigherPriority(newerTag, olderTag), 
            "Tag with higher ID (newer) should not have higher priority");
    }

    @Test
    void isValidForApiCall_shouldReturnTrue_whenTagHasName() {
        // Given
        generator = new TestableLastfmTagApiCallGenerator(
            apiCallService, snapshotService, entityService);
            
        LastfmTag tag = createTag(1L, "Tag1", ApprovalStatus.APPROVED, 1000);
        
        // When & Then
        assertTrue(generator.isValidForApiCall(tag), 
            "Tag with name should be valid for API call");
    }

    @Test
    void isValidForApiCall_shouldReturnFalse_whenTagHasNoName() {
        // Given
        generator = new TestableLastfmTagApiCallGenerator(
            apiCallService, snapshotService, entityService);
            
        LastfmTag tag = createTag(1L, null, ApprovalStatus.APPROVED, 1000);
        
        // When & Then
        assertFalse(generator.isValidForApiCall(tag), 
            "Tag without name should not be valid for API call");
    }

    @Test
    void getApiCallUniqueKey_shouldReturnTagName() {
        // Given
        generator = new TestableLastfmTagApiCallGenerator(
            apiCallService, snapshotService, entityService);
            
        LastfmTag tag = createTag(1L, "Tag1", ApprovalStatus.APPROVED, 1000);
        
        // When
        String key = generator.getApiCallUniqueKey(tag);
        
        // Then
        assertEquals("Tag1", key, "Key should be the tag name");
    }

    // open protected methods for testing
    private static class TestableLastfmTagApiCallGenerator extends LastfmTagApiCallGenerator {
        public TestableLastfmTagApiCallGenerator(
            LastfmApiCallService apiCallService,
            LastfmDataSnapshotService snapshotService,
            LastfmApiCallEntityService entityService
        ) {
            super(apiCallService, snapshotService, entityService);
        }

        @Override
        public List<LastfmTag> deduplicateEntitiesForApiCalls(List<LastfmTag> entities) {
            return super.deduplicateEntitiesForApiCalls(entities);
        }

        @Override
        public LastfmApiCallType getApiCallType() {
            return LastfmApiCallType.TAG_TOP_ARTISTS;
        }

        @Override
        protected int getDueDurationDays() {
            return 7;
        }
        
        @Override
        public boolean isValidForApiCall(LastfmTag tag) {
            return super.isValidForApiCall(tag);
        }
        
        @Override
        public String getApiCallUniqueKey(LastfmTag tag) {
            return super.getApiCallUniqueKey(tag);
        }
        
        @Override
        public boolean hasHigherPriority(LastfmTag candidate, LastfmTag existing) {
            return super.hasHigherPriority(candidate, existing);
        }
    }

    private LastfmTag createTag(Long id, String name, ApprovalStatus status, Integer usageCount) {
        // Create a mock API call for the required field
        LastfmApiCall mockApiCall = LastfmApiCall.builder()
            .id(1L)
            .type(LastfmApiCallType.TAG_TOP_TAGS)
            .dataSnapshotId(1L)
            .dueDttm(java.time.Instant.now())
            .params(Map.of())
            .build();
            
        LastfmTag tag = LastfmTag.builder()
            .name(name)
            .approvalStatus(status)
            .usageCount(usageCount)
            .apiCall(mockApiCall)
            .build();
        
        ReflectionTestUtils.setField(tag, "id", id);
        return tag;
    }
}
