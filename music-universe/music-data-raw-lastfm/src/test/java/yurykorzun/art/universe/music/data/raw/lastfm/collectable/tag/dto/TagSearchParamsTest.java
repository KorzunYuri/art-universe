package yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.dto;

import org.junit.jupiter.api.Test;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;

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

        // When
        TagSearchParams params = new TagSearchParams(search, approvalStatuses);

        // Then
        assertEquals(search, params.search());
        assertEquals(approvalStatuses, params.approvalStatuses());
    }

    @Test
    void constructor_shouldHandleNullParameters() {
        // When
        TagSearchParams params = new TagSearchParams(null, null);

        // Then
        assertNull(params.search());
        assertNull(params.approvalStatuses());
    }
    
    @Test
    void constructor_shouldHandlePartialParameters() {
        // Given
        String search = "test";
        
        // When
        TagSearchParams params = new TagSearchParams(search, null);
        
        // Then
        assertEquals(search, params.search());
        assertNull(params.approvalStatuses());
    }
    
    @Test
    void equals_shouldReturnTrue_whenObjectsAreEqual() {
        // Given
        String search = "test";
        Set<Integer> approvalStatuses = new HashSet<>();
        approvalStatuses.add(ApprovalStatus.APPROVED.getCode());
        
        TagSearchParams params1 = new TagSearchParams(search, approvalStatuses);
        TagSearchParams params2 = new TagSearchParams(search, approvalStatuses);
        
        // When & Then
        assertEquals(params1, params2);
        assertEquals(params1.hashCode(), params2.hashCode());
    }
    
    @Test
    void equals_shouldReturnFalse_whenObjectsAreDifferent() {
        // Given
        TagSearchParams params1 = new TagSearchParams("test1", null);
        TagSearchParams params2 = new TagSearchParams("test2", null);
        
        // When & Then
        assertNotEquals(params1, params2);
    }
    
    @Test
    void toString_shouldReturnNonEmptyString() {
        // Given
        TagSearchParams params = new TagSearchParams("test", null);
        
        // When
        String result = params.toString();
        
        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.contains("test"));
    }
}
