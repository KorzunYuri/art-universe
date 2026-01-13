package yurykorzun.art.universe.music.data.raw.lastfm.domain.dto;

import org.junit.jupiter.api.Test;
import yurykorzun.art.universe.common.data.raw.domain.entity.ApprovalStatus;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TagSearchParamsTest {

    @Test
    void constructor_shouldCreateInstanceWithAllParameters() {
        // Given
        String search = "test";
        Set<Integer> approvalStatuses = new HashSet<>();
        approvalStatuses.add(ApprovalStatus.APPROVED.getCode());
        Integer minUsageCount = 100;
        Integer minUsageUsersCount = 50;

        // When
        TagSearchParams params = new TagSearchParams(search, approvalStatuses, minUsageCount, minUsageUsersCount);

        // Then
        assertEquals(search, params.search());
        assertEquals(approvalStatuses, params.approvalStatuses());
        assertEquals(minUsageCount, params.minUsageCount());
        assertEquals(minUsageUsersCount, params.minUsageUsersCount());
    }

    @Test
    void constructor_shouldHandleNullParameters() {
        // When
        TagSearchParams params = new TagSearchParams(null, null, null, null);

        // Then
        assertNull(params.search());
        assertNull(params.approvalStatuses());
        assertNull(params.minUsageCount());
        assertNull(params.minUsageUsersCount());
    }
    
    @Test
    void constructor_shouldHandlePartialParameters() {
        // Given
        String search = "test";
        Integer minUsageCount = 100;
        
        // When
        TagSearchParams params = new TagSearchParams(search, null, minUsageCount, null);
        
        // Then
        assertEquals(search, params.search());
        assertNull(params.approvalStatuses());
        assertEquals(minUsageCount, params.minUsageCount());
        assertNull(params.minUsageUsersCount());
    }
    
    @Test
    void equals_shouldReturnTrue_whenObjectsAreEqual() {
        // Given
        String search = "test";
        Set<Integer> approvalStatuses = new HashSet<>();
        approvalStatuses.add(ApprovalStatus.APPROVED.getCode());
        Integer minUsageCount = 100;
        Integer minUsageUsersCount = 50;
        
        TagSearchParams params1 = new TagSearchParams(search, approvalStatuses, minUsageCount, minUsageUsersCount);
        TagSearchParams params2 = new TagSearchParams(search, approvalStatuses, minUsageCount, minUsageUsersCount);
        
        // When & Then
        assertEquals(params1, params2);
        assertEquals(params1.hashCode(), params2.hashCode());
    }
    
    @Test
    void equals_shouldReturnFalse_whenObjectsAreDifferent() {
        // Given
        TagSearchParams params1 = new TagSearchParams("test1", null, 100, null);
        TagSearchParams params2 = new TagSearchParams("test2", null, 200, null);
        
        // When & Then
        assertNotEquals(params1, params2);
    }
    
    @Test
    void toString_shouldReturnNonEmptyString() {
        // Given
        TagSearchParams params = new TagSearchParams("test", null, 100, 50);
        
        // When
        String result = params.toString();
        
        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.contains("test"));
        assertTrue(result.contains("100"));
        assertTrue(result.contains("50"));
    }
}
