package yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.dto;

import org.junit.jupiter.api.Test;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AlbumSearchParamsTest {

    @Test
    void constructor_shouldCreateInstanceWithAllParameters() {
        // Given
        String search = "test";
        Long minPlayCount = 1000L;
        Long minListenersCount = 500L;
        Long artistId = 123L;
        Set<Integer> approvalStatuses = new HashSet<>();
        approvalStatuses.add(ApprovalStatus.APPROVED.getCode());
        Long tagId = 456L;

        // When
        AlbumSearchParams params = new AlbumSearchParams(search, minPlayCount, minListenersCount, artistId, approvalStatuses, tagId);

        // Then
        assertEquals(search, params.search());
        assertEquals(minPlayCount, params.minPlayCount());
        assertEquals(minListenersCount, params.minListenersCount());
        assertEquals(artistId, params.artistId());
        assertEquals(approvalStatuses, params.approvalStatuses());
        assertEquals(tagId, params.tagId());
    }

    @Test
    void constructor_shouldHandleNullParameters() {
        // When
        AlbumSearchParams params = new AlbumSearchParams(null, null, null, null, null, null);

        // Then
        assertNull(params.search());
        assertNull(params.minPlayCount());
        assertNull(params.minListenersCount());
        assertNull(params.artistId());
        assertNull(params.approvalStatuses());
        assertNull(params.tagId());
    }
    
    @Test
    void constructor_shouldHandlePartialParameters() {
        // Given
        String search = "test";
        Long artistId = 123L;
        
        // When
        AlbumSearchParams params = new AlbumSearchParams(search, null, null, artistId, null, null);
        
        // Then
        assertEquals(search, params.search());
        assertNull(params.minPlayCount());
        assertNull(params.minListenersCount());
        assertEquals(artistId, params.artistId());
        assertNull(params.approvalStatuses());
        assertNull(params.tagId());
    }
    
    @Test
    void constructor_shouldHandleTagIdParameter() {
        // Given
        Long tagId = 789L;
        
        // When
        AlbumSearchParams params = new AlbumSearchParams(null, null, null, null, null, tagId);
        
        // Then
        assertNull(params.search());
        assertNull(params.minPlayCount());
        assertNull(params.minListenersCount());
        assertNull(params.artistId());
        assertNull(params.approvalStatuses());
        assertEquals(tagId, params.tagId());
    }
    
    @Test
    void equals_shouldReturnTrue_whenObjectsAreEqual() {
        // Given
        String search = "test";
        Long minPlayCount = 1000L;
        Long minListenersCount = 500L;
        Long artistId = 123L;
        Set<Integer> approvalStatuses = new HashSet<>();
        approvalStatuses.add(ApprovalStatus.APPROVED.getCode());
        Long tagId = 456L;
        
        AlbumSearchParams params1 = new AlbumSearchParams(search, minPlayCount, minListenersCount, artistId, approvalStatuses, tagId);
        AlbumSearchParams params2 = new AlbumSearchParams(search, minPlayCount, minListenersCount, artistId, approvalStatuses, tagId);
        
        // When & Then
        assertEquals(params1, params2);
        assertEquals(params1.hashCode(), params2.hashCode());
    }
    
    @Test
    void equals_shouldReturnFalse_whenObjectsAreDifferent() {
        // Given
        AlbumSearchParams params1 = new AlbumSearchParams("test1", 1000L, 500L, 123L, null, null);
        AlbumSearchParams params2 = new AlbumSearchParams("test2", 1000L, 500L, 123L, null, null);
        
        // When & Then
        assertNotEquals(params1, params2);
    }
    
    @Test
    void toString_shouldReturnNonEmptyString() {
        // Given
        AlbumSearchParams params = new AlbumSearchParams("test", 1000L, 500L, 123L, null, 456L);
        
        // When
        String result = params.toString();
        
        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.contains("test"));
        assertTrue(result.contains("1000"));
        assertTrue(result.contains("500"));
        assertTrue(result.contains("123"));
        assertTrue(result.contains("456"));
    }
}
