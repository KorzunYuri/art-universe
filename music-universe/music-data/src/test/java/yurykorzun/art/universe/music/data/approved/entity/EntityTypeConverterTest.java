package yurykorzun.art.universe.music.data.approved.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import yurykorzun.art.universe.common.CodedRegistry;

import static org.junit.jupiter.api.Assertions.*;

class EntityTypeConverterTest {

    private EntityTypeConverter converter;
    
    @BeforeEach
    void setUp() {
        converter = new EntityTypeConverter();
    }
    
    @Test
    void convertToDatabaseColumn_shouldReturnCode() {
        // Given
        EntityType entityType = EntityType.ARTIST;
        
        // When
        Integer code = converter.convertToDatabaseColumn(entityType);
        
        // Then
        assertEquals(entityType.getCode(), code);
    }
    
    @Test
    void convertToDatabaseColumn_withNull_shouldReturnNull() {
        // When
        Integer code = converter.convertToDatabaseColumn(null);
        
        // Then
        assertNull(code);
    }
    
    @Test
    void convertToEntityAttribute_shouldReturnEntityType() {
        // Given
        Integer code = EntityType.ARTIST.getCode();
        
        // When
        EntityType entityType = converter.convertToEntityAttribute(code);
        
        // Then
        assertEquals(EntityType.ARTIST, entityType);
    }
    
    @Test
    void convertToEntityAttribute_withNull_shouldReturnNull() {
        // When
        EntityType entityType = converter.convertToEntityAttribute(null);
        
        // Then
        assertNull(entityType);
    }
    
    @Test
    void convertToEntityAttribute_withInvalidCode_shouldThrowException() {
        // Given
        Integer invalidCode = 999;
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            converter.convertToEntityAttribute(invalidCode));
    }
    
    @Test
    void codedRegistry_shouldContainEntityTypes() {
        // When
        var artistType = CodedRegistry.getByCode(1, EntityType.class);
        var albumType = CodedRegistry.getByCode(2, EntityType.class);
        var trackType = CodedRegistry.getByCode(3, EntityType.class);
        var categoryType = CodedRegistry.getByCode(4, EntityType.class);
        
        // Then
        assertTrue(artistType.isPresent());
        assertTrue(albumType.isPresent());
        assertTrue(trackType.isPresent());
        assertTrue(categoryType.isPresent());
        
        assertEquals(EntityType.ARTIST, artistType.get());
        assertEquals(EntityType.ALBUM, albumType.get());
        assertEquals(EntityType.TRACK, trackType.get());
        assertEquals(EntityType.CATEGORY, categoryType.get());
    }
}
