package yurykorzun.art.universe.music.data.raw.lastfm.collectable.dto;

import org.junit.jupiter.api.Test;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ArtistSearchParamsTest {

    @Test
    void constructor_shouldCreateInstanceWithAllParameters() {
        // Given
        String search = "test";
        Long minPlayCount = 1000L;
        Long minListenersCount = 500L;
        Set<Integer> approvalStatuses = new HashSet<>();
        approvalStatuses.add(ApprovalStatus.APPROVED.getCode());
        Long tagId = 123L;

        // When
        ArtistSearchParams params = new ArtistSearchParams(search, minPlayCount, minListenersCount, approvalStatuses, tagId);

        // Then
        assertEquals(search, params.search());
        assertEquals(minPlayCount, params.minPlayCount());
        assertEquals(minListenersCount, params.minListenersCount());
        assertEquals(approvalStatuses, params.approvalStatuses());
        assertEquals(tagId, params.tagId());
    }

    @Test
    void constructor_shouldHandleNullParameters() {
        // When
        ArtistSearchParams params = new ArtistSearchParams(null, null, null, null, null);

        // Then
        assertNull(params.search());
        assertNull(params.minPlayCount());
        assertNull(params.minListenersCount());
        assertNull(params.approvalStatuses());
        assertNull(params.tagId());
    }
    
    @Test
    void constructor_shouldHandlePartialParameters() {
        // Given
        String search = "test";
        
        // When
        ArtistSearchParams params = new ArtistSearchParams(search, null, null, null, null);
        
        // Then
        assertEquals(search, params.search());
        assertNull(params.minPlayCount());
        assertNull(params.minListenersCount());
        assertNull(params.approvalStatuses());
        assertNull(params.tagId());
    }
    
    @Test
    void equals_shouldReturnTrue_whenObjectsAreEqual() {
        // Given
        String search = "test";
        Long minPlayCount = 1000L;
        Long minListenersCount = 500L;
        Set<Integer> approvalStatuses = new HashSet<>();
        approvalStatuses.add(ApprovalStatus.APPROVED.getCode());
        
        ArtistSearchParams params1 = new ArtistSearchParams(search, minPlayCount, minListenersCount, approvalStatuses, null);
        ArtistSearchParams params2 = new ArtistSearchParams(search, minPlayCount, minListenersCount, approvalStatuses, null);
        
        // When & Then
        assertEquals(params1, params2);
        assertEquals(params1.hashCode(), params2.hashCode());
    }
    
    @Test
    void equals_shouldReturnFalse_whenObjectsAreDifferent() {
        // Given
        ArtistSearchParams params1 = new ArtistSearchParams("test1", 1000L, 500L, null, null);
        ArtistSearchParams params2 = new ArtistSearchParams("test2", 1000L, 500L, null, null);
        
        // When & Then
        assertNotEquals(params1, params2);
    }
    
    @Test
    void toString_shouldReturnNonEmptyString() {
        // Given
        ArtistSearchParams params = new ArtistSearchParams("test", 1000L, 500L, null, null);
        
        // When
        String result = params.toString();
        
        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.contains("test"));
        assertTrue(result.contains("1000"));
        assertTrue(result.contains("500"));
    }
    
    @Test
    void constructor_shouldHandleTagIdParameter() {
        // Given
        Long tagId = 456L;
        
        // When
        ArtistSearchParams params = new ArtistSearchParams(null, null, null, null, tagId);
        
        // Then
        assertNull(params.search());
        assertNull(params.minPlayCount());
        assertNull(params.minListenersCount());
        assertNull(params.approvalStatuses());
        assertEquals(tagId, params.tagId());
    }
}
