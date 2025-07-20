package yurykorzun.art.universe.music.data.master.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RelationPairTest {

    @Test
    void fromString_withValidFormat_shouldCreateRelationPair() {
        // Given
        String pairString = "123-456";
        
        // When
        RelationPair pair = RelationPair.fromString(pairString);
        
        // Then
        assertEquals(123L, pair.getSourceId());
        assertEquals(456L, pair.getTargetId());
    }
    
    @Test
    void fromString_withInvalidFormat_shouldThrowException() {
        // Given
        String invalidPairString = "123-456-789";
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            RelationPair.fromString(invalidPairString));
    }
    
    @Test
    void fromString_withNonNumericValues_shouldThrowException() {
        // Given
        String invalidPairString = "abc-def";
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            RelationPair.fromString(invalidPairString));
    }
    
    @Test
    void builder_shouldCreateRelationPair() {
        // When
        RelationPair pair = RelationPair.builder()
            .sourceId(123L)
            .targetId(456L)
            .build();
        
        // Then
        assertEquals(123L, pair.getSourceId());
        assertEquals(456L, pair.getTargetId());
    }
    
    @Test
    void equalsAndHashCode_withSameValues_shouldBeEqual() {
        // Given
        RelationPair pair1 = new RelationPair(123L, 456L);
        RelationPair pair2 = new RelationPair(123L, 456L);
        
        // Then
        assertEquals(pair1, pair2);
        assertEquals(pair1.hashCode(), pair2.hashCode());
    }
    
    @Test
    void equalsAndHashCode_withDifferentValues_shouldNotBeEqual() {
        // Given
        RelationPair pair1 = new RelationPair(123L, 456L);
        RelationPair pair2 = new RelationPair(789L, 101L);
        
        // Then
        assertNotEquals(pair1, pair2);
        assertNotEquals(pair1.hashCode(), pair2.hashCode());
    }
}
